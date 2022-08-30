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
import net.ymate.platform.commons.util.RuntimeUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

/**
 * 配置体系目录结构生成器
 *
 * @author 刘镇 (suninformation@163.com) on 2018/05/18 10:23
 */
@Mojo(name = "configuration")
public class ConfigurationMojo extends AbstractMojo {

    /**
     * 配置体系目录结构
     */
    private static final String[] HOME_BASE_DIRS = new String[]{
            "cfgs",
            "classes",
            "lib",
            "logs",
            "temp"
    };

    private static final String[] HOME_EXTEND_DIRS = new String[]{
            "bin",
            "dist",
            "projects"
    };

    /**
     * 配置体系根路径，默认为当前项目基准路径，若指定路径不存在则创建之
     */
    @Parameter(property = "homeDir", defaultValue = "${basedir}")
    private String homeDir;

    /**
     * 项目名称
     */
    @Parameter(property = "projectName")
    private String projectName;

    /**
     * 模块名称集合
     */
    @Parameter(property = "moduleNames")
    private String[] moduleNames;

    /**
     * 插件名称集合
     */
    @Parameter(property = "pluginNames")
    private String[] pluginNames;

    /**
     * 是否执行缺失文件修复(除目录结构自动补全外，该参数将对缺失的文件进行补全)
     */
    @Parameter(property = "repair")
    private boolean repair;

    private void doMakeDirs(File parent, boolean home) {
        if (home) {
            for (String dirName : HOME_EXTEND_DIRS) {
                File targetDir = new File(parent, dirName);
                if (targetDir.mkdirs()) {
                    getLog().info(String.format("Create directory: %s", targetDir.getPath()));
                }
            }
        }
        for (String dirName : HOME_BASE_DIRS) {
            File targetDir = new File(parent, dirName);
            if (targetDir.mkdirs()) {
                getLog().info(String.format("Create directory: %s", targetDir.getPath()));
            }
        }
    }

    private void doRepairLog4jFile(File baseDir) throws IOException, TemplateException {
        if (repair) {
            doWriterTemplateFile(new File(baseDir, "cfgs/log4j.xml"), "configuration/log4j.xml", Collections.emptyMap());
        }
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            File baseDir = new File(homeDir);
            if (!baseDir.isAbsolute()) {
                baseDir = new File(getBasedir(), homeDir);
            }
            getLog().info(String.format("Base directory: %s", baseDir.getPath()));
            doMakeDirs(baseDir, true);
            doRepairLog4jFile(baseDir);
            //
            if (StringUtils.isNotBlank(projectName)) {
                File projectDir = new File(new File(baseDir, "projects"), projectName);
                doMakeDirs(projectDir, false);
                doRepairLog4jFile(projectDir);
                //
                if (!ArrayUtils.isEmpty(moduleNames)) {
                    for (String mName : moduleNames) {
                        File moduleDir = new File(new File(projectDir, "modules"), mName);
                        doMakeDirs(moduleDir, false);
                        doRepairLog4jFile(moduleDir);
                        //
                        if (!ArrayUtils.isEmpty(pluginNames)) {
                            for (String pName : pluginNames) {
                                File pluginDir = new File(new File(moduleDir, "plugins"), pName);
                                doMakeDirs(pluginDir, false);
                            }
                        }
                    }
                } else if (!ArrayUtils.isEmpty(pluginNames)) {
                    for (String pName : pluginNames) {
                        File pluginDir = new File(new File(projectDir, "plugins"), pName);
                        doMakeDirs(pluginDir, false);
                    }
                }
            } else if (!ArrayUtils.isEmpty(pluginNames)) {
                for (String pName : pluginNames) {
                    File pluginDir = new File(new File(baseDir, "plugins"), pName);
                    doMakeDirs(pluginDir, false);
                }
            }
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), RuntimeUtils.unwrapThrow(e));
        }
    }
}
