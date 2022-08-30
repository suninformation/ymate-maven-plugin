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

import net.ymate.platform.commons.IPasswordProcessor;
import net.ymate.platform.commons.util.RuntimeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.net.URLClassLoader;

/**
 * @author 刘镇 (suninformation@163.com) on 2016/12/17 05:52
 */
@Mojo(name = "encrypt", requiresDependencyResolution = ResolutionScope.RUNTIME, requiresDependencyCollection = ResolutionScope.RUNTIME)
//@Execute(phase = LifecyclePhase.COMPILE)
public class EncryptMojo extends AbstractMojo {

    @Parameter(required = true, readonly = true, defaultValue = "${project}")
    private MavenProject mavenProject;

    @Parameter(property = "content", required = true)
    private String content;

    @Parameter(property = "passkey")
    private String passkey;

    @Parameter(property = "implClass")
    private String implClass;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try (URLClassLoader classloader = buildRuntimeClassLoader(mavenProject)) {
            IPasswordProcessor processor = loadPasswordProcessor(implClass, classloader);
            if (StringUtils.isNotBlank(passkey)) {
                processor.setPassKey(passkey);
            }
            getLog().info(String.format("Use passkey: %s", processor.getPassKey()));
            getLog().info(String.format("Encrypt content: %s", processor.encrypt(content)));
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), RuntimeUtils.unwrapThrow(e));
        }
    }
}
