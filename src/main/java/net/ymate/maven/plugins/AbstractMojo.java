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
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import net.ymate.platform.commons.FreemarkerConfigBuilder;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.configuration.IConfigReader;
import net.ymate.platform.core.configuration.impl.MapSafeConfigReader;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    @Parameter(property = "packageName", defaultValue = "${project.groupId}")
    private String packageName;

    @Parameter(defaultValue = "${project.artifactId}")
    private String projectName;

    @Parameter(defaultValue = "${project.version}")
    private String version;

    @Parameter(property = "cfgFile")
    private String cfgFile;

    /**
     * 是否覆盖已存在的文件
     */
    @Parameter(property = "overwrite")
    private boolean overwrite;

    public AbstractMojo() {
        templateRootPath = AbstractMojo.class.getPackage().getName().replace(".", "/");
        try {
            freemarkerConfig = FreemarkerConfigBuilder.create().addTemplateClass(AbstractMojo.class, "/")
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

    public IConfigReader loadConfigFile() throws MojoExecutionException {
        IConfigReader configReader = getCfgFile() == null ? getDefaultConfigFileAsReader() : getConfigFileAsReader(getCfgFile());
        if (configReader == null) {
            throw new MojoExecutionException(String.format("Configuration file '%s' does not exist!", RuntimeUtils.replaceEnvVariable(getCfgFile())));
        }
        return configReader;
    }

    public void doWriterTemplateFile(String path, String fileName, String tmplFile, Map<String, Object> properties) throws IOException, TemplateException {
        File outputFile = new File(path, fileName);
        doWriterTemplateFile(new FileOutputStream(outputFile), tmplFile, properties);
        this.getLog().info("Output file: " + outputFile);
    }

    public void doWriterTemplateFile(OutputStream output, String tmplFile, Map<String, Object> properties) throws IOException, TemplateException {
        if (tmplFile.charAt(0) != '/') {
            tmplFile = String.format("/%s", tmplFile);
        }
        if (!tmplFile.startsWith("/tmpl")) {
            tmplFile = String.format("/tmpl%s", tmplFile);
        }
        if (!tmplFile.endsWith(".ftl")) {
            tmplFile = String.format("%s.ftl", tmplFile);
        }
        try (Writer writer = new OutputStreamWriter(output, getFreemarkerConfig().getOutputEncoding())) {
            getFreemarkerConfig().getTemplate(getTemplateRootPath() + tmplFile).process(properties, new BufferedWriter(writer));
        }
    }

    public void doWriterTemplateFile(File targetFile, String tmplFile, Map<String, Object> properties) throws IOException, TemplateException {
        boolean notSkipped = !targetFile.exists() || targetFile.exists() && isOverwrite();
        if (notSkipped) {
            File parentFile = targetFile.getParentFile();
            if (parentFile.exists() || parentFile.mkdirs()) {
                doWriterTemplateFile(parentFile.getPath(), targetFile.getName(), tmplFile, properties);
            }
        } else {
            getLog().warn("Skip existing file " + targetFile);
        }
    }

    public URLClassLoader buildRuntimeClassLoader(MavenProject mavenProject) throws MalformedURLException {
        List<URL> urls = new ArrayList<>();
        urls.add(new File(mavenProject.getBuild().getOutputDirectory()).toURI().toURL());
        for (Artifact dependency : mavenProject.getArtifacts()) {
            urls.add(dependency.getFile().toURI().toURL());
        }
        return new URLClassLoader(urls.toArray(new URL[0]), this.getClass().getClassLoader());
    }

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

    public String getCfgFile() {
        return cfgFile;
    }

    public boolean isOverwrite() {
        return overwrite;
    }
}
