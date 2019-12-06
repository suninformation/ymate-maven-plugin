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

import net.ymate.platform.commons.util.RuntimeUtils;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 15/10/26 下午2:56
 */
@Mojo(name = "tomcat")
public class TomcatMojo extends AbstractMojo {

    /**
     * 需要创建的目录列表
     */
    private static String[] NEED_MK_DIRS = {
            "bin",
            "conf",
            "logs",
            "temp",
            "webapps",
            "webapps/ROOT",
            "work"
    };

    /**
     * 需要复制的配置文件列表
     */
    private static String[] NEED_COPY_FILES = {
            "conf/catalina.policy",
            "conf/catalina.properties",
            "conf/logging.properties",
            "conf/context.xml",
            "conf/tomcat-users.xml",
            "conf/web.xml"
    };

    /**
     * Tomcat8/9需要复制的文件列表
     */
    private static String[] V8_COPY_FILES = {
            "conf/jaspic-providers.xml",
            "conf/jaspic-providers.xsd",
            "conf/tomcat-users.xsd"
    };

    @Parameter(property = "serviceName", required = true)
    private String serviceName;

    @Parameter(property = "catalinaHome", required = true, defaultValue = "${env.CATALINA_HOME}")
    private String catalinaHome;

    @Parameter(property = "catalinaBase", defaultValue = "${basedir}")
    private String catalinaBase;

    @Parameter(property = "hostName", defaultValue = "localhost")
    private String hostName;

    @Parameter(property = "hostAlias")
    private String hostAlias;

    @Parameter(property = "tomcatVersion", defaultValue = "7")
    private int tomcatVersion;

    @Parameter(property = "serverPort", defaultValue = "8005")
    private int serverPort;

    @Parameter(property = "connectorPort", defaultValue = "8080")
    private int connectorPort;

    @Parameter(property = "redirectPort", defaultValue = "8443")
    private int redirectPort;

    @Parameter(property = "ajp")
    private boolean ajp;

    @Parameter(property = "ajpHost", defaultValue = "localhost")
    private String ajpHost;

    @Parameter(property = "ajpPort", defaultValue = "8009")
    private int ajpPort;

    private void doCheckFiles() throws Exception {
        File file = new File(catalinaHome);
        if (!file.exists() || !file.isDirectory()) {
            throw new IllegalArgumentException("'catalinaHome' invalid, not exist or not a directory");
        }
        file = new File(catalinaBase);
        if (!file.exists() || !file.isDirectory()) {
            throw new IllegalArgumentException("'catalinaBase' invalid, not exist or not a directory");
        }
        for (String fileName : NEED_COPY_FILES) {
            File tmpFile = new File(this.catalinaHome, fileName);
            if (!tmpFile.exists() || !tmpFile.isFile()) {
                throw new FileNotFoundException(fileName);
            }
        }
    }

    private void doMakeDirs(File parent) {
        if (parent.mkdir()) {
            for (String dirName : NEED_MK_DIRS) {
                new File(parent, dirName).mkdir();
            }
        }
    }

    private void doCopyConfFiles(File parent) throws Exception {
        for (String fileName : NEED_COPY_FILES) {
            FileUtils.copyFile(new File(catalinaHome, fileName), new File(parent, fileName));
        }
        if (tomcatVersion == 8 || tomcatVersion == 9) {
            for (String fileName : V8_COPY_FILES) {
                File targetFile = new File(catalinaHome, fileName);
                if (targetFile.exists() && targetFile.isFile()) {
                    FileUtils.copyFile(targetFile, new File(parent, fileName));
                }
            }
        }
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            doCheckFiles();
            //
            File parent = new File(catalinaBase, serviceName);
            if (parent.exists() && !isOverwrite()) {
                throw new FileExistsException(parent);
            }
            doMakeDirs(parent);
            //
            Map<String, Object> props = new HashMap<>(16);
            props.put("catalina_home", catalinaHome);
            props.put("catalina_base", parent.getPath());
            if (tomcatVersion <= 0) {
                tomcatVersion = 7;
            } else if (tomcatVersion < 6 || tomcatVersion > 9) {
                throw new IllegalArgumentException("'tomcatVersion' invalid, only supports 6, 7, 8, 9");
            }
            hostName = StringUtils.trimToEmpty(hostName);
            hostAlias = StringUtils.trimToEmpty(hostAlias);
            if (serverPort <= 0) {
                throw new IllegalArgumentException("'serverPort' invalid, must be gt 0");
            }
            if (connectorPort <= 0) {
                throw new IllegalArgumentException("'connectorPort' invalid, must be gt 0");
            }
            if (redirectPort <= 0) {
                throw new IllegalArgumentException("'redirectPort' invalid, must be gt 0");
            }
            ajpHost = StringUtils.defaultIfBlank(ajpHost, "localhost");
            if (ajpPort <= 0) {
                throw new IllegalArgumentException("'ajpPort' invalid, must be gt 0");
            }
            //
            props.put("tomcat_version", Integer.toString(tomcatVersion));
            props.put("host_name", hostName);
            props.put("host_alias", hostAlias);
            props.put("website_root_path", new File(parent, "webapps/ROOT").getPath());
            props.put("service_name", serviceName);
            props.put("server_port", Integer.toString(serverPort));
            props.put("connector_port", Integer.toString(connectorPort));
            props.put("redirect_port", Integer.toString(redirectPort));
            props.put("ajp", ajp);
            props.put("ajp_host", ajpHost);
            props.put("ajp_port", Integer.toString(ajpPort));
            //
            getLog().info("Tomcat Service:" + serviceName);
            getLog().info("\t|--CatalinaHome:" + catalinaHome);
            getLog().info("\t|--CatalinaBase:" + catalinaBase);
            getLog().info("\t|--HostName:" + hostName);
            getLog().info("\t|--HostAlias:" + hostAlias);
            getLog().info("\t|--TomcatVersion:" + tomcatVersion);
            getLog().info("\t|--ServerPort:" + serverPort);
            getLog().info("\t|--ConnectorPort:" + connectorPort);
            getLog().info("\t|--RedirectPort:" + redirectPort);
            getLog().info("\t|--Ajp:" + ajp);
            if (ajp) {
                getLog().info("\t|--AjpHost:" + ajpHost);
                getLog().info("\t|--AjpPort:" + ajpPort);
            }
            //
            doCopyConfFiles(parent);
            //
            doWriterTemplateFile(parent.getPath(), "conf/server.xml", "/tomcat/v" + tomcatVersion + "/server-xml.ftl", props);
            doWriterTemplateFile(parent.getPath(), "vhost.conf", "/tomcat/vhost-conf.ftl", props);
            doWriterTemplateFile(parent.getPath(), "bin/install.bat", "/tomcat/install-cmd.ftl", props);
            doWriterTemplateFile(parent.getPath(), "bin/manager.bat", "/tomcat/manager-cmd.ftl", props);
            doWriterTemplateFile(parent.getPath(), "bin/shutdown.bat", "/tomcat/shutdown-cmd.ftl", props);
            doWriterTemplateFile(parent.getPath(), "bin/startup.bat", "/tomcat/startup-cmd.ftl", props);
            doWriterTemplateFile(parent.getPath(), "bin/uninstall.bat", "/tomcat/uninstall-cmd.ftl", props);
            doWriterTemplateFile(parent.getPath(), "bin/manager.sh", "/tomcat/manager-sh.ftl", props);
            doWriterTemplateFile(parent.getPath(), "webapps/ROOT/index.jsp", "/tomcat/index-jsp.ftl", props);
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), RuntimeUtils.unwrapThrow(e));
        }
    }
}
