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
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 验证器类生成器
 *
 * @author 刘镇 (suninformation@163.com) on 2015/10/26 16:37
 */
@Mojo(name = "validator")
public class ValidatorMojo extends AbstractMojo {

    private static final String NAME_SUFFIX = "Validator";

    private static final String PACKAGE_SUFFIX = ".validate";

    /**
     * 验证器名称
     */
    @Parameter(property = "name", required = true)
    private String name;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Map<String, Object> properties = new HashMap<>(15);
        //
        name = StringUtils.capitalize(StringUtils.substringBefore(name, NAME_SUFFIX));
        String packageName = getPackageName();
        if (!StringUtils.endsWithIgnoreCase(packageName, PACKAGE_SUFFIX)) {
            packageName = packageName + PACKAGE_SUFFIX;
        }
        //
        properties.put("validatorName", name);
        properties.put("packageName", packageName);
        //
        getLog().info("properties:");
        getLog().info("\t|--validatorName:" + name);
        getLog().info("\t|--packageName:" + packageName);
        //
        try {
            File path = new File(String.format("%s/src/main/java", getBasedir()), packageName.replace(".", "/"));
            //
            doWriterTemplateFile(new File(path, String.format("V%s.java", name)), "/validator/validator-annotation", properties);
            doWriterTemplateFile(new File(path, String.format("%s%s.java", name, NAME_SUFFIX)), "/validator/validator", properties);
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), RuntimeUtils.unwrapThrow(e));
        }
    }
}
