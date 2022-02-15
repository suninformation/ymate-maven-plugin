<#setting number_format="#">
/*
 * Copyright 2007-2022 the original author or authors.
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
;(function ($) {

    "use strict";

    function __processBasePath(basePath, originUrl) {
        if (basePath && originUrl) {
            originUrl = basePath + originUrl;
        }
        return originUrl;
    }

    function __getSidebarItems(pagePath) {
        return [
            {type: 'header', title: 'MAIN NAVIGATION'},
            <#list navMap?keys as key>{
                title: '${navMap['${key}']}',
                icon: '',
                href: __processBasePath(pagePath, '${key}'),
                items: []
            }, </#list>
        ];
    }

    function __getNavMenuItems(basePath) {
        return [
            {type: 'push-menu'}
        ];
    }

    function __getNavActionItems(basePath, options) {
        var opts = options || {};
        return [
            {type: 'dropdown', title: opts.noNewMsgText || '没有新通知', icon: 'bell', href: '#', label: {color: 'warning', text: '8'}, footer: opts.seeAllText || '查看全部'},
            {
                type: 'user-menu',
                title: opts.userName || 'NO NAME',
                description: opts.userDescription || '',
                icon: opts.userAvatar || __processBasePath(basePath, 'assets/commons/img/avatar-none.jpg'),
                footer: {
                    profileHref: opts.settingsUrl || '#',
                    profileText: opts.settingsText || '设置',
                    signOutHref: opts.logoutUrl || '#',
                    signOutText: opts.logoutText || '退出'
                }
            },
            {type: 'theme-switch'},
            {type: 'fullscreen'},
            {type: 'control-sidebar'},
        ];
    }

    window.__commons = {
        processBasePath: __processBasePath,
        sidebarItems: __getSidebarItems,
        navMenuItems: __getNavMenuItems,
        navActionItems: __getNavActionItems,
    }
})(jQuery);