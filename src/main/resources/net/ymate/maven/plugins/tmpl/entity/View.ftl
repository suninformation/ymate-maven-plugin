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

import net.ymate.platform.core.persistence.annotation.Comment;
import net.ymate.platform.core.persistence.annotation.Entity;
import net.ymate.platform.core.persistence.annotation.Id;
import net.ymate.platform.core.persistence.annotation.Property;
import net.ymate.platform.core.persistence.annotation.Readonly;<#if (!config.useBaseEntity)>
import net.ymate.platform.persistence.jdbc.support.BaseEntity;
import net.ymate.platform.persistence.jdbc.IDatabase;
import net.ymate.platform.persistence.jdbc.IDatabaseConnectionHolder;</#if>
<#if (entityInfo.primaryKeyType == "Serializable")>
import java.io.Serializable;</#if>

/**
 * ${entityInfo.name?cap_first}<#if (config.useClassSuffix)>${config.classSuffix?cap_first}</#if> generated By EntityMojo on ${lastUpdateTime?string("yyyy/MM/dd HH:mm:ss")}
 *
 * @author YMP (https://www.ymate.net/)
 */
@Entity(${entityInfo.name?cap_first}<#if (config.useClassSuffix)>${config.classSuffix?cap_first}</#if>.TABLE_NAME)
@Readonly
public class ${entityInfo.name?cap_first}<#if (config.useClassSuffix)>${config.classSuffix?cap_first}</#if> extends <#if (config.useBaseEntity)>BaseEntity<${entityInfo.primaryKeyType}><#else>BaseEntity<${entityInfo.name?cap_first}<#if (config.useClassSuffix)>${config.classSuffix?cap_first}</#if>, ${entityInfo.primaryKeyType}></#if> {

    private static final long serialVersionUID = 1L;

<#list entityInfo.fields as field>
    <#if entityInfo.primaryKeyName == field.varName>@Id</#if><#if (field.columnName!"undefined") != "undefined">
    @Property(name = FIELDS.${field.columnName?upper_case})<#if (field.remarks!"undefined") != "undefined">
    @Comment("${field.remarks}")</#if></#if>
    private ${field.varType} ${field.varName};

</#list>

    public ${entityInfo.name?cap_first}<#if (config.useClassSuffix)>${config.classSuffix?cap_first}</#if>() {
    }

<#if (!config.useBaseEntity)>
    public ${entityInfo.name?cap_first}<#if (config.useClassSuffix)>${config.classSuffix?cap_first}</#if>(IDatabase dbOwner) {
        super(dbOwner);
    }
</#if>

    @Override
    public ${entityInfo.primaryKeyType} getId() {
        return <#if (entityInfo.primaryKeyType == "Serializable")>null<#else>${entityInfo.primaryKeyName}</#if>;
    }

    @Override
    public void setId(${entityInfo.primaryKeyType} id) {
        <#if (entityInfo.primaryKeyType == "Serializable")>throw new UnsupportedOperationException("View does not included property id.")<#else>this.${entityInfo.primaryKeyName} = id</#if>;
    }

<#list entityInfo.fields as field>
    <#if field.varName != 'id'>
    public ${field.varType} get${field.varName?cap_first}() {
        return ${field.varName};
    }

    public void set${field.varName?cap_first}(${field.varType} ${field.varName}) {
        this.${field.varName} = ${field.varName};
    }

    <#elseif field.varName != entityInfo.primaryKeyName>
    public ${field.varType} get_${field.varName?cap_first}() {
        return ${field.varName};
    }

    public void set_${field.varName?cap_first}(${field.varType} ${field.varName}) {
        this.${field.varName} = ${field.varName};
    }

    </#if>
</#list>

<#if (config.useChainMode)>
    public Builder bind() {
        return new Builder(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    <#if (!config.useBaseEntity)>
        public static Builder builder(IDatabase dbOwner) {
            return new Builder(dbOwner);
        }
    </#if>

    public static class Builder {

        private final ${entityInfo.name?cap_first}<#if (config.useClassSuffix)>${config.classSuffix?cap_first}</#if> targetEntity;

        public Builder() {
            targetEntity = new ${entityInfo.name?cap_first}<#if (config.useClassSuffix)>${config.classSuffix?cap_first}</#if>();
        }

    <#if (!config.useBaseEntity)>
        public Builder(IDatabase dbOwner) {
            targetEntity = new ${entityInfo.name?cap_first}<#if (config.useClassSuffix)>${config.classSuffix?cap_first}</#if>(dbOwner);
        }
    </#if>

        public Builder(${entityInfo.name?cap_first}<#if (config.useClassSuffix)>${config.classSuffix?cap_first}</#if> targetEntity) {
            this.targetEntity = targetEntity;
        }

        public ${entityInfo.name?cap_first}<#if (config.useClassSuffix)>${config.classSuffix?cap_first}</#if> build() {
            return targetEntity;
        }

    <#if (!config.useBaseEntity)>
        public IDatabaseConnectionHolder connectionHolder() {
            return targetEntity.getConnectionHolder();
        }

        public Builder connectionHolder(IDatabaseConnectionHolder connectionHolder) {
            targetEntity.setConnectionHolder(connectionHolder);
            return this;
        }

        public String dataSourceName() {
            return targetEntity.getDataSourceName();
        }

        public Builder dataSourceName(String dataSourceName) {
            targetEntity.setDataSourceName(dataSourceName);
            return this;
        }
    </#if>

    <#list entityInfo.fields as field>

        public ${field.varType} ${field.varName}() {
            return targetEntity.get${field.varName?cap_first}();
        }

        public Builder ${field.varName}(${field.varType} ${field.varName}) {
            targetEntity.set${field.varName?cap_first}(${field.varName});
            return this;
        }
    </#list>
    }
</#if>

    public interface FIELDS {
    <#list entityInfo.constFields as field>
        ${field.varType} ${field.varName} = "${field.columnName}";
    </#list>
    }

    public static final String TABLE_NAME = "${entityInfo.tableName}";
}
