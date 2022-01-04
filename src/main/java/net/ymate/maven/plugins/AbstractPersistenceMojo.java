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

import net.ymate.platform.core.*;
import net.ymate.platform.core.configuration.IConfigReader;
import net.ymate.platform.core.impl.DefaultApplicationConfigureParser;
import net.ymate.platform.core.persistence.IPersistenceConfig;
import net.ymate.platform.persistence.jdbc.IDatabaseConfig;
import net.ymate.platform.persistence.jdbc.JDBC;
import net.ymate.platform.persistence.jdbc.impl.DefaultDatabaseConfigurable;
import net.ymate.platform.persistence.jdbc.impl.DefaultDatabaseDataSourceConfigurable;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-12-24 13:28
 */
public abstract class AbstractPersistenceMojo extends AbstractMojo {

    private static final String CONFIG_KEY_PREFIX = "ymp.configs.persistence.jdbc.ds.%s.%s";

    @Parameter(property = "dataSource", defaultValue = IPersistenceConfig.DEFAULT_STR)
    private String dataSource;

    /**
     * 输出格式: table|markdown|csv, 默认值: table
     */
    @Parameter(property = "format", defaultValue = "table")
    private String format;

    public IApplicationConfigureFactory buildApplicationConfigureFactory() throws MojoExecutionException {
        IConfigReader configReader = loadConfigFile();
        //
        String connectionUrlKey = String.format(CONFIG_KEY_PREFIX, dataSource, IDatabaseConfig.CONNECTION_URL);
        String connectionUrl = configReader.getString(connectionUrlKey);
        if (StringUtils.isBlank(connectionUrl)) {
            throw new MojoExecutionException(String.format("'%s' parameter is not set in the configuration file!", connectionUrlKey));
        }
        IApplicationConfigurer configurer = ApplicationConfigureBuilder.builder(DefaultApplicationConfigureParser.defaultEmpty()).runEnv(IApplication.Environment.DEV)
                .includedModules(JDBC.class.getName())
                .addModuleConfigurers(DefaultDatabaseConfigurable.builder()
                        .addDataSources(DefaultDatabaseDataSourceConfigurable.builder(IPersistenceConfig.DEFAULT_STR)
                                .connectionUrl(connectionUrl)
                                .username(configReader.getString(String.format(CONFIG_KEY_PREFIX, dataSource, IDatabaseConfig.USERNAME)))
                                .password(configReader.getString(String.format(CONFIG_KEY_PREFIX, dataSource, IDatabaseConfig.PASSWORD)))
                                .passwordEncrypted(configReader.getBoolean(String.format(CONFIG_KEY_PREFIX, dataSource, IDatabaseConfig.PASSWORD_ENCRYPTED)))
                                .showSql(true).build()).build())
                .addParameters(configReader.getMap("ymp.params.")).build();
        return new AbstractApplicationConfigureFactory() {

            @Override
            public IApplicationConfigurer getConfigurer() {
                return configurer;
            }
        };
    }

    public String getDataSource() {
        return dataSource;
    }

    public String getFormat() {
        return format;
    }
}
