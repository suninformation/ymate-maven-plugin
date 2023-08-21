<#setting number_format="#">
<?xml version="1.0" encoding="UTF-8" ?>
<!--
	${app.name?capitalize} Tag Library 1.0
-->
<taglib xmlns="http://java.sun.com/xml/ns/j2ee" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://java.sun.com/xml/ns/j2ee http://java.sun.com/xml/ns/j2ee/web-jsptaglibrary_2_0.xsd"
        version="2.0">

    <description>${app.name?capitalize} Tag Library 1.0</description>
    <display-name>${app.name}-taglib</display-name>
    <tlib-version>1.0</tlib-version>
    <short-name>${app.name}</short-name>
    <uri>http://www.ymate.net/${app.name}</uri>

    <#list tags as p>
        ${p}
    </#list>

</taglib>