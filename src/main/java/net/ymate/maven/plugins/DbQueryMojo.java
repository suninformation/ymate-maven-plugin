/*
 * Copyright 2007-2019 the original author or authors.
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

import net.ymate.platform.commons.ConsoleTableBuilder;
import net.ymate.platform.commons.lang.BlurObject;
import net.ymate.platform.commons.util.DateTimeUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.Application;
import net.ymate.platform.core.ApplicationConfigureBuilder;
import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.IApplicationConfigurer;
import net.ymate.platform.core.configuration.IConfigReader;
import net.ymate.platform.core.impl.DefaultApplicationConfigureParser;
import net.ymate.platform.core.persistence.IPersistenceConfig;
import net.ymate.platform.core.persistence.IResultSet;
import net.ymate.platform.core.persistence.Page;
import net.ymate.platform.persistence.jdbc.IDatabaseConfig;
import net.ymate.platform.persistence.jdbc.JDBC;
import net.ymate.platform.persistence.jdbc.base.IResultSetHandler;
import net.ymate.platform.persistence.jdbc.impl.DefaultDatabaseConfigurable;
import net.ymate.platform.persistence.jdbc.impl.DefaultDatabaseDataSourceConfigurable;
import net.ymate.platform.persistence.jdbc.query.SQL;
import net.ymate.platform.persistence.jdbc.support.ResultSetHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/3/27 下午6:25
 */
@Mojo(name = "dbquery")
public class DbQueryMojo extends AbstractMojo {

    private static final String CONFIG_KEY_PREFIX = "ymp.configs.persistence.jdbc.ds.%s.%s";

    private static final String SQL_TYPE_SELECT = "select";

    @Parameter(property = "sql", required = true)
    private String sql;

    @Parameter(property = "format", defaultValue = "table")
    private String format;

    @Parameter(property = "page", defaultValue = "0")
    private int page;

    @Parameter(property = "pageSize", defaultValue = "0")
    private int pageSize;

    @Parameter(property = "cfg")
    private String cfgFile;

    @Parameter(property = "dataSource", defaultValue = IPersistenceConfig.DEFAULT_STR)
    private String dataSource;

    @Parameter(property = "dateColumns")
    private String[] dateColumns;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        sql = StringUtils.trimToEmpty(StringUtils.replaceChars(sql, "\r\n\t", StringUtils.SPACE));
        if (!StringUtils.startsWithIgnoreCase(sql, SQL_TYPE_SELECT)) {
            throw new MojoExecutionException("Invalid SQL parameter value, only select query statement is supported!");
        }
        IConfigReader configReader = cfgFile == null ? getDefaultConfigFileAsReader() : getConfigFileAsReader(cfgFile);
        if (configReader == null) {
            throw new MojoExecutionException(String.format("Configuration file '%s' does not exist!", RuntimeUtils.replaceEnvVariable(cfgFile)));
        }
        String connectionUrlKey = String.format(CONFIG_KEY_PREFIX, dataSource, IDatabaseConfig.CONNECTION_URL);
        String connectionUrl = configReader.getString(connectionUrlKey);
        if (StringUtils.isBlank(connectionUrl)) {
            throw new MojoExecutionException(String.format("'%s' parameter is not set in the configuration file!", connectionUrlKey));
        }
        IApplicationConfigurer configurer = ApplicationConfigureBuilder.builder(DefaultApplicationConfigureParser.defaultEmpty()).runEnv(IApplication.Environment.DEV)
                .addModuleConfigurers(DefaultDatabaseConfigurable.builder()
                        .addDataSources(DefaultDatabaseDataSourceConfigurable.builder(IPersistenceConfig.DEFAULT_STR)
                                .connectionUrl(connectionUrl)
                                .username(configReader.getString(String.format(CONFIG_KEY_PREFIX, dataSource, IDatabaseConfig.USERNAME)))
                                .password(configReader.getString(String.format(CONFIG_KEY_PREFIX, dataSource, IDatabaseConfig.PASSWORD)))
                                .passwordEncrypted(configReader.getBoolean(String.format(CONFIG_KEY_PREFIX, dataSource, IDatabaseConfig.PASSWORD_ENCRYPTED)))
                                .showSql(true).build()).build()).build();
        try (IApplication application = new Application(configurer)) {
            application.initialize();
            //
            List<String> columns = dateColumns != null ? Arrays.asList(dateColumns) : Collections.emptyList();
            ResultSetHelper.ColumnRender columnRender = columns.isEmpty() ? null : (columnName, value) -> columns.contains(columnName) ? DateTimeUtils.formatTime(BlurObject.bind(value).toLongValue(), DateTimeUtils.YYYY_MM_DD_HH_MM_SS) : value;
            //
            IResultSet<Object[]> resultSet = SQL.create(application.getModuleManager().getModule(JDBC.class), sql).find(IResultSetHandler.ARRAY, Page.createIfNeed(pageSize > 0 && page <= 0 ? 1 : page, page > 0 && pageSize <= 0 ? Page.DEFAULT_PAGE_SIZE : pageSize));
            if (resultSet.isResultsAvailable()) {
                ResultSetHelper resultSetHelper = ResultSetHelper.bind(resultSet);
                switch (StringUtils.lowerCase(format)) {
                    case ConsoleTableBuilder.TYPE_CSV:
                        System.out.println(resultSetHelper.toCsv(columnRender));
                        break;
                    case ConsoleTableBuilder.TYPE_MARKDOWN:
                        System.out.println(resultSetHelper.toMarkdown(columnRender));
                        break;
                    default:
                        System.out.println(resultSetHelper.toString(columnRender));
                }
            }
            getLog().info("------------------------------------------------------------------------");
            if (resultSet.isPaginated()) {
                getLog().info(String.format("Number of records: %d", resultSet.getRecordCount()));
                getLog().info(String.format("Page No.: %d", resultSet.getPageNumber()));
                getLog().info(String.format("Total pages: %d", resultSet.getPageCount()));
                getLog().info(String.format("Records per page: %d", resultSet.getPageSize()));
            } else {
                getLog().info(String.format("Number of records: %d", resultSet.getResultData().size()));
            }
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), RuntimeUtils.unwrapThrow(e));
        }
    }
}
