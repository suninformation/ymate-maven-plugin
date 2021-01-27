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
package ${app.packageName};

import ${app.packageName}.repository.impl.*;
import net.ymate.platform.core.annotation.EnableAutoScan;
import net.ymate.platform.core.annotation.EnableBeanProxy;
import net.ymate.platform.core.annotation.EnableDevMode;
import net.ymate.platform.test.YMPJUnit4Suite;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * RepositoryTestSuite generated By CrudMojo on ${.now?string("yyyy/MM/dd a HH:mm")}
 *
 * @author ${app.author!"YMP (https://www.ymate.net/"}
 * @version ${app.version!"1.0.0"}
 */
@RunWith(YMPJUnit4Suite.class)
@Suite.SuiteClasses({<#list app.apis as p>
    ${p.name?cap_first}RepositoryTest.class<#if p_has_next>, </#if></#list>
})
@EnableAutoScan
@EnableBeanProxy
@EnableDevMode
public class RepositoryTestSuite {

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
}