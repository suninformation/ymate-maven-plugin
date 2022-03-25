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

import net.ymate.platform.commons.util.RuntimeUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 模块代码生成器
 *
 * @author 刘镇 (suninformation@163.com) on 17/2/21 下午4:27
 */
@Mojo(name = "module")
public class ModuleMojo extends AbstractMojo {

    /**
     * 模块名称
     */
    @Parameter(property = "name", required = true)
    private String name;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        name = StringUtils.capitalize(name);
        //
        Map<String, Object> properties = new HashMap<>(16);
        properties.put("moduleName", name);
        properties.put("packageName", getPackageName());
        properties.put("moduleArtifactId", getProjectName());
        //
        getLog().info("properties:");
        getLog().info("\t|--moduleName:" + name);
        getLog().info("\t|--packageName:" + getPackageName());
        getLog().info("\t|--moduleArtifactId:" + getProjectName());
        //
        try {
            File path = new File(String.format("%s/src/main/java", getBasedir()), getPackageName().replace(".", "/"));
            //
            doWriterTemplateFile(new File(getBasedir(), "/misc/ymp-conf.properties"), "/module/module-config-file", properties);
            doWriterTemplateFile(new File(path, String.format("I%s.java", name)), "/module/module-interface", properties);
            doWriterTemplateFile(new File(path, String.format("%s.java", name)), "/module/module-class", properties);
            doWriterTemplateFile(new File(path, String.format("I%sConfig.java", name)), "/module/module-config", properties);
            doWriterTemplateFile(new File(path, String.format("annotation/%sConf.java", name)), "/module/module-annotation", properties);
            doWriterTemplateFile(new File(path, String.format("impl/Default%sConfig.java", name)), "/module/module-config-impl", properties);
            doWriterTemplateFile(new File(path, String.format("impl/Default%sConfigurable.java", name)), "/module/module-configurable-impl", properties);
            //
            path = new File(String.format("%s/src/main/resources/META-INF/services/internal/net.ymate.platform.core.module.IModule", getBasedir()));
            String charset = "UTF-8";
            String className = String.format("%s.%s", getPackageName(), name);
            if (path.exists()) {
                List<String> lines = FileUtils.readLines(path, charset);
                if (!lines.contains(className)) {
                    lines.add(className);
                    FileUtils.writeLines(path, charset, lines);
                }
            } else {
                File parent = path.getParentFile();
                if (parent.exists() || parent.mkdirs()) {
                    if (path.createNewFile()) {
                        FileUtils.write(path, className, charset);
                    }
                }
            }
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), RuntimeUtils.unwrapThrow(e));
        }
    }
}
