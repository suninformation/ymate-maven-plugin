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
<#macro buildFieldName field><#if (field.prefix!"")?length == 0><#if (field.value!"")?contains(".")>${field.value!""}<#else>"${field.value!""}"</#if><#else>Fields.field("${field.prefix!""}", <#if (field.value!"")?contains(".")>${field.value!""}<#else>"${field.value!""}"</#if>)</#if></#macro>
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

import ${api.entityClass};
import ${app.packageName}.bean.*;
import ${app.packageName}.dto.*;
import ${app.packageName}.repository.I${api.name?cap_first}Repository;<#if multiPrimaryKey>
import ${app.packageName}.repository.impl.${api.name?cap_first}Repository;</#if>
import ${app.packageName}.vo.${api.name?cap_first}VO;<#if apidocs>
import net.ymate.apidocs.annotation.*;</#if>
import net.ymate.platform.commons.ExcelFileExportHelper;
import net.ymate.platform.core.beans.annotation.Inject;
import net.ymate.platform.core.persistence.*;
import net.ymate.platform.core.persistence.annotation.Transaction;
import net.ymate.platform.persistence.jdbc.JDBC;
import net.ymate.platform.validation.annotation.*;
import net.ymate.platform.validation.validate.*;
import net.ymate.platform.webmvc.annotation.*;
import net.ymate.platform.webmvc.base.Type;
import net.ymate.platform.webmvc.util.WebErrorCode;
import net.ymate.platform.webmvc.util.WebResult;
import net.ymate.platform.webmvc.view.View;
import net.ymate.platform.webmvc.view.impl.HttpStatusView;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.util.Collections;

/**
 * <#if api.description?? && (api.description?length > 0)>${api.description}<br></#if>
 *
 * ${api.name?cap_first}Controller generated By CrudMojo on ${.now?string("yyyy/MM/dd a HH:mm")}
 *
 * @author ${app.author!"YMP (https://www.ymate.net/"}
 * @version ${app.version!"1.0.0"}
 */
<#if apidocs>@Api(value = "${api.name}", description = "${api.description!""}")
</#if>@Controller
@RequestMapping("${api.mapping}")
public class ${api.name?cap_first}Controller {

    @Inject
    private JDBC database;

    @Inject
    private I${api.name?cap_first}Repository repository;

    <#if !(api.settings??) || api.settings.enableQuery!true><#if apidocs>@ApiAction(value = "${languageMap.query}", description = "")
    @ApiResponses(description = "", type = ${api.name?cap_first}VO.class)
    @ApiGenerateResponseExample(paging = true)</#if>
    @RequestMapping("/query")
    public Object query(<#if apidocs>@ApiParam </#if>@ModelBind ${api.name?cap_first}DTO ${api.name?uncap_first},

                        <#if apidocs>@ApiParam </#if>@ModelBind PageDTO page) throws Exception {
        IResultSet<${api.name?cap_first}VO> resultSet = repository.query${api.name?cap_first}s(database, ${api.name?uncap_first}.toBean(), <#if hideInListFields?? && (hideInListFields?size > 0)>Fields.create(<#list hideInListFields as field><@buildFieldName field/><#if field_has_next>, </#if></#list>)<#else>null</#if>, page.toPage());
        return WebResult.succeed().data(resultSet);
    }

    <#if apidocs>@ApiAction(value = "${languageMap.detail}", description = "")
    @ApiResponses(description = "", type = ${api.name?cap_first}VO.class)
    @ApiGenerateResponseExample</#if>
    @RequestMapping("/detail")
    public Object detail(<#list primaryFields as p><@parseField p false/><#if p_has_next>,

                        </#if></#list>) throws Exception {
        ${api.name?cap_first}VO ${api.name?uncap_first} = repository.query${api.name?cap_first}(database, <#if multiPrimaryKey>${api.name?cap_first}Repository.buildPrimaryKey(<#list primaryFields as p>${p.name}<#if p_has_next>, </#if></#list>)<#else>${primaryKey.name}</#if>, null);
        if (${api.name?uncap_first} != null) {
            return WebResult.succeed().data(${api.name?uncap_first});
        }
        return WebResult.create(WebErrorCode.resourceNotFoundOrNotExist());
    }</#if>

    <#if !(api.settings??) || api.settings.enableCreate!true><#if apidocs>@ApiAction(value = "${languageMap.create}", description = "")</#if>
    @RequestMapping(value = "/create", method = Type.HttpMethod.POST)
    @Transaction
    public Object create(<#if multiPrimaryKey><#list primaryFields as p><@parseField p false/><#if p_has_next>,

                         </#if></#list>, </#if><#if apidocs>@ApiParam </#if>@ModelBind ${api.name?cap_first}UpdateDTO ${api.name?uncap_first}Update) throws Exception {
        repository.create${api.name?cap_first}(database, <#if multiPrimaryKey>${api.name?cap_first}Repository.buildPrimaryKey(<#list primaryFields as p>${p.name}<#if p_has_next>, </#if></#list>), </#if>${api.name?uncap_first}Update.toBean());
        return WebResult.succeed();
    }</#if>

    <#if !(api.settings??) || api.settings.enableUpdate!true><#if apidocs>@ApiAction(value = "${languageMap.update}", description = "")</#if>
    @RequestMapping(value = "/update", method = Type.HttpMethod.POST)
    @Transaction
    public Object update(<#list primaryFields as p><@parseField p false/><#if p_has_next>,

                         </#if></#list>,

                         <#if apidocs>@ApiParam </#if>@ModelBind ${api.name?cap_first}UpdateDTO ${api.name?uncap_first}Update<#if lastModifyTimeProp??>,

                         <@parseField lastModifyTimeProp false/></#if>) throws Exception {
        repository.update${api.name?cap_first}(database, <#if multiPrimaryKey>${api.name?cap_first}Repository.buildPrimaryKey(<#list primaryFields as p>${p.name}<#if p_has_next>, </#if></#list>)<#else>${primaryKey.name}</#if>, ${api.name?uncap_first}Update.toBean()<#if lastModifyTimeProp??>, ${lastModifyTimeProp.name}</#if>);
        return WebResult.succeed();
    }<#list api.properties as p><#if !p.primary && p.config?? && p.config.status??><#list p.config.status as s><#if s.enabled>

    <#if apidocs>@ApiAction(value = "${s.name!""}", description = "${s.description!""}")</#if>
    @RequestMapping(value = "${s.mapping!""}", method = Type.HttpMethod.POST)
    @Transaction
    public Object ${s.name?uncap_first}(<#if multiPrimaryKey><#list primaryFields as p><@parseField p false/><#if p_has_next>,

                    </#if></#list><#else><@parseField primaryKey false/></#if><#if s.reason>,

                    <#if apidocs>@ApiParam
                    </#if>@VLength(max = 100)
                    @VField(name = "${languageMap.reason}")
                    @RequestParam String reason</#if>) throws Exception {
        repository.update${api.name?cap_first}s(database, ArrayUtils.toArray(<#if multiPrimaryKey>${api.name?cap_first}Repository.buildPrimaryKey(<#list primaryFields as p>${p.name}<#if p_has_next>, </#if></#list>)<#else>${primaryKey.name}</#if>), Fields.create(<@buildFieldName p.field/>), Params.create(${s.value}));
        return WebResult.succeed();
    }</#if></#list></#if></#list></#if>

    <#if !(api.settings??) || api.settings.enableRemove!true><#if apidocs>@ApiAction(value = "${languageMap.remove}", description = "")</#if>
    @RequestMapping(value = "/remove", method = Type.HttpMethod.POST)
    @Transaction
    public Object remove(<#if multiPrimaryKey><#list primaryFields as p><@parseField p false/><#if p_has_next>,

                         </#if></#list><#else><@parseField primaryKey true/></#if>) throws Exception {
        repository.remove${api.name?cap_first}s(database, <#if multiPrimaryKey>ArrayUtils.toArray(${api.name?cap_first}Repository.buildPrimaryKey(<#list primaryFields as p>${p.name}<#if p_has_next>, </#if></#list>))<#else>${primaryKey.name}</#if>);
        return WebResult.succeed();
    }</#if>

    <#if !(api.settings??) || api.settings.enableExport!true><#if apidocs>@ApiAction(value = "${languageMap.export}", description = "", notes = "${languageMap.notes}")</#if>
    @RequestMapping("/export")
    public Object export(<#if apidocs>@ApiParam </#if>@ModelBind ${api.name?cap_first}DTO ${api.name?uncap_first}) throws Exception {
        ExcelFileExportHelper exportHelper = ExcelFileExportHelper.bind(index -> {
            IResultSet<${api.name?cap_first}VO> resultSet = repository.query${api.name?cap_first}s(database, ${api.name?uncap_first}.toBean(), <#if hideInListFields?? && (hideInListFields?size > 0)>Fields.create(<#list hideInListFields as field><@buildFieldName field/><#if field_has_next>, </#if></#list>)<#else>null</#if>, Page.create(index).pageSize(10000).count(false));
            if (resultSet != null && resultSet.isResultsAvailable()) {
                return Collections.singletonMap("data", resultSet.getResultData());
            }
            return null;
        });
        File resultFile = exportHelper.export(${api.name?cap_first}VO.class);
        if (resultFile != null) {
            return View.binaryView(resultFile).useAttachment(resultFile.getName());
        }
        return HttpStatusView.NOT_FOUND;
    }</#if>
}