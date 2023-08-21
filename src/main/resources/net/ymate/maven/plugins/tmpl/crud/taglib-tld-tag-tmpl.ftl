<#setting number_format="#">
<#macro buildField p>
    <#if p.config?? && p.config.query?? && p.config.query.enabled>
        <#if p.config.query.validation?? && p.config.query.validation.dateTime?? && p.config.query.validation.dateTime.enabled>
            <attribute>
                <name>${p.name}Str</name>
                <rtexprvalue>true</rtexprvalue>
                <type>java.lang.String</type>
            </attribute>
        <#else>
            <attribute>
                <name>${p.name}</name>
                <rtexprvalue>true</rtexprvalue>
                <type>${p.type}</type>
            </attribute>
        </#if>
    </#if>
</#macro>

<!-- ${prefix}${api.name?cap_first}Tag -->
<tag>
    <name>${api.name?uncap_first}</name>
    <tag-class>${app.packageName}.taglib.${prefix}${api.name?cap_first}Tag</tag-class>
    <body-content>JSP</body-content>
    <attribute>
        <name>var</name>
        <rtexprvalue>true</rtexprvalue>
        <type>java.lang.String</type>
    </attribute>
    <attribute>
        <name>scope</name>
        <rtexprvalue>true</rtexprvalue>
        <type>java.lang.String</type>
    </attribute>
    <attribute>
        <name>innerLoop</name>
        <rtexprvalue>true</rtexprvalue>
        <type>boolean</type>
    </attribute>
    <attribute>
        <name>always</name>
        <rtexprvalue>true</rtexprvalue>
        <type>boolean</type>
    </attribute>
    <#if multiPrimaryKey><#list primaryFields as p>
        <@buildField p/>
    </#list><#elseif primaryKey??><@buildField primaryKey/></#if>
    <#list normalFields as p>
        <@buildField p/>
    </#list>
    <attribute>
        <name>dataSourceName</name>
        <rtexprvalue>true</rtexprvalue>
        <type>java.lang.String</type>
    </attribute>
    <attribute>
        <name>excludedFields</name>
        <rtexprvalue>true</rtexprvalue>
        <type>java.lang.String</type>
    </attribute>
    <attribute>
        <name>orderByFields</name>
        <rtexprvalue>true</rtexprvalue>
        <type>java.lang.String</type>
    </attribute>
    <attribute>
        <name>page</name>
        <rtexprvalue>true</rtexprvalue>
        <type>java.lang.Integer</type>
    </attribute>
    <attribute>
        <name>pageSize</name>
        <rtexprvalue>true</rtexprvalue>
        <type>java.lang.Integer</type>
    </attribute>
</tag>