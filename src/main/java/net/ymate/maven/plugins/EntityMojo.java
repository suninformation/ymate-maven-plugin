/*
 * Copyright 2007-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.ymate.maven.plugins;

import freemarker.template.TemplateException;
import net.ymate.platform.commons.ConsoleTableBuilder;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.Application;
import net.ymate.platform.core.IApplication;
import net.ymate.platform.persistence.jdbc.IDatabase;
import net.ymate.platform.persistence.jdbc.IDatabaseConfig;
import net.ymate.platform.persistence.jdbc.JDBC;
import net.ymate.platform.persistence.jdbc.scaffold.EntityInfo;
import net.ymate.platform.persistence.jdbc.scaffold.INamedFilter;
import net.ymate.platform.persistence.jdbc.scaffold.Scaffold;
import net.ymate.platform.persistence.jdbc.scaffold.TableInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-12-24 11:38
 */
@Mojo(name = "entity", requiresDependencyResolution = ResolutionScope.RUNTIME, requiresDependencyCollection = ResolutionScope.RUNTIME)
@Execute(phase = LifecyclePhase.COMPILE)
public class EntityMojo extends AbstractPersistenceMojo {

    @Parameter(required = true, readonly = true, defaultValue = "${project}")
    private MavenProject mavenProject;

    /**
     * 是否为视图
     */
    @Parameter(property = "view")
    private boolean view;

    /**
     * 是否仅在控制台输出结构信息（不生成任何文件）
     */
    @Parameter(property = "showOnly")
    private boolean showOnly;

    @Parameter(property = "beanOnly")
    private boolean beanOnly;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try (IApplication application = new Application(buildApplicationConfigureFactory())) {
            application.initialize();
            //
            Scaffold.Builder builder = Scaffold.builder(application, false);
            String namedFilterClass = application.getParam(IDatabaseConfig.PARAMS_JDBC_NAMED_FILTER_CLASS);
            if (StringUtils.isNotBlank(namedFilterClass)) {
                builder.namedFilter((INamedFilter) buildRuntimeClassLoader(mavenProject).loadClass(namedFilterClass).newInstance());
            }
            doCreateEntityClassFiles(application.getModuleManager().getModule(JDBC.class), builder.build(), view);
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), RuntimeUtils.unwrapThrow(e));
        }
    }

    private void doCreateEntityClassFiles(IDatabase owner, Scaffold scaffold, boolean view) throws Exception {
        List<TableInfo> tableInfos = scaffold.getTables(owner, getDataSource(), view);
        if (!tableInfos.isEmpty()) {
            Map<String, Object> properties = new HashMap<>(16);
            properties.put("config", scaffold);
            properties.put("lastUpdateTime", new Date());
            //
            if (!showOnly && scaffold.isUseBaseEntity()) {
                doWriteTargetFile(scaffold, String.format("/%s/BaseEntity.java", StringUtils.lowerCase(scaffold.getClassSuffix())), "/entity/BaseEntity", properties);
            }
            for (TableInfo tableInfo : tableInfos) {
                if (!showOnly) {
                    EntityInfo entityInfo = scaffold.buildEntityInfo(tableInfo);
                    properties.put("entityInfo", entityInfo);
                    //
                    if (beanOnly) {
                        doWriteTargetFile(scaffold, String.format("/%s/%sBean.java", StringUtils.lowerCase(scaffold.getClassSuffix()), entityInfo.getName()), "/entity/Bean", properties);
                    } else {
                        String finalEntityName = String.format("%s%s", entityInfo.getName(), scaffold.isUseClassSuffix() ? StringUtils.capitalize(scaffold.getClassSuffix()) : StringUtils.EMPTY);
                        doWriteTargetFile(scaffold, String.format("/%s/%s.java", StringUtils.lowerCase(scaffold.getClassSuffix()), finalEntityName), view ? "/entity/View" : "/entity/Entity", properties);
                        if (!view && tableInfo.getPrimaryKeys().size() > 1) {
                            doWriteTargetFile(scaffold, String.format("/%s/%sPK.java", StringUtils.lowerCase(scaffold.getClassSuffix()), entityInfo.getName()), "/entity/EntityPK", properties);
                        }
                    }
                } else {
                    ConsoleTableBuilder consoleTableBuilder = ConsoleTableBuilder.create(10).escape();
                    System.out.println(String.format("%s_NAME: %s", view ? "VIEW" : "TABLE", tableInfo.getName()));
                    if (ConsoleTableBuilder.TYPE_MARKDOWN.equals(getFormat())) {
                        System.out.println();
                        consoleTableBuilder.markdown();
                    } else if (ConsoleTableBuilder.TYPE_CSV.equals(getFormat())) {
                        consoleTableBuilder.csv();
                    }
                    consoleTableBuilder.addRow()
                            .addColumn("COLUMN_NAME")
                            .addColumn("COLUMN_CLASS_NAME")
                            .addColumn("PRIMARY_KEY")
                            .addColumn("AUTO_INCREMENT")
                            .addColumn("SIGNED")
                            .addColumn("PRECISION")
                            .addColumn("SCALE")
                            .addColumn("NULLABLE")
                            .addColumn("DEFAULT")
                            .addColumn("REMARKS");
                    tableInfo.getColumns().values().forEach(columnInfo -> consoleTableBuilder.addRow()
                            .addColumn(columnInfo.getColumnName())
                            .addColumn(columnInfo.getColumnType())
                            .addColumn(columnInfo.isPrimaryKey() ? "TRUE" : "FALSE")
                            .addColumn(columnInfo.isAutoIncrement() ? "TRUE" : "FALSE")
                            .addColumn(columnInfo.isSigned() ? "TRUE" : "FALSE")
                            .addColumn(columnInfo.getPrecision() + "")
                            .addColumn(columnInfo.getScale() + "")
                            .addColumn(columnInfo.isNullable() ? "TRUE" : "FALSE")
                            .addColumn(columnInfo.getDefaultValue())
                            .addColumn(columnInfo.getRemarks()));
                    System.out.println(consoleTableBuilder.toString());
                }
            }
        }
    }

    private void doWriteTargetFile(Scaffold scaffold, String fileName, String tmplFile, Map<String, Object> properties) throws IOException, TemplateException {
        File outputFile = new File(scaffold.getOutputPath(), new File(scaffold.getPackageName().replace('.', '/'), fileName).getPath());
        File parentFile = outputFile.getParentFile();
        if (parentFile.exists() || parentFile.mkdirs()) {
            doWriterTemplateFile(outputFile, tmplFile, properties);
        }
    }
}
