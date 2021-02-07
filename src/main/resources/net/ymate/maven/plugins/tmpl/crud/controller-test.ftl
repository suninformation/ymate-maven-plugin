<#setting number_format="#">
<#macro parseField p array><#if apidocs>@ApiParam
                         </#if>@VRequired<#if p.config.query.validation.length?? && p.config.query.validation.length.enabled><#if p.config.query.validation.dateTime?? && p.config.query.validation.dateTime.enabled><#else>
                         @VLength(min = ${p.config.query.validation.length.min}, max = ${p.config.query.validation.length.max}, eq = ${p.config.query.validation.length.eq}<#if (p.config.query.validation.length.msg?length > 0)>, msg = "${p.config.query.validation.length.msg}"</#if>)</#if></#if><#if p.config.query.validation.dataRange?? && p.config.query.validation.dataRange.enabled && p.config.query.validation.dataRange.values?? && (p.config.query.validation.dataRange.values?size > 0)>
                         @VDataRange(value = {<#list p.config.query.validation.dataRange.values as v>"${v}"<#if v_has_next>, </#if></#list>}, ignoreCase = ${p.config.query.validation.dataRange.ignoreCase?string}<#if (p.config.query.validation.dataRange.msg?length > 0)>, msg = "${p.config.query.validation.dataRange.msg}"</#if>)</#if><#if p.config.query.validation??><#if p.config.query.validation.regex?? && p.config.query.validation.regex.enabled && (p.config.query.validation.regex.regex?length > 0)>
                         @VRegex(regex = "${p.config.query.validation.regex.regex}"<#if (p.config.query.validation.regex.msg?length > 0)>, msg = "${p.config.query.validation.regex.msg}"</#if>)</#if><#if p.config.query.validation.idCard?? && p.config.query.validation.idCard.enabled>
                         @VIDCard<#if (p.config.query.validation.idCard.msg?length > 0)>(msg = "${p.config.query.validation.idCard.msg}")</#if></#if><#if p.config.query.validation.email?? && p.config.query.validation.email.enabled>
                         @VEmail<#if (p.config.query.validation.email.msg?length > 0)>(msg = "${p.config.query.validation.email.msg}")</#if></#if><#if p.config.query.validation.mobile?? && p.config.query.validation.mobile.enabled>
                         @VMobile(regex = "${p.config.query.validation.mobile.regex!""}"<#if (p.config.query.validation.mobile.msg?length > 0)>, msg = "${p.config.query.validation.mobile.msg}"</#if><#if (p.config.query.validation.mobile.msg?length > 0)>, msg = "${p.config.query.validation.mobile.msg}"</#if>)</#if><#if p.config.query.validation.numeric?? && p.config.query.validation.numeric.enabled>
                         @VNumeric(min = ${p.config.query.validation.numeric.min}, max = ${p.config.query.validation.numeric.max}, eq = ${p.config.query.validation.numeric.eq}, decimals = ${p.config.query.validation.numeric.decimals}<#if (p.config.query.validation.numeric.msg?length > 0)>, msg = "${p.config.query.validation.numeric.msg}"</#if>)</#if></#if><#if p.description?? && (p.description?length > 0)>
                         @VField(name = "${p.description}")</#if>
                         @RequestParam ${p.type!"String"}<#if array!false>[]</#if> ${p.name}</#macro>
<#macro buildFieldName field withoutPrefix><#if withoutPrefix || (field.prefix!"")?length == 0><#if (field.value!"")?contains(".")>${field.value!""}<#else>"${field.value!""}"</#if><#else>Fields.field("${field.prefix!""}", <#if (field.value!"")?contains(".")>${field.value!""}<#else>"${field.value!""}"</#if>)</#if></#macro>
/*
 * Copyright ${.now?string("yyyy")} the original author or authors.
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
package ${app.packageName}.controller;

<#if entityPackageName??>import ${entityPackageName}.*;<#elseif api.entityClass??>import ${api.entityClass};</#if>
import ${app.packageName}.bean.*;
import ${app.packageName}.dto.*;
import ${app.packageName}.repository.I${api.name?cap_first}Repository;<#if multiPrimaryKey>
import ${app.packageName}.repository.impl.${api.name?cap_first}Repository;</#if>
import ${app.packageName}.vo.${api.name?cap_first}VO;
import net.ymate.platform.mock.web.*;
import net.ymate.platform.persistence.jdbc.JDBC;
import net.ymate.platform.core.annotation.EnableAutoScan;
import net.ymate.platform.core.annotation.EnableBeanProxy;
import net.ymate.platform.core.annotation.EnableDevMode;
import net.ymate.platform.core.beans.annotation.Inject;
import net.ymate.platform.test.YMPJUnit4ClassRunner;
import org.junit.*;
import org.junit.runner.RunWith;

/**
 * <#if api.description?? && (api.description?length > 0)>${api.description}<br></#if>
 *
 * ${api.name?cap_first}ControllerTest generated By CrudMojo on ${.now?string("yyyy/MM/dd a HH:mm")}
 *
 * @author ${app.author!"YMP (https://www.ymate.net/"}
 * @version ${app.version!"1.0.0"}
 */
@RunWith(YMPJUnit4ClassRunner.class)
@EnableAutoScan
@EnableBeanProxy
@EnableDevMode
public class ${api.name?cap_first}ControllerTest {

    @Inject
    private JDBC database;

    @Inject
    private I${api.name?cap_first}Repository repository;

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    <#if !(api.settings??) || api.settings.enableQuery!true>@Test
    public void testQuery() throws Exception {
    }

    <#if !api.view>@Test
    public void testDetail() throws Exception {
    }</#if>

    <#if !(api.settings??) || api.settings.enableCreate!true>@Test
    public void testCreate() throws Exception {
    }</#if>

    <#if !(api.settings??) || api.settings.enableUpdate!true>@Test
    public void testUpdate() throws Exception {
    }<#list api.properties as p><#if !p.primary && p.config?? && p.config.status??><#list p.config.status as s><#if s.enabled>

    @Test
    public void test${s.methodName?cap_first}() throws Exception {
    }</#if></#list></#if></#list></#if>

    <#if !(api.settings??) || api.settings.enableRemove!true>@Test
    public void testRemove() throws Exception {
    }</#if></#if>

    <#if !(api.settings??) || api.settings.enableExport!true>@Test
    public void testExport() throws Exception {
    }</#if>
}