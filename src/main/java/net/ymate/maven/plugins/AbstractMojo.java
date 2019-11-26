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

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import net.ymate.platform.commons.FreemarkerConfigBuilder;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.configuration.IConfigReader;
import net.ymate.platform.core.configuration.impl.MapSafeConfigReader;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author 刘镇 (suninformation@163.com) on 15/10/26 上午12:24
 */
public abstract class AbstractMojo extends org.apache.maven.plugin.AbstractMojo {

    private String templateRootPath;

    private Configuration freemarkerConfig;

    /**
     * 当前项目基准路径
     */
    @Parameter(defaultValue = "${basedir}")
    private String basedir;

    @Parameter(defaultValue = "${project.groupId}")
    private String packageName;

    @Parameter(defaultValue = "${project.artifactId}")
    private String projectName;

    @Parameter(defaultValue = "${project.version}")
    private String version;

    public AbstractMojo() {
        templateRootPath = AbstractMojo.class.getPackage().getName().replace(".", "/");
        try {
            freemarkerConfig = FreemarkerConfigBuilder.create().addTemplateClass(AbstractMojo.class, "/")
                    .setEncoding("UTF-8")
                    .setTemplateExceptionHandler(TemplateExceptionHandler.DEBUG_HANDLER).build();
        } catch (IOException e) {
            getLog().error(RuntimeUtils.unwrapThrow(e));
        }
    }

    public IConfigReader getDefaultConfigFileAsReader() {
        return getConfigFileAsReader(new File(basedir, "/src/main/resources/ymp-conf.properties").getPath());
    }

    public IConfigReader getConfigFileAsReader(String confFilePath) {
        Properties properties = new Properties();
        confFilePath = RuntimeUtils.replaceEnvVariable(confFilePath);
        File confFile = new File(confFilePath);
        if (confFile.isAbsolute() && confFile.exists() && confFile.isFile()) {
            try (InputStream inputStream = new FileInputStream(confFile)) {
                properties.load(inputStream);
                getLog().info(String.format("Found and load the config file: %s", confFile.getPath()));
                return MapSafeConfigReader.bind(properties);
            } catch (Exception e) {
                getLog().error(RuntimeUtils.unwrapThrow(e));
            }
        }
        return null;
    }

    /**
     * 是否覆盖已存在的文件
     */
    @Parameter(property = "overwrite")
    private boolean overwrite;

    public String getTemplateRootPath() {
        return templateRootPath;
    }

    public Configuration getFreemarkerConfig() {
        return freemarkerConfig;
    }

    public String getBasedir() {
        return basedir;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getVersion() {
        return version;
    }

    public boolean isOverwrite() {
        return overwrite;
    }
}
