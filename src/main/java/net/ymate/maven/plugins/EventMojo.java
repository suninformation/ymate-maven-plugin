/*
 * Copyright 2007-2023 the original author or authors.
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

import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 事件类代码生成器
 *
 * @author 刘镇 (suninformation@163.com) on 2023/12/22 03:29
 * @since 1.0.3
 */
@Mojo(name = "event")
public class EventMojo extends AbstractMojo {

    /**
     * 事件类名称
     */
    @Parameter(property = "name", required = true)
    private String name;

    /**
     * 事件名称集合
     */
    @Parameter(property = "events")
    private String[] events;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        name = StringUtils.capitalize(name);
        Set<String> eventList = new LinkedHashSet<>();
        if (ArrayUtils.isNotEmpty(events)) {
            for (String event : events) {
                if (StringUtils.isNotBlank(event)) {
                    eventList.add(ClassUtils.fieldNameToPropertyName(event, 2));
                }
            }
        }
        if (eventList.isEmpty()) {
            eventList.add(ClassUtils.fieldNameToPropertyName(name, 2));
        }
        //
        Map<String, Object> properties = new HashMap<>(16);
        properties.put("eventName", name);
        properties.put("eventList", eventList);
        properties.put("packageName", getPackageName());
        properties.put("moduleArtifactId", getProjectName());
        //
        getLog().info("properties:");
        getLog().info("\t|--eventName:" + name);
        getLog().info("\t|--eventList:" + eventList);
        getLog().info("\t|--packageName:" + getPackageName());
        getLog().info("\t|--moduleArtifactId:" + getProjectName());
        //
        try {
            File path = new File(String.format("%s/src/main/java", getBasedir()), getPackageName().replace(".", "/"));
            //
            doWriterTemplateFile(new File(path, String.format("%sEvent.java", name)), "/event/event-class", properties);
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), RuntimeUtils.unwrapThrow(e));
        }
    }
}
