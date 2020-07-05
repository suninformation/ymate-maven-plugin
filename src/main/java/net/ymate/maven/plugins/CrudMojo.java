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

import com.alibaba.fastjson.annotation.JSONField;
import net.ymate.platform.commons.DateTimeHelper;
import net.ymate.platform.commons.json.JsonWrapper;
import net.ymate.platform.commons.util.DateTimeUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.Application;
import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.persistence.base.EntityMeta;
import net.ymate.platform.persistence.jdbc.IDatabase;
import net.ymate.platform.persistence.jdbc.IDatabaseConfig;
import net.ymate.platform.persistence.jdbc.JDBC;
import net.ymate.platform.persistence.jdbc.query.Cond;
import net.ymate.platform.persistence.jdbc.query.Join;
import net.ymate.platform.persistence.jdbc.query.annotation.QFrom;
import net.ymate.platform.persistence.jdbc.query.annotation.QOrderField;
import net.ymate.platform.persistence.jdbc.scaffold.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 刘镇 (suninformation@163.com) on 2017/10/13 下午2:47
 */
@Mojo(name = "crud", requiresDependencyResolution = ResolutionScope.RUNTIME, requiresDependencyCollection = ResolutionScope.RUNTIME)
//@Execute(phase = LifecyclePhase.COMPILE)
public class CrudMojo extends AbstractPersistenceMojo {

    private static final String DEFAULT_CRUD_FILE = "misc/crud.json";

    @Parameter(required = true, readonly = true, defaultValue = "${project}")
    private MavenProject mavenProject;

    @Parameter(property = "file", defaultValue = DEFAULT_CRUD_FILE)
    private String file;

    @Parameter(property = "filter")
    private String[] filter;

    @Parameter(property = "fromDb")
    private boolean fromDb;

    @Parameter(property = "apidocs")
    private boolean apidocs;

    /**
     * 自定义语言
     */
    @Parameter(property = "language")
    private String language;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            File cfgFile = new File(getBasedir(), StringUtils.defaultIfBlank(file, DEFAULT_CRUD_FILE));
            if (cfgFile.exists() && cfgFile.isFile()) {
                if (fromDb) {
                    buildCrudFromDb(cfgFile);
                } else {
                    InputStream inputStream = new FileInputStream(cfgFile);
                    CApplication cApp = JsonWrapper.deserialize(IOUtils.toByteArray(inputStream), CApplication.class);
                    if (cApp != null) {
                        if (cApp.isLocked()) {
                            getLog().info("CRUD has bean locked.");
                        } else {
                            File path = new File(String.format("%s/src/main/java", getBasedir()), cApp.getPackageName().replace(".", "/"));
                            //
                            Map<String, Object> props = new HashMap<>();
                            props.put("app", cApp);
                            props.put("apidocs", apidocs);
                            //
                            boolean hasQuery = false;
                            //
                            for (CApi cApi : cApp.getApis()) {
                                if (cApi.isLocked()) {
                                    getLog().info("API '" + cApi.getName() + "' has been locked.");
                                    continue;
                                }
                                Map<String, Object> properties = new HashMap<>(props);
                                properties.put("api", cApi);
                                properties.put("entityName", StringUtils.substringAfterLast(cApi.getEntityClass(), "."));
                                properties.put("entityPackageName", StringUtils.substringBeforeLast(cApi.getEntityClass(), "."));
                                //
                                cApi.getProperties().forEach(cProperty -> {
                                    if (StringUtils.equalsIgnoreCase(cProperty.getColumn(), "create_time") && StringUtils.equals(cProperty.getType(), Long.class.getName())) {
                                        properties.put("createTimeProp", cProperty);
                                    } else if (StringUtils.equalsIgnoreCase(cProperty.getColumn(), "last_modify_time") && StringUtils.equals(cProperty.getType(), Long.class.getName())) {
                                        properties.put("lastModifyTimeProp", cProperty);
                                    }
                                });
                                properties.put("hideInListFields", cApi.getProperties().stream().filter(CProperty::isHideInList).map(CProperty::getField).collect(Collectors.toList()));
                                List<CProperty> primaryFields = cApi.getProperties().stream().filter(CProperty::isPrimary).collect(Collectors.toList());
                                boolean multiPrimaryKey = primaryFields.size() > 1;
                                properties.put("primaryFields", primaryFields);
                                properties.put("nonAutoPrimaryFields", primaryFields.stream().filter(cProperty -> !cProperty.isAutoIncrement()).collect(Collectors.toList()));
                                properties.put("multiPrimaryKey", multiPrimaryKey);
                                if (!multiPrimaryKey && !primaryFields.isEmpty()) {
                                    properties.put("primaryKey", primaryFields.get(0));
                                }
                                properties.put("normalFields", cApi.getProperties().stream().filter(cProperty -> !cProperty.isPrimary()).collect(Collectors.toList()));
                                //
                                doWriterTemplateFile(new File(path, String.format("repository/I%sRepository.java", StringUtils.capitalize(cApi.getName()))), "/crud/repository-interface-tmpl", properties);
                                doWriterTemplateFile(new File(path, String.format("repository/impl/%sRepository.java", StringUtils.capitalize(cApi.getName()))), "/crud/repository-tmpl", properties);
                                doWriterTemplateFile(new File(path, String.format("controller/%sController.java", StringUtils.capitalize(cApi.getName()))), "/crud/controller-tmpl", properties);
                                if (cApi.getSettings() == null || cApi.getSettings().enableQuery) {
                                    hasQuery = true;
                                    doWriterTemplateFile(new File(path, String.format("dto/%sDTO.java", StringUtils.capitalize(cApi.getName()))), "/crud/dto-tmpl", properties);
                                    doWriterTemplateFile(new File(path, String.format("vo/%sVO.java", StringUtils.capitalize(cApi.getName()))), "/crud/vo-tmpl", properties);
                                    doWriterTemplateFile(new File(path, String.format("bean/%sBean.java", StringUtils.capitalize(cApi.getName()))), "/crud/bean-tmpl", properties);
                                }
                                if (cApi.getSettings() == null || cApi.getSettings().enableCreate || cApi.getSettings().enableUpdate) {
                                    doWriterTemplateFile(new File(path, String.format("dto/%sUpdateDTO.java", StringUtils.capitalize(cApi.getName()))), "/crud/dto-update-tmpl", properties);
                                    doWriterTemplateFile(new File(path, String.format("bean/%sUpdateBean.java", StringUtils.capitalize(cApi.getName()))), "/crud/bean-update-tmpl", properties);
                                }
                            }
                            if (hasQuery) {
                                doWriterTemplateFile(new File(path, "dto/PageDTO.java"), "/crud/page-dto-tmpl", props);
                            }
                        }
                    }
                }
            } else if (fromDb) {
                buildCrudFromDb(cfgFile);
            } else {
                getLog().warn(String.format("File '%s' does not exist.", cfgFile.getAbsolutePath()));
            }
        } catch (Exception e) {
            getLog().error(e.getMessage(), RuntimeUtils.unwrapThrow(e));
        }
    }

    private void buildCrudFromDb(File cfgFile) throws Exception {
        if (cfgFile == null) {
            cfgFile = new File(getBasedir(), StringUtils.defaultIfBlank(file, DEFAULT_CRUD_FILE));
        }
        if (cfgFile.exists() && cfgFile.isFile()) {
            if (!isOverwrite()) {
                getLog().warn("Skip existing file " + cfgFile);
                return;
            }
        }
        try (IApplication application = new Application(buildApplicationConfigureFactory())) {
            application.initialize();
            //
            Scaffold.Builder builder = Scaffold.builder(application, false);
            String namedFilterClass = application.getParam(IDatabaseConfig.PARAMS_JDBC_NAMED_FILTER_CLASS);
            if (StringUtils.isNotBlank(namedFilterClass)) {
                builder.namedFilter((INamedFilter) buildRuntimeClassLoader(mavenProject).loadClass(namedFilterClass).newInstance());
            }
            Scaffold scaffold = builder.build();
            IDatabase owner = application.getModuleManager().getModule(JDBC.class);
            //
            CApplication cApp = new CApplication()
                    .setName(getProjectName())
                    .setPackageName(getPackageName())
                    .setAuthor("YMP (https://www.ymate.net/")
                    .setVersion(getVersion())
                    .setCreateTime(DateTimeHelper.now().toString(DateTimeUtils.YYYY_MM_DD_HH_MM_SS));
            //
            List<CApi> cApis = new ArrayList<>();
            scaffold.getTables(owner, getDataSource(), false)
                    .forEach(tableInfo -> {
                        if (!ArrayUtils.isEmpty(filter) && !ArrayUtils.contains(filter, tableInfo.getName())) {
                            getLog().info("Table Name: " + tableInfo.getName() + " has been filtered.");
                            return;
                        }
                        cApis.add(buildApi(scaffold, tableInfo, false));
                    });
            cApp.setApis(cApis);
            //
            scaffold.getTables(owner, getDataSource(), true)
                    .forEach(tableInfo -> {
                        if (!ArrayUtils.isEmpty(filter) && !ArrayUtils.contains(filter, tableInfo.getName())) {
                            getLog().info("View Name: " + tableInfo.getName() + " has been filtered.");
                            return;
                        }
                        cApis.add(buildApi(scaffold, tableInfo, true));
                    });
            String content = JsonWrapper.toJsonString(cApp, true, true);
            IOUtils.write(content, new FileOutputStream(cfgFile), StandardCharsets.UTF_8);
            getLog().info("Output file: " + cfgFile);
        }
    }

    private CApi buildApi(Scaffold scaffold, TableInfo tableInfo, boolean view) {
        EntityInfo entityInfo = scaffold.buildEntityInfo(tableInfo);
        //
        CApi cApi = new CApi();
        String entityName = String.format("%s%s", entityInfo.getName(), scaffold.isUseClassSuffix() ? StringUtils.capitalize(scaffold.getClassSuffix()) : StringUtils.EMPTY);
        cApi.setEntityClass(String.format("%s.%s.%s", scaffold.getPackageName(), StringUtils.lowerCase(scaffold.getClassSuffix()), entityName));
        cApi.setName(entityInfo.getName());
        cApi.setMapping("/" + EntityMeta.fieldNameToPropertyName(entityInfo.getTableName(), 0).replace('_', '/'));
        cApi.setQuery(new CQuery()
                .setFroms(Collections.singletonList(new CFrom().setValue(String.format("%s.TABLE_NAME", entityName)).setType(QFrom.Type.TABLE))))
                .setSettings(new CSettings()
                        .setEnableCreate(!view)
                        .setEnableQuery(true)
                        .setEnableUpdate(!view)
                        .setEnableRemove(!view)
                        .setEnableExport(true)
                        .setEnableStatus(!view))
                .setLocked(false);
        //
        List<CProperty> cProperties = new ArrayList<>();
        List<Attr> attrs = new ArrayList<>(entityInfo.getPrimaryKeys());
        entityInfo.getFields().stream().filter(attr -> !attr.getVarType().equals(String.format("%sPK", entityInfo.getName()))).forEach(attrs::add);
        attrs.forEach(attr -> {
            CProperty cProperty = new CProperty()
                    .setName(attr.getVarName())
                    .setType(attr.getVarType())
                    .setColumn(attr.getColumnName());
            boolean isPrimary;
            if (!entityInfo.getPrimaryKeys().isEmpty()) {
                isPrimary = entityInfo.getPrimaryKeys().contains(attr);
            } else {
                isPrimary = StringUtils.equals(entityInfo.getPrimaryKeyName(), attr.getColumnName());
            }
            cProperty.setPrimary(isPrimary)
                    .setAutoIncrement(attr.isAutoIncrement())
                    .setExport(true)
                    .setDefaultValue(attr.getDefaultValue())
                    .setDemoValue(attr.getDefaultValue())
                    .setDescription(attr.getRemarks())
                    .setField(new CField().setValue(String.format("%s.FIELDS.%S", entityName, attr.getColumnName())));
            //
            boolean isRegion = StringUtils.equals(attr.getColumnName(), "create_time");
            if (isRegion) {
                cApi.getQuery().setOrderFields(Collections.singletonList(new COrderField().setValue(String.format("%s.FIELDS.CREATE_TIME", entityName)).setType(QOrderField.Type.DESC)));
            }
            boolean isVersion = StringUtils.equals(attr.getColumnName(), "last_modify_time");
            boolean isStatus = StringUtils.equals(attr.getVarName(), "status");
            boolean isRequired = !view && entityInfo.getNonNullableFields().contains(attr);
            CConfig cConfig = new CConfig()
                    .setQuery(new CQueryConf()
                            .setEnabled(!isPrimary)
                            .setRegion(isRegion || isVersion)
                            .setValidation(new CValidation()
                                    .setEmail(new CVEmail())
                                    .setIdCard(new CVIdCard())
                                    .setLength(new CVLength().setEnabled(!StringUtils.equals(attr.getVarType(), Boolean.class.getName())).setMax(attr.getPrecision()))
                                    .setMobile(new CVMobile())
                                    .setNumeric(new CVNumeric())
                                    .setRegex(new CVRegex())
                                    .setDataRange(new CVDataRange())
                                    .setDateTime(new CVDateTime().setEnabled(isRegion || isVersion).setSingle(!isRegion && !isVersion))))
                    .setCreateOrUpdate(new CCreateOrUpdateConf()
                            .setEnabled(!view && !(isPrimary || isRegion || isVersion || attr.isReadonly()))
                            .setRequired(isRequired)
                            .setValidation(new CValidation()
                                    .setEmail(new CVEmail())
                                    .setIdCard(new CVIdCard())
                                    .setMobile(new CVMobile())
                                    .setNumeric(new CVNumeric())
                                    .setRegex(new CVRegex())
                                    .setDataRange(new CVDataRange())
                                    .setLength(new CVLength().setEnabled(!StringUtils.equals(attr.getVarType(), Boolean.class.getName())).setMax(attr.getPrecision()))));
            if (!view && isStatus) {
                cConfig.setStatus(Arrays.asList(new CStatusConf().setEnabled(true).setName("Enable").setMapping("/enable").setValue("1"),
                        new CStatusConf().setEnabled(true).setName("Disable").setMapping("/disable").setValue("0")));
            }
            cProperty.setConfig(cConfig);
            //
            cProperties.add(cProperty);
        });
        cApi.setProperties(cProperties);
        return cApi;
    }

    public static class CApplication {

        private String name;

        private String version;

        @JSONField(name = "package")
        private String packageName;

        private String author;

        private String createTime;

        private boolean locked;

        private List<CApi> apis;

        public String getName() {
            return name;
        }

        public CApplication setName(String name) {
            this.name = name;
            return this;
        }

        public String getVersion() {
            return version;
        }

        public CApplication setVersion(String version) {
            this.version = version;
            return this;
        }

        public String getPackageName() {
            return packageName;
        }

        public CApplication setPackageName(String packageName) {
            this.packageName = packageName;
            return this;
        }

        public String getAuthor() {
            return author;
        }

        public CApplication setAuthor(String author) {
            this.author = author;
            return this;
        }

        public String getCreateTime() {
            return createTime;
        }

        public CApplication setCreateTime(String createTime) {
            this.createTime = createTime;
            return this;
        }

        public boolean isLocked() {
            return locked;
        }

        public CApplication setLocked(boolean locked) {
            this.locked = locked;
            return this;
        }

        public List<CApi> getApis() {
            return apis;
        }

        public CApplication setApis(List<CApi> apis) {
            this.apis = apis;
            return this;
        }
    }

    public static class CApi {

        private String mapping;

        private String name;

        private String entityClass;

        private String description;

        private CQuery query;

        private List<CProperty> properties;

        private CSettings settings;

        private boolean locked;

        public String getMapping() {
            return mapping;
        }

        public CApi setMapping(String mapping) {
            this.mapping = mapping;
            return this;
        }

        public String getName() {
            return name;
        }

        public CApi setName(String name) {
            this.name = name;
            return this;
        }

        public String getEntityClass() {
            return entityClass;
        }

        public CApi setEntityClass(String entityClass) {
            this.entityClass = entityClass;
            return this;
        }

        public String getDescription() {
            return description;
        }

        public CApi setDescription(String description) {
            this.description = description;
            return this;
        }

        public CQuery getQuery() {
            return query;
        }

        public CApi setQuery(CQuery query) {
            this.query = query;
            return this;
        }

        public List<CProperty> getProperties() {
            return properties;
        }

        public CApi setProperties(List<CProperty> properties) {
            this.properties = properties;
            return this;
        }

        public CSettings getSettings() {
            return settings;
        }

        public CApi setSettings(CSettings settings) {
            this.settings = settings;
            return this;
        }

        public boolean isLocked() {
            return locked;
        }

        public CApi setLocked(boolean locked) {
            this.locked = locked;
            return this;
        }
    }

    public static class CSettings {

        private boolean enableQuery = true;

        private boolean enableCreate = true;

        private boolean enableUpdate = true;

        private boolean enableRemove = true;

        private boolean enableExport = true;

        private boolean enableStatus = true;

        public boolean isEnableQuery() {
            return enableQuery;
        }

        public CSettings setEnableQuery(boolean enableQuery) {
            this.enableQuery = enableQuery;
            return this;
        }

        public boolean isEnableCreate() {
            return enableCreate;
        }

        public CSettings setEnableCreate(boolean enableCreate) {
            this.enableCreate = enableCreate;
            return this;
        }

        public boolean isEnableUpdate() {
            return enableUpdate;
        }

        public CSettings setEnableUpdate(boolean enableUpdate) {
            this.enableUpdate = enableUpdate;
            return this;
        }

        public boolean isEnableRemove() {
            return enableRemove;
        }

        public CSettings setEnableRemove(boolean enableRemove) {
            this.enableRemove = enableRemove;
            return this;
        }

        public boolean isEnableExport() {
            return enableExport;
        }

        public CSettings setEnableExport(boolean enableExport) {
            this.enableExport = enableExport;
            return this;
        }

        public boolean isEnableStatus() {
            return enableStatus;
        }

        public CSettings setEnableStatus(boolean enableStatus) {
            this.enableStatus = enableStatus;
            return this;
        }
    }

    public static class CProperty {

        private String name;

        private String column;

        private String type;

        private boolean primary;

        private boolean autoIncrement;

        private boolean export;

        private boolean hideInList;

        private String defaultValue;

        private String demoValue;

        private String description;

        private CField field;

        private CConfig config;

        public String getName() {
            return name;
        }

        public CProperty setName(String name) {
            this.name = name;
            return this;
        }

        public String getColumn() {
            return column;
        }

        public CProperty setColumn(String column) {
            this.column = column;
            return this;
        }

        public String getType() {
            return type;
        }

        public CProperty setType(String type) {
            this.type = type;
            return this;
        }

        public boolean isPrimary() {
            return primary;
        }

        public CProperty setPrimary(boolean primary) {
            this.primary = primary;
            return this;
        }

        public boolean isAutoIncrement() {
            return autoIncrement;
        }

        public CProperty setAutoIncrement(boolean autoIncrement) {
            this.autoIncrement = autoIncrement;
            return this;
        }

        public boolean isExport() {
            return export;
        }

        public CProperty setExport(boolean export) {
            this.export = export;
            return this;
        }

        public boolean isHideInList() {
            return hideInList;
        }

        public void setHideInList(boolean hideInList) {
            this.hideInList = hideInList;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public CProperty setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public String getDemoValue() {
            return demoValue;
        }

        public CProperty setDemoValue(String demoValue) {
            this.demoValue = demoValue;
            return this;
        }

        public String getDescription() {
            return description;
        }

        public CProperty setDescription(String description) {
            this.description = description;
            return this;
        }

        public CField getField() {
            return field;
        }

        public CProperty setField(CField field) {
            this.field = field;
            return this;
        }

        public CConfig getConfig() {
            return config;
        }

        public CProperty setConfig(CConfig config) {
            this.config = config;
            return this;
        }
    }

    public static class CConfig {

        private CQueryConf query;

        private CCreateOrUpdateConf createOrUpdate;

        private List<CStatusConf> status;

        public CQueryConf getQuery() {
            return query;
        }

        public CConfig setQuery(CQueryConf query) {
            this.query = query;
            return this;
        }

        public CCreateOrUpdateConf getCreateOrUpdate() {
            return createOrUpdate;
        }

        public CConfig setCreateOrUpdate(CCreateOrUpdateConf createOrUpdate) {
            this.createOrUpdate = createOrUpdate;
            return this;
        }

        public List<CStatusConf> getStatus() {
            return status;
        }

        public CConfig setStatus(List<CStatusConf> status) {
            this.status = status;
            return this;
        }
    }

    public static class CQueryConf {

        private boolean enabled;

        private boolean required;

        private boolean like;

        private boolean region;

        private CValidation validation;

        public CValidation getValidation() {
            return validation;
        }

        public CQueryConf setValidation(CValidation validation) {
            this.validation = validation;
            return this;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public CQueryConf setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public boolean isRequired() {
            return required;
        }

        public CQueryConf setRequired(boolean required) {
            this.required = required;
            return this;
        }

        public boolean isLike() {
            return like;
        }

        public CQueryConf setLike(boolean like) {
            this.like = like;
            return this;
        }

        public boolean isRegion() {
            return region;
        }

        public CQueryConf setRegion(boolean region) {
            this.region = region;
            return this;
        }
    }

    public static class CCreateOrUpdateConf {

        private boolean enabled;

        private boolean required;

        private CValidation validation;

        public CValidation getValidation() {
            return validation;
        }

        public CCreateOrUpdateConf setValidation(CValidation validation) {
            this.validation = validation;
            return this;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public CCreateOrUpdateConf setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public boolean isRequired() {
            return required;
        }

        public CCreateOrUpdateConf setRequired(boolean required) {
            this.required = required;
            return this;
        }
    }

    public static class CStatusConf {

        private boolean enabled;

        private String name;

        private String mapping;

        private String value;

        private boolean reason;

        private String description;

        public boolean isEnabled() {
            return enabled;
        }

        public CStatusConf setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public String getName() {
            return name;
        }

        public CStatusConf setName(String name) {
            this.name = name;
            return this;
        }

        public String getMapping() {
            return mapping;
        }

        public CStatusConf setMapping(String mapping) {
            this.mapping = mapping;
            return this;
        }

        public String getValue() {
            return value;
        }

        public CStatusConf setValue(String value) {
            this.value = value;
            return this;
        }

        public boolean isReason() {
            return reason;
        }

        public CStatusConf setReason(boolean reason) {
            this.reason = reason;
            return this;
        }

        public String getDescription() {
            return description;
        }

        public CStatusConf setDescription(String description) {
            this.description = description;
            return this;
        }
    }

    public static class CQuery {

        private List<CFrom> froms;

        private List<CJoin> joins;

        private List<COrderField> orderFields;

        public List<CFrom> getFroms() {
            return froms;
        }

        public CQuery setFroms(List<CFrom> froms) {
            this.froms = froms;
            return this;
        }

        public List<CJoin> getJoins() {
            return joins;
        }

        public CQuery setJoins(List<CJoin> joins) {
            this.joins = joins;
            return this;
        }

        public List<COrderField> getOrderFields() {
            return orderFields;
        }

        public CQuery setOrderFields(List<COrderField> orderFields) {
            this.orderFields = orderFields;
            return this;
        }
    }

    public static class CValidation {

        private CVLength length;

        private CVRegex regex;

        private CVMobile mobile;

        private CVEmail email;

        private CVIdCard idCard;

        private CVNumeric numeric;

        private CVDateTime dateTime;

        private CVDataRange dataRange;

        public CVLength getLength() {
            return length;
        }

        public CValidation setLength(CVLength length) {
            this.length = length;
            return this;
        }

        public CVRegex getRegex() {
            return regex;
        }

        public CValidation setRegex(CVRegex regex) {
            this.regex = regex;
            return this;
        }

        public CVMobile getMobile() {
            return mobile;
        }

        public CValidation setMobile(CVMobile mobile) {
            this.mobile = mobile;
            return this;
        }

        public CVEmail getEmail() {
            return email;
        }

        public CValidation setEmail(CVEmail email) {
            this.email = email;
            return this;
        }

        public CVIdCard getIdCard() {
            return idCard;
        }

        public CValidation setIdCard(CVIdCard idCard) {
            this.idCard = idCard;
            return this;
        }

        public CVNumeric getNumeric() {
            return numeric;
        }

        public CValidation setNumeric(CVNumeric numeric) {
            this.numeric = numeric;
            return this;
        }

        public CVDateTime getDateTime() {
            return dateTime;
        }

        public CValidation setDateTime(CVDateTime dateTime) {
            this.dateTime = dateTime;
            return this;
        }

        public CVDataRange getDataRange() {
            return dataRange;
        }

        public CValidation setDataRange(CVDataRange dataRange) {
            this.dataRange = dataRange;
            return this;
        }
    }

    public static class CVRegex {

        private String regex;

        private boolean enabled;

        private String msg;

        public boolean isEnabled() {
            return enabled;
        }

        public CVRegex setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public String getMsg() {
            return msg;
        }

        public CVRegex setMsg(String msg) {
            this.msg = msg;
            return this;
        }

        public String getRegex() {
            return regex;
        }

        public CVRegex setRegex(String regex) {
            this.regex = regex;
            return this;
        }
    }

    public static class CVMobile {

        private String regex;

        private boolean enabled;

        private String msg;

        public boolean isEnabled() {
            return enabled;
        }

        public CVMobile setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public String getMsg() {
            return msg;
        }

        public CVMobile setMsg(String msg) {
            this.msg = msg;
            return this;
        }

        public String getRegex() {
            return regex;
        }

        public CVMobile setRegex(String regex) {
            this.regex = regex;
            return this;
        }
    }

    public static class CVEmail {

        private boolean enabled;

        private String msg;

        public boolean isEnabled() {
            return enabled;
        }

        public CVEmail setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public String getMsg() {
            return msg;
        }

        public CVEmail setMsg(String msg) {
            this.msg = msg;
            return this;
        }
    }

    public static class CVIdCard {

        private boolean enabled;

        private String msg;

        public boolean isEnabled() {
            return enabled;
        }

        public CVIdCard setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public String getMsg() {
            return msg;
        }

        public CVIdCard setMsg(String msg) {
            this.msg = msg;
            return this;
        }
    }

    public static class CVLength {

        private int max;

        private int min;

        private int eq;

        private boolean enabled;

        private String msg;

        public boolean isEnabled() {
            return enabled;
        }

        public CVLength setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public String getMsg() {
            return msg;
        }

        public CVLength setMsg(String msg) {
            this.msg = msg;
            return this;
        }

        public int getMax() {
            return max;
        }

        public CVLength setMax(int max) {
            this.max = max;
            return this;
        }

        public int getMin() {
            return min;
        }

        public CVLength setMin(int min) {
            this.min = min;
            return this;
        }

        public int getEq() {
            return eq;
        }

        public CVLength setEq(int eq) {
            this.eq = eq;
            return this;
        }
    }

    public static class CVNumeric {

        private int max;

        private int min;

        private int eq;

        private int decimals;

        private boolean enabled;

        private String msg;

        public boolean isEnabled() {
            return enabled;
        }

        public CVNumeric setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public String getMsg() {
            return msg;
        }

        public CVNumeric setMsg(String msg) {
            this.msg = msg;
            return this;
        }

        public int getMax() {
            return max;
        }

        public CVNumeric setMax(int max) {
            this.max = max;
            return this;
        }

        public int getMin() {
            return min;
        }

        public CVNumeric setMin(int min) {
            this.min = min;
            return this;
        }

        public int getEq() {
            return eq;
        }

        public CVNumeric setEq(int eq) {
            this.eq = eq;
            return this;
        }

        public int getDecimals() {
            return decimals;
        }

        public CVNumeric setDecimals(int decimals) {
            this.decimals = decimals;
            return this;
        }
    }

    public static class CVDateTime {

        private String pattern;

        private boolean single;

        private String separator;

        private int maxDays;

        private boolean enabled;

        private String msg;

        public boolean isEnabled() {
            return enabled;
        }

        public CVDateTime setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public String getMsg() {
            return msg;
        }

        public CVDateTime setMsg(String msg) {
            this.msg = msg;
            return this;
        }

        public String getPattern() {
            return pattern;
        }

        public CVDateTime setPattern(String pattern) {
            this.pattern = pattern;
            return this;
        }

        public boolean isSingle() {
            return single;
        }

        public CVDateTime setSingle(boolean single) {
            this.single = single;
            return this;
        }

        public String getSeparator() {
            return separator;
        }

        public CVDateTime setSeparator(String separator) {
            this.separator = separator;
            return this;
        }

        public int getMaxDays() {
            return maxDays;
        }

        public CVDateTime setMaxDays(int maxDays) {
            this.maxDays = maxDays;
            return this;
        }
    }

    public static class CVDataRange {

        private List<String> values;

        private boolean ignoreCase;

        private boolean enabled;

        private String msg;

        public boolean isEnabled() {
            return enabled;
        }

        public CVDataRange setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public String getMsg() {
            return msg;
        }

        public CVDataRange setMsg(String msg) {
            this.msg = msg;
            return this;
        }

        public List<String> getValues() {
            return values;
        }

        public CVDataRange setValues(List<String> values) {
            this.values = values;
            return this;
        }

        public boolean isIgnoreCase() {
            return ignoreCase;
        }

        public CVDataRange setIgnoreCase(boolean ignoreCase) {
            this.ignoreCase = ignoreCase;
            return this;
        }
    }

    public static class CWithLabel {

        private String name;

        private String label;

        public String getName() {
            return name;
        }

        public CWithLabel setName(String name) {
            this.name = name;
            return this;
        }

        public String getLabel() {
            return label;
        }

        public CWithLabel setLabel(String label) {
            this.label = label;
            return this;
        }
    }

    public static class CFrom {

        private String prefix;

        private String value;

        private String alias;

        private QFrom.Type type;

        public String getPrefix() {
            return prefix;
        }

        public CFrom setPrefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public String getValue() {
            return value;
        }

        public CFrom setValue(String value) {
            this.value = value;
            return this;
        }

        public String getAlias() {
            return alias;
        }

        public CFrom setAlias(String alias) {
            this.alias = alias;
            return this;
        }

        public QFrom.Type getType() {
            return type;
        }

        public CFrom setType(QFrom.Type type) {
            this.type = type;
            return this;
        }
    }

    public static class CField {

        private String prefix;

        private String value;

        private String alias;

        public String getPrefix() {
            return prefix;
        }

        public CField setPrefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public String getValue() {
            return value;
        }

        public CField setValue(String value) {
            this.value = value;
            return this;
        }

        public String getAlias() {
            return alias;
        }

        public CField setAlias(String alias) {
            this.alias = alias;
            return this;
        }
    }

    public static class COrderField {

        private String prefix;

        private String value;

        private QOrderField.Type type;

        public String getPrefix() {
            return prefix;
        }

        public COrderField setPrefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public String getValue() {
            return value;
        }

        public COrderField setValue(String value) {
            this.value = value;
            return this;
        }

        public QOrderField.Type getType() {
            return type;
        }

        public COrderField setType(QOrderField.Type type) {
            this.type = type;
            return this;
        }
    }

    public static class CJoin {

        private CFrom from;

        private Join.Type type;

        private List<COn> on;

        public CFrom getFrom() {
            return from;
        }

        public CJoin setFrom(CFrom from) {
            this.from = from;
            return this;
        }

        public Join.Type getType() {
            return type;
        }

        public CJoin setType(Join.Type type) {
            this.type = type;
            return this;
        }

        public List<COn> getOn() {
            return on;
        }

        public CJoin setOn(List<COn> on) {
            this.on = on;
            return this;
        }
    }

    public static class COn {

        private CField field;

        private CField with;

        private String opt;

        private Cond.LogicalOpt logicalOpt;

        private boolean ignorable;

        public CField getField() {
            return field;
        }

        public COn setField(CField field) {
            this.field = field;
            return this;
        }

        public CField getWith() {
            return with;
        }

        public COn setWith(CField with) {
            this.with = with;
            return this;
        }

        public String getOpt() {
            return opt;
        }

        public COn setOpt(String opt) {
            this.opt = opt;
            return this;
        }

        public Cond.LogicalOpt getLogicalOpt() {
            return logicalOpt;
        }

        public COn setLogicalOpt(Cond.LogicalOpt logicalOpt) {
            this.logicalOpt = logicalOpt;
            return this;
        }

        public boolean isIgnorable() {
            return ignorable;
        }

        public COn setIgnorable(boolean ignorable) {
            this.ignorable = ignorable;
            return this;
        }
    }
}