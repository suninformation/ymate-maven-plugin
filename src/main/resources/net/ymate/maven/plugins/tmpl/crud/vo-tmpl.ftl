<#setting number_format="#">
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
package ${app.packageName}.vo;

<#if apidocs>import net.ymate.apidocs.annotation.*;</#if>
import net.ymate.platform.commons.annotation.ExportColumn;
import net.ymate.platform.persistence.jdbc.query.Cond;
import net.ymate.platform.persistence.jdbc.query.Join;
import net.ymate.platform.persistence.jdbc.query.annotation.*;
import org.apache.commons.lang3.builder.ToStringBuilder;

<#if entityPackageName??>import ${entityPackageName}.*;<#elseif api.entityClass??>import ${api.entityClass};</#if>

import java.io.Serializable;

/**
 * <#if api.description?? && (api.description?length > 0)>${api.description}<br></#if>
 *
 * ${api.name?cap_first}VO generated By CrudMojo on ${.now?string("yyyy/MM/dd a HH:mm")}
 *
 * @author ${app.author!"YMP (https://www.ymate.net/)"}
 * @version ${app.version!"1.0.0"}
 */<#if api.query??><#if api.query.froms??><#list api.query.froms as from>
@QFrom(type = QFrom.Type.${from.type!"TABLE"}, prefix = "${from.prefix!""}", value = <#if (from.value!"")?contains(".")>${from.value!""}<#else>"${from.value!""}"</#if>, alias = "${from.alias!""}")</#list></#if><#if api.query.joins??><#list api.query.joins as join><#if (join.from?? && (join.from.value!"")?length > 0 && join.on?? && join.on?size > 0)>
@QJoin(type = Join.Type.${join.type!"LEFT"}, from = @QFrom(type = QFrom.Type.${join.from.type!"TABLE"}, prefix = "${join.from.prefix!""}", value = <#if (join.from.value!"")?contains(".")>${join.from.value!""}<#else>"${join.from.value!""}"</#if>, alias = "${join.from.alias!""}"),
        on = {<#list join.on as on><#if ((on.field.value!"")?length > 0)>@QCond(field = @QField(prefix = "${on.field.prefix!""}", value = <#if (on.field.value!"")?contains(".")>${on.field.value!""}<#else>"${on.field.value!""}"</#if>), opt = Cond.OPT.${on.opt!"EQ"}, with = @QField(prefix = "${on.with.prefix!""}", value = <#if (on.with.value!"")?contains(".")>${on.with.value!""}<#else>"${on.with.value!""}"</#if>), logicalOpt = Cond.LogicalOpt.${on.logicalOpt!"AND"}, ignorable = ${on.ignorable?string})<#if on_has_next>, </#if></#if></#list>})</#if></#list></#if><#if api.query.orderFields??>
@QOrderBy({<#list api.query.orderFields as orderField>@QOrderField(prefix = "${orderField.prefix!""}", value = <#if (orderField.value!"")?contains(".")>${orderField.value!""}<#else>"${orderField.value!""}"</#if>, type = QOrderField.Type.${orderField.type!"ASC"?upper_case})<#if orderField_has_next>, </#if></#list>})</#if></#if>
public class ${api.name?cap_first}VO implements Serializable {

    private static final long serialVersionUID = 1L;

<#if (api.properties?? && api.properties?size > 0)><#list api.properties as p><#if (p.description??)>
    /**
     * ${p.description}
     */</#if><#if apidocs>
    @ApiProperty(description = "${p.description!""}", demoValue = "${p.demoValue!""}")</#if><#if p.field??>
    @QField(prefix = "${p.field.prefix!""}", value = <#if (p.field.value!"")?contains(".")>${p.field.value!""}<#else>"${p.field.value!""}"</#if><#if (p.field.alias?? && p.field.alias?length > 0)>, alias = <#if (p.field.alias!"")?contains(".")>${p.field.alias!""}<#else>"${p.field.alias!""}"</#if></#if>)</#if><#if p.export>
    @ExportColumn(value = "${p.description!""}"<#if p.column??><#if p.column?lower_case == "create_time" || p.column?lower_case == "createtime" || p.column?lower_case == "create_at" || p.column?lower_case == "createat" || p.column?lower_case == "last_modify_time" || p.column?lower_case == "lastmodifytime" || p.column?lower_case == "last_modify_at" || p.column?lower_case == "lastmodifyat">, dateTime = true</#if><#if p.column == "status">, dataRange = {"启用", "禁用"}</#if></#if>)<#else>
    @ExportColumn(excluded = true)</#if>
    private ${p.type} ${p.name};

</#list></#if><#if (api.properties?? && api.properties?size > 0)><#list api.properties as p>
    public ${p.type} get${p.name?cap_first}() {
        return ${p.name};
    }

    public void set${p.name?cap_first}(${p.type} ${p.name}) {
        this.${p.name} = ${p.name};
    }

</#list></#if><#if (api.properties?? && api.properties?size > 0)>
    @Override
    public String toString() {
        return new ToStringBuilder(this)<#list api.properties as p>
            .append("${p.name}", ${p.name})</#list>
            .toString();
    }

    public Builder bind() {
        return new Builder(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final ${api.name?cap_first}VO targetVO;

        public Builder() {
            targetVO = new ${api.name?cap_first}VO();
        }

        public Builder(${api.name?cap_first}VO targetVO) {
            this.targetVO = targetVO;
        }

        public ${api.name?cap_first}VO build() {
            return targetVO;
        }<#list api.properties as p>

        public ${p.type} ${p.name}() {
            return targetVO.get${p.name?cap_first}();
        }

        public Builder ${p.name}(${p.type} ${p.name}) {
            targetVO.set${p.name?cap_first}(${p.name});
            return this;
        }</#list>
    }</#if>
}