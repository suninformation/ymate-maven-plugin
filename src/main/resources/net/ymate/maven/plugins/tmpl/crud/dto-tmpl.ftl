<#setting number_format="#">
<#macro buildField p>
    <#if p.config?? && p.config.query?? && p.config.query.enabled><#if (p.description??)>
        /**
        * ${p.description}
        */</#if><#if apidocs>
        @ApiParam<#if p.config.query.validation?? && p.config.query.validation.dateTime?? && p.config.query.validation.dateTime.enabled>(description = "<#if p.description?? && (p.description?length > 0)>${p.description}<br/></#if>格式：`${((p.config.query.validation.dateTime.pattern!"")?length > 0)?string(p.config.query.validation.dateTime.pattern!"", "yyyy-MM-dd")}<#if p.config.query.validation.dateTime.single><#else> / ${((p.config.query.validation.dateTime.pattern!"")?length > 0)?string(p.config.query.validation.dateTime.pattern!"", "yyyy-MM-dd")}</#if>`")</#if></#if><#if p.config.query.required>
        @VRequired</#if><#if p.config.query.validation??><#if p.config.query.validation.length?? && p.config.query.validation.length.enabled><#if p.config.query.validation.dateTime?? && p.config.query.validation.dateTime.enabled><#else>
        @VLength(min = ${p.config.query.validation.length.min}, max = ${p.config.query.validation.length.max}, eq = ${p.config.query.validation.length.eq}<#if (p.config.query.validation.length.msg?length > 0)>, msg = "${p.config.query.validation.length.msg}"</#if>)</#if></#if><#if p.config.query.validation.dataRange?? && p.config.query.validation.dataRange.enabled && p.config.query.validation.dataRange.values?? && (p.config.query.validation.dataRange.values?size > 0)>
        @VDataRange(value = {<#list p.config.query.validation.dataRange.values as v>"${v}"<#if v_has_next>, </#if></#list>}, ignoreCase = ${p.config.query.validation.dataRange.ignoreCase?string}<#if (p.config.query.validation.dataRange.msg?length > 0)>, msg = "${p.config.query.validation.dataRange.msg}"</#if>)</#if><#if p.config.query.validation??><#if p.config.query.validation.regex?? && p.config.query.validation.regex.enabled && (p.config.query.validation.regex.regex?length > 0)>
        @VRegex(regex = "${p.config.query.validation.regex.regex}"<#if (p.config.query.validation.regex.msg?length > 0)>, msg = "${p.config.query.validation.regex.msg}"</#if>)</#if><#if p.config.query.validation.idCard?? && p.config.query.validation.idCard.enabled>
        @VIDCard<#if (p.config.query.validation.idCard.msg?length > 0)>(msg = "${p.config.query.validation.idCard.msg}")</#if></#if><#if p.config.query.validation.email?? && p.config.query.validation.email.enabled>
        @VEmail<#if (p.config.query.validation.email.msg?length > 0)>(msg = "${p.config.query.validation.email.msg}")</#if></#if><#if p.config.query.validation.mobile?? && p.config.query.validation.mobile.enabled>
        @VMobile(regex = "${p.config.query.validation.mobile.regex!""}"<#if (p.config.query.validation.mobile.msg?length > 0)>, msg = "${p.config.query.validation.mobile.msg}"</#if><#if (p.config.query.validation.mobile.msg?length > 0)>, msg = "${p.config.query.validation.mobile.msg}"</#if>)</#if><#if p.config.query.validation.dateTime?? && p.config.query.validation.dateTime.enabled>
        @VDateTime(value = "${p.config.query.validation.dateTime.value!p.name}", pattern = <#if ((p.config.query.validation.dateTime.pattern!"")?length > 0)>"${p.config.query.validation.dateTime.pattern!""}"<#else>DateTimeUtils.YYYY_MM_DD</#if>, single = ${p.config.query.validation.dateTime.single?string}, separator = "${((p.config.query.validation.dateTime.separator!"")?length > 0)?string(p.config.query.validation.dateTime.separator!"", "/")}", maxDays = ${p.config.query.validation.dateTime.maxDays}<#if (p.config.query.validation.dateTime.msg?length > 0)>, msg = "${p.config.query.validation.dateTime.msg}"</#if>)</#if><#if p.config.query.validation.numeric?? && p.config.query.validation.numeric.enabled>
        @VNumeric(min = ${p.config.query.validation.numeric.min}, max = ${p.config.query.validation.numeric.max}, eq = ${p.config.query.validation.numeric.eq}, decimals = ${p.config.query.validation.numeric.decimals}<#if (p.config.query.validation.numeric.msg?length > 0)>, msg = "${p.config.query.validation.numeric.msg}"</#if>)</#if></#if><#if p.description?? && (p.description?length > 0)>
        @VField(name = "${p.description}")</#if></#if>
        @RequestParam
        private <#if p.config.query.validation?? && p.config.query.validation.dateTime?? && p.config.query.validation.dateTime.enabled>String<#else>${p.type}</#if> ${p.name};</#if>

</#macro>
<#macro builderGetAndSet p>

    public <#if p.config.query.validation?? && p.config.query.validation.dateTime?? && p.config.query.validation.dateTime.enabled>String<#else>${p.type}</#if> ${p.name}() {
        return targetDTO.get${p.name?cap_first}();
    }

    public Builder ${p.name}(<#if p.config.query.validation?? && p.config.query.validation.dateTime?? && p.config.query.validation.dateTime.enabled>String<#else>${p.type}</#if> ${p.name}) {
        targetDTO.set${p.name?cap_first}(${p.name});
        return this;
    }
</#macro>
<#macro buildGetAndSet p>

    public <#if p.config.query.validation?? && p.config.query.validation.dateTime?? && p.config.query.validation.dateTime.enabled>String<#else>${p.type}</#if> get${p.name?cap_first}() {
        return ${p.name};
    }

    public void set${p.name?cap_first}(<#if p.config.query.validation?? && p.config.query.validation.dateTime?? && p.config.query.validation.dateTime.enabled>String<#else>${p.type}</#if> ${p.name}) {
        this.${p.name} = ${p.name};
    }

</#macro>
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
package ${app.packageName}.dto;

import ${app.packageName}.bean.${api.name?cap_first}Bean;<#if apidocs>
import net.ymate.apidocs.annotation.*;</#if>
import net.ymate.platform.commons.util.DateTimeUtils;
import net.ymate.platform.core.beans.annotation.*;
import net.ymate.platform.validation.annotation.*;
import net.ymate.platform.validation.validate.*;
import net.ymate.platform.webmvc.annotation.*;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * <#if api.description?? && (api.description?length > 0)>${api.description}<br></#if>
 *
 * ${api.name?cap_first}DTO generated By CrudMojo on ${.now?string("yyyy/MM/dd a HH:mm")}
 *
 * @author ${app.author!"YMP (https://www.ymate.net/)"}
 * @version ${app.version!"1.0.0"}
 */
public class ${api.name?cap_first}DTO implements Serializable {

    private static final long serialVersionUID = 1L;

<#if multiPrimaryKey><#list primaryFields as p><#if p.config?? && p.config.query?? && p.config.query.enabled>
    <@buildField p/>
</#if></#list><#elseif primaryKey?? && primaryKey.config?? && primaryKey.config.query?? && primaryKey.config.query.enabled><@buildField primaryKey/></#if>

<#list normalFields as p><#if p.config?? && p.config.query?? && p.config.query.enabled>
    <@buildField p/>
</#if></#list><#if multiPrimaryKey><#list primaryFields as p><#if p.config?? && p.config.query?? && p.config.query.enabled>
    <@buildGetAndSet p/>
</#if></#list><#elseif primaryKey?? && primaryKey.config?? && primaryKey.config.query?? && primaryKey.config.query.enabled><@buildGetAndSet primaryKey/></#if><#list normalFields as p><#if p.config?? && p.config.query?? && p.config.query.enabled>
    <@buildGetAndSet p/>
</#if></#list><#if (normalFields?size > 0)>
    public ${api.name?cap_first}Bean toBean() {
        ${api.name?cap_first}Bean.Builder builder = ${api.name?cap_first}Bean.builder()<#if multiPrimaryKey><#list primaryFields as p><#if p.config?? && p.config.query?? && p.config.query.enabled>
                .${p.name}(${p.name})</#if></#list><#elseif primaryKey?? && primaryKey.config?? && primaryKey.config.query?? && primaryKey.config.query.enabled>
                .${primaryKey.name}(${primaryKey.name})</#if><#list normalFields as p><#if p.config?? && p.config.query?? && p.config.query.enabled><#if p.config.query.validation?? && p.config.query.validation.dateTime?? && p.config.query.validation.dateTime.enabled><#else>
                .${p.name}(${p.name})</#if></#if></#list>;<#list normalFields as p><#if p.config?? && p.config.query?? && p.config.query.enabled><#if p.config.query.validation?? && p.config.query.validation.dateTime?? && p.config.query.validation.dateTime.enabled>
        DateTimeValue.get("${p.name}", ${p.name}Value -> builder.start${p.name?cap_first}(<#if p.type?ends_with('sql.Date')>new java.sql.Date(${p.name}Value.getStartDateTimeMillis())<#else>${p.name}Value.<#if p.type?ends_with('Timestamp') || p.type?ends_with('util.Date')>getStartDateTimestampOrNull<#else>getStartDateTimeMillisOrNull</#if>()</#if>)
                .end${p.name?cap_first}(<#if p.type?ends_with('sql.Date')>new java.sql.Date(${p.name}Value.getEndDateTimeMillis())<#else>${p.name}Value.<#if p.type?ends_with('Timestamp') || p.type?ends_with('util.Date')>getEndDateTimestampOrNull<#else>getEndDateTimeMillisOrNull</#if>()</#if>));</#if></#if></#list>
        return builder.build();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)<#if multiPrimaryKey><#list primaryFields as p><#if p.config?? && p.config.query?? && p.config.query.enabled>
            .append("${p.name}", ${p.name})</#if></#list><#elseif primaryKey?? && primaryKey.config?? && primaryKey.config.query?? && primaryKey.config.query.enabled>
            .append("${primaryKey.name}", ${primaryKey.name})</#if><#list normalFields as p><#if p.config?? && p.config.query?? && p.config.query.enabled>
            .append("${p.name}", ${p.name})</#if></#list>
            .toString();
    }

    public Builder bind() {
        return new Builder(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final ${api.name?cap_first}DTO targetDTO;

        public Builder() {
            targetDTO = new ${api.name?cap_first}DTO();
        }

        public Builder(${api.name?cap_first}DTO targetDTO) {
            this.targetDTO = targetDTO;
        }

        public ${api.name?cap_first}DTO build() {
            return targetDTO;
        }<#if multiPrimaryKey><#list primaryFields as p><#if p.config?? && p.config.query?? && p.config.query.enabled>
        <@builderGetAndSet p/></#if></#list><#elseif primaryKey?? && primaryKey.config?? && primaryKey.config.query?? && primaryKey.config.query.enabled>
        <@builderGetAndSet primaryKey/></#if><#list normalFields as p><#if p.config?? && p.config.query?? && p.config.query.enabled><@builderGetAndSet p/></#if></#list>
    }</#if>
}