/*
 * Copyright 2007-2021 the original author or authors.
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

import net.ymate.apidocs.Docs;
import net.ymate.apidocs.IDocs;
import net.ymate.apidocs.annotation.Api;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.beans.IBeanLoader;
import net.ymate.platform.core.beans.impl.DefaultBeanLoader;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @author ει (suninformation@163.com) on 2021/11/14 2:05 δΈε
 * @since 1.1
 */
@Mojo(name = "apidocs", requiresDependencyResolution = ResolutionScope.RUNTIME, requiresDependencyCollection = ResolutionScope.RUNTIME)
@Execute(phase = LifecyclePhase.COMPILE)
public class ApiDocsMojo extends AbstractMojo {

    @Parameter(required = true, readonly = true, defaultValue = "${project}")
    private MavenProject mavenProject;

    @Parameter(property = "packageNames", defaultValue = "${project.groupId}")
    private String[] packageNames;

    /**
     * θΎεΊζ ΌεΌ: html|gitbook|postman|json|markdown, ι»θ?€εΌ: markdown
     */
    @Parameter(property = "format", defaultValue = "markdown")
    private String format;

    @Parameter(property = "outputDir", defaultValue = "${basedir}")
    private String outputDir;

    /**
     * θͺε?δΉθ―­θ¨
     */
    @Parameter(property = "language")
    private String language;

    /**
     * ζ―ε¦θ¦ηε·²ε­ε¨ηζδ»Ά
     */
    @Parameter(property = "overwrite")
    private boolean overwrite;

    /**
     * εΏ½η₯ηθ―·ζ±ζΉζ³εη§°ιε
     */
    @Parameter(property = "ignoredRequestMethods")
    private String[] ignoredRequestMethods;

    @Override
    @SuppressWarnings("unchecked")
    public void execute() throws MojoExecutionException, MojoFailureException {
        try (IApplication application = YMP.run()) {
            if (StringUtils.isNotBlank(language)) {
                application.getI18n().current(LocaleUtils.toLocale(language));
            }
            List<URL> urls = new ArrayList<>();
            urls.add(new File(mavenProject.getBuild().getOutputDirectory()).toURI().toURL());
            for (Artifact dependency : mavenProject.getArtifacts()) {
                urls.add(dependency.getFile().toURI().toURL());
            }
            IBeanLoader beanLoader = new DefaultBeanLoader();
            beanLoader.setClassLoader(new URLClassLoader(urls.toArray(new URL[0]), this.getClass().getClassLoader()));
            beanLoader.registerPackageNames(Arrays.asList(packageNames));
            //
            getLog().info(String.format("packageNames: %s", beanLoader.getPackageNames()));
            getLog().info(String.format("outputDir: %s", outputDir));
            //
            IDocs docs = application.getModuleManager().getModule(Docs.class);
            if (!ArrayUtils.isEmpty(ignoredRequestMethods)) {
                Set<String> ignoredRequestMethodSet = docs.getConfig().getIgnoredRequestMethods();
                Arrays.stream(ignoredRequestMethods).filter(StringUtils::isNotBlank).forEach(methodName -> ignoredRequestMethodSet.add(methodName.toUpperCase()));
            }
            for (Class<?> clazz : beanLoader.load()) {
                if (clazz.isAnnotationPresent(Api.class)) {
                    getLog().info(String.format("Scanned to: %s", clazz.getName()));
                    docs.registerApi((Class<? extends Api>) clazz);
                }
            }
            if (docs.getDocInfoMap().isEmpty()) {
                getLog().warn("No documents found.");
            } else {
                File distDir = new File(outputDir, "docs/");
                switch (StringUtils.lowerCase(format)) {
                    case "html":
                    case "docusaurus":
                        docs.writeToDocusaurus(new File(distDir, "docusaurus"), overwrite);
                        break;
                    case "gitbook":
                        docs.writeToGitbook(new File(distDir, "gitbook"), overwrite);
                        break;
                    case "postman":
                        docs.writeToPostman(new File(distDir, "postman"), overwrite);
                        break;
                    case "json":
                        docs.writeToJson(distDir, overwrite);
                        break;
                    case "markdown":
                        docs.writeToMarkdown(distDir, overwrite);
                        break;
                    default:
                        getLog().warn(String.format("Output in %s format is not supported.", format));
                }
            }
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), RuntimeUtils.unwrapThrow(e));
        }
    }
}
