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

<#if (config.useStateSupport)>import net.ymate.platform.core.beans.annotation.PropertyState;</#if>
import net.ymate.platform.core.persistence.annotation.Comment;
import net.ymate.platform.core.persistence.annotation.Default;
import net.ymate.platform.core.persistence.annotation.Entity;
import net.ymate.platform.core.persistence.annotation.Id;
import net.ymate.platform.core.persistence.annotation.Property;
import net.ymate.platform.core.persistence.annotation.Readonly;<#if (!config.useBaseEntity)>
import net.ymate.platform.persistence.jdbc.support.BaseEntity;
import net.ymate.platform.core.persistence.IShardingable;
import net.ymate.platform.persistence.jdbc.IDatabase;
import net.ymate.platform.persistence.jdbc.IDatabaseConnectionHolder;</#if>

/**
 * ${entityInfo.name?cap_first}<#if (config.useClassSuffix)>${config.classSuffix?cap_first}</#if> generated By EntityMojo on ${lastUpdateTime?string("yyyy/MM/dd HH:mm:ss")}
 *
 * @author YMP (https://www.ymate.net/)
 */
@Entity(${entityInfo.name?cap_first}<#if (config.useClassSuffix)>${config.classSuffix?cap_first}</#if>.TABLE_NAME)
public class ${entityInfo.name?cap_first}<#if (config.useClassSuffix)>${config.classSuffix?cap_first}</#if> extends <#if (config.useBaseEntity)>BaseEntity<${entityInfo.primaryKeyType}><#else>BaseEntity<${entityInfo.name?cap_first}<#if (config.useClassSuffix)>${config.classSuffix?cap_first}</#if>, ${entityInfo.primaryKeyType}></#if> {

    private static final long serialVersionUID = 1L;

<#list entityInfo.fields as field>
    <#if entityInfo.primaryKeyName == field.varName>@Id</#if><#if (field.columnName!"undefined") != "undefined">
    @Property(name = FIELDS.${field.columnName?upper_case}<#if (field.autoIncrement)>, autoincrement=true</#if><#if (!field.nullable)>, nullable = false</#if><#if (!field.signed)>, unsigned = true</#if><#if (field.precision > 0)>, length = ${field.precision?string('#')}</#if><#if (field.scale > 0)>, decimals = ${field.scale}</#if>)<#if (field.defaultValue!"undefined") != "undefined">
    @Default("${field.defaultValue}")</#if><#if (field.remarks!"undefined") != "undefined">
    @Comment("${field.remarks}")</#if><#if (config.useStateSupport)>
    @PropertyState(propertyName = FIELDS.${field.columnName?upper_case})</#if><#if (field.readonly)>
    @Readonly</#if></#if>
    private ${field.varType} ${field.varName};

</#list>

    public ${entityInfo.name?cap_first}<#if (config.useClassSuffix)>${config.classSuffix?cap_first}</#if>() {
    }

<#if (!config.useBaseEntity)>
    public ${entityInfo.name?cap_first}<#if (config.useClassSuffix)>${config.classSuffix?cap_first}</#if>(IDatabase dbOwner) {
        super(dbOwner);
    }
</#if>

<#if (entityInfo.nonNullableFields?size > 0 && entityInfo.nonNullableFields?size != entityInfo.fields?size)>
    public ${entityInfo.name?cap_first}<#if (config.useClassSuffix)>${config.classSuffix?cap_first}</#if>(<#list entityInfo.nonNullableFields as field>${field.varType} ${field.varName}<#if field_has_next>, </#if></#list>) {
    <#list entityInfo.nonNullableFields as field>
        this.${field.varName} = ${field.varName};
    </#list>
    }

    <#if (!config.useBaseEntity)>
        public ${entityInfo.name?cap_first}<#if (config.useClassSuffix)>${config.classSuffix?cap_first}</#if>(IDatabase dbOwner, <#list entityInfo.nonNullableFields as field>${field.varType} ${field.varName}<#if field_has_next>, </#if></#list>) {
            super(dbOwner);
        <#list entityInfo.nonNullableFields as field>
            this.${field.varName} = ${field.varName};
        </#list>
        }
    </#if>
</#if>

    public ${entityInfo.name?cap_first}<#if (config.useClassSuffix)>${config.classSuffix?cap_first}</#if>(<#list entityInfo.fields as field>${field.varType} ${field.varName}<#if field_has_next>, </#if></#list>) {
    <#list entityInfo.fields as field>
        this.${field.varName} = ${field.varName};
    </#list>
    }

<#if (!config.useBaseEntity)>
    public ${entityInfo.name?cap_first}<#if (config.useClassSuffix)>${config.classSuffix?cap_first}</#if>(IDatabase dbOwner, <#list entityInfo.fields as field>${field.varType} ${field.varName}<#if field_has_next>, </#if></#list>) {
        super(dbOwner);
    <#list entityInfo.fields as field>
        this.${field.varName} = ${field.varName};
    </#list>
    }
</#if>

    @Override
    public ${entityInfo.primaryKeyType} getId() {
        return ${entityInfo.primaryKeyName};
    }

    @Override
    public void setId(${entityInfo.primaryKeyType} id) {
        this.${entityInfo.primaryKeyName} = id;
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

        public IShardingable shardingable() {
            return targetEntity.getShardingable();
        }

        public Builder shardingable(IShardingable shardingable) {
            targetEntity.setShardingable(shardingable);
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
