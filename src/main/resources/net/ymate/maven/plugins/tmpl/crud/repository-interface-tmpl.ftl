<#setting number_format="#">
<#macro toSetId><#if primaryKey?? && !primaryKey.autoIncrement>.id(buildPrimaryKey())<#elseif multiPrimaryKey>.id(id)</#if></#macro>
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
package ${app.packageName}.repository;

<#if entityPackageName??>import ${entityPackageName}.*;<#elseif api.entityClass??>import ${api.entityClass};</#if>
import ${app.packageName}.bean.${api.name?cap_first}Bean;<#if !api.view>
import ${app.packageName}.bean.${api.name?cap_first}UpdateBean;<#if multiPrimaryKey>
import ${entityPackageName}.${api.name?cap_first}PK;</#if></#if>
import ${app.packageName}.vo.${api.name?cap_first}VO;
import net.ymate.platform.core.persistence.Fields;
import net.ymate.platform.core.persistence.IResultSet;
import net.ymate.platform.core.persistence.Page;
import net.ymate.platform.core.persistence.Params;
import net.ymate.platform.persistence.jdbc.IDatabase;
import net.ymate.platform.persistence.jdbc.query.Cond;
import net.ymate.platform.persistence.jdbc.query.OrderBy;

/**
 * <#if api.description?? && (api.description?length > 0)>${api.description}<br></#if>
 *
 * I${api.name?cap_first}Repository generated By CrudMojo on ${.now?string("yyyy/MM/dd a HH:mm")}
 *
 * @author ${app.author!"YMP (https://www.ymate.net/"}
 * @version ${app.version!"1.0.0"}
 */
public interface I${api.name?cap_first}Repository {

    <#if !api.view><#if !(api.settings??) || api.settings.enableCreate!true>default ${entityName} create${api.name?cap_first}(IDatabase owner, <#if multiPrimaryKey>${api.name?cap_first}PK id, </#if>${api.name?cap_first}UpdateBean updateBean) throws Exception {
        return create${api.name?cap_first}(owner, owner.getConfig().getDefaultDataSourceName(), <#if multiPrimaryKey>id, </#if>updateBean);
    }

    ${entityName} create${api.name?cap_first}(IDatabase owner, String dataSourceName, <#if multiPrimaryKey>${api.name?cap_first}PK id, </#if>${api.name?cap_first}UpdateBean updateBean) throws Exception;</#if>

    <#if !(api.settings??) || api.settings.enableUpdate!true>default ${entityName} update${api.name?cap_first}(IDatabase owner, <#if multiPrimaryKey>${api.name?cap_first}PK<#else>${primaryKey.type}</#if> id, ${api.name?cap_first}UpdateBean updateBean<#if lastModifyTimeProp??>, ${lastModifyTimeProp.type} lastModifyTime</#if>) throws Exception {
        return update${api.name?cap_first}(owner, owner.getConfig().getDefaultDataSourceName(), id, updateBean<#if lastModifyTimeProp??>, lastModifyTime</#if>);
    }

    ${entityName} update${api.name?cap_first}(IDatabase owner, String dataSourceName, <#if multiPrimaryKey>${api.name?cap_first}PK<#else>${primaryKey.type}</#if> id, ${api.name?cap_first}UpdateBean updateBean<#if lastModifyTimeProp??>, ${lastModifyTimeProp.type} lastModifyTime</#if>) throws Exception;

    default int update${api.name?cap_first}s(IDatabase owner, <#if multiPrimaryKey>${api.name?cap_first}PK<#else>${primaryKey.type}</#if>[] ids, Fields fields, Params values) throws Exception {
        return update${api.name?cap_first}s(owner, owner.getConfig().getDefaultDataSourceName(), ids, fields, values);
    }

    int update${api.name?cap_first}s(IDatabase owner, String dataSourceName, <#if multiPrimaryKey>${api.name?cap_first}PK<#else>${primaryKey.type}</#if>[] ids, Fields fields, Params values) throws Exception;</#if></#if>

    <#if !(api.settings??) || api.settings.enableQuery!true><#if !api.view>default ${api.name?cap_first}VO query${api.name?cap_first}(IDatabase owner, <#if multiPrimaryKey>${api.name?cap_first}PK<#else>${primaryKey.type}</#if> id, Fields excludedFields) throws Exception {
        return query${api.name?cap_first}(owner, owner.getConfig().getDefaultDataSourceName(), id, excludedFields);
    }

    default ${api.name?cap_first}VO query${api.name?cap_first}(IDatabase owner, <#if multiPrimaryKey>${api.name?cap_first}PK<#else>${primaryKey.type}</#if> id) throws Exception {
        return query${api.name?cap_first}(owner, owner.getConfig().getDefaultDataSourceName(), id, null);
    }

    default ${api.name?cap_first}VO query${api.name?cap_first}(IDatabase owner, String dataSourceName, <#if multiPrimaryKey>${api.name?cap_first}PK<#else>${primaryKey.type}</#if> id) throws Exception {
        return query${api.name?cap_first}(owner, dataSourceName, id, null);
    }

    ${api.name?cap_first}VO query${api.name?cap_first}(IDatabase owner, String dataSourceName, <#if multiPrimaryKey>${api.name?cap_first}PK<#else>${primaryKey.type}</#if> id, Fields excludedFields) throws Exception;</#if>

    default IResultSet<${api.name?cap_first}VO> query${api.name?cap_first}s(IDatabase owner, ${api.name?cap_first}Bean queryBean, Fields excludedFields, Page page) throws Exception {
        return query${api.name?cap_first}s(owner, owner.getConfig().getDefaultDataSourceName(), queryBean, null, null, excludedFields, page);
    }

    default IResultSet<${api.name?cap_first}VO> query${api.name?cap_first}s(IDatabase owner, String dataSourceName, ${api.name?cap_first}Bean queryBean, Fields excludedFields, Page page) throws Exception {
        return query${api.name?cap_first}s(owner, dataSourceName, queryBean, null, null, excludedFields, page);
    }

    default IResultSet<${api.name?cap_first}VO> query${api.name?cap_first}s(IDatabase owner, ${api.name?cap_first}Bean queryBean, Cond otherCond, OrderBy orderBy, Fields excludedFields, Page page) throws Exception {
        return query${api.name?cap_first}s(owner, owner.getConfig().getDefaultDataSourceName(), queryBean, otherCond, orderBy, excludedFields, page);
    }

    default IResultSet<${api.name?cap_first}VO> query${api.name?cap_first}s(IDatabase owner, ${api.name?cap_first}Bean queryBean, Page page) throws Exception {
        return query${api.name?cap_first}s(owner, owner.getConfig().getDefaultDataSourceName(), queryBean, null, null, null, page);
    }

    default IResultSet<${api.name?cap_first}VO> query${api.name?cap_first}s(IDatabase owner, String dataSourceName, ${api.name?cap_first}Bean queryBean, Page page) throws Exception {
        return query${api.name?cap_first}s(owner, dataSourceName, queryBean, null, null, null, page);
    }

    default IResultSet<${api.name?cap_first}VO> query${api.name?cap_first}s(IDatabase owner, ${api.name?cap_first}Bean queryBean, Cond otherCond, OrderBy orderBy, Page page) throws Exception {
        return query${api.name?cap_first}s(owner, owner.getConfig().getDefaultDataSourceName(), queryBean, otherCond, orderBy, null, page);
    }

    IResultSet<${api.name?cap_first}VO> query${api.name?cap_first}s(IDatabase owner, String dataSourceName, ${api.name?cap_first}Bean queryBean, Cond otherCond, OrderBy orderBy, Fields excludedFields, Page page) throws Exception;</#if>

    <#if !api.view><#if !(api.settings??) || api.settings.enableRemove!true>default int remove${api.name?cap_first}(IDatabase owner, <#if multiPrimaryKey>${api.name?cap_first}PK<#else>${primaryKey.type}</#if> id) throws Exception {
        return remove${api.name?cap_first}(owner, owner.getConfig().getDefaultDataSourceName(), id);
    }

    int remove${api.name?cap_first}(IDatabase owner, String dataSourceName, <#if multiPrimaryKey>${api.name?cap_first}PK<#else>${primaryKey.type}</#if> id) throws Exception;

    default int remove${api.name?cap_first}s(IDatabase owner, <#if multiPrimaryKey>${api.name?cap_first}PK<#else>${primaryKey.type}</#if>[] ids) throws Exception {
        return remove${api.name?cap_first}s(owner, owner.getConfig().getDefaultDataSourceName(), ids);
    }

    int remove${api.name?cap_first}s(IDatabase owner, String dataSourceName, <#if multiPrimaryKey>${api.name?cap_first}PK<#else>${primaryKey.type}</#if>[] ids) throws Exception;</#if></#if>
}