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
package ${config.packageName}.${config.classSuffix?lower_case};

import java.io.Serializable;

/**
 * ${entityInfo.name?cap_first}Bean generated By EntityMojo on ${lastUpdateTime?string("yyyy/MM/dd HH:mm:ss")}
 *
 * @author YMP (https://www.ymate.net/)
 */
public class ${entityInfo.name?cap_first}Bean implements Serializable {

    private static final long serialVersionUID = 1L;

<#list entityInfo.primaryKeys as field>
    <#if (field.remarks!"undefined") != "undefined">/**
     * ${field.remarks}
     */</#if>
    private ${field.varType} ${field.varName};

</#list>

<#list entityInfo.fields as field>
    <#if field.varName != entityInfo.primaryKeyName || (entityInfo.primaryKeys?size == 0)>
    <#if (field.remarks!"undefined") != "undefined">/**
     * ${field.remarks}
     */</#if>
    private ${field.varType} ${field.varName};

    </#if>
</#list>

    public ${entityInfo.name?cap_first}Bean() {
    }

<#list entityInfo.primaryKeys as field>
    public ${field.varType} get${field.varName?cap_first}() {
        return ${field.varName};
    }

    public void set${field.varName?cap_first}(${field.varType} ${field.varName}) {
        this.${field.varName} = ${field.varName};
    }

</#list>
<#list entityInfo.fields as field>
    <#if field.varName != entityInfo.primaryKeyName || (entityInfo.primaryKeys?size == 0)>
    public ${field.varType} get${field.varName?cap_first}() {
        return ${field.varName};
    }

    public void set${field.varName?cap_first}(${field.varType} ${field.varName}) {
        this.${field.varName} = ${field.varName};
    }

    </#if>
</#list>

<#if (config.useChainMode)>
    public ${entityInfo.name?cap_first}BeanBuilder bind() {
        return new ${entityInfo.name?cap_first}BeanBuilder(this);
    }

    public static ${entityInfo.name?cap_first}BeanBuilder builder() {
        return new ${entityInfo.name?cap_first}BeanBuilder();
    }

    public static class ${entityInfo.name?cap_first}BeanBuilder {

        private final ${entityInfo.name?cap_first}Bean targetBean;

        public ${entityInfo.name?cap_first}BeanBuilder() {
            targetBean = new ${entityInfo.name?cap_first}Bean();
        }

        public ${entityInfo.name?cap_first}BeanBuilder(${entityInfo.name?cap_first}Bean targetBean) {
            this.targetBean = targetBean;
        }

        public ${entityInfo.name?cap_first}Bean build() {
            return targetBean;
        }
    <#list entityInfo.primaryKeys as field>

        public ${field.varType} ${field.varName}() {
            return targetBean.get${field.varName?cap_first}();
        }

        public ${entityInfo.name?cap_first}BeanBuilder ${field.varName}(${field.varType} ${field.varName}) {
            targetBean.set${field.varName?cap_first}(${field.varName});
            return this;
        }
    </#list>

    <#list entityInfo.fields as field>
        <#if field.varName != entityInfo.primaryKeyName || (entityInfo.primaryKeys?size == 0)>
        public ${field.varType} ${field.varName}() {
            return targetBean.get${field.varName?cap_first}();
        }

        public ${entityInfo.name?cap_first}BeanBuilder ${field.varName}(${field.varType} ${field.varName}) {
            targetBean.set${field.varName?cap_first}(${field.varName});
            return this;
        }
        </#if>
    </#list>
    }
</#if>
}
