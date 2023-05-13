/*
 * Copyright 2007-2023 the original author or authors.
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
package net.ymate.maven.plugins.support;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.persistence.jdbc.scaffold.INamedFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.logging.SystemStreamLog;

/**
 * 将汉字转换为拼音命名过滤器（从旧版本中移植）
 *
 * @author 刘镇 (suninformation@163.com) on 17/4/18 上午9:54
 * @since 1.0.2
 */
public class ChinesePinyinNamedFilter implements INamedFilter {

    public String filter(Type type, String original) {
        String returnValue = null;
        try {
            HanyuPinyinOutputFormat outputFormat = new HanyuPinyinOutputFormat();
            outputFormat.setVCharType(HanyuPinyinVCharType.WITH_V);
            outputFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
            outputFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
            //
            returnValue = PinyinHelper.toHanYuPinyinString(original, outputFormat, "_", true);
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            new SystemStreamLog().warn(String.format("Exception occurred while processing field name '%s' for Type '%s': %s", original, type, e.getMessage()), RuntimeUtils.unwrapThrow(e));
        }
        return StringUtils.defaultIfBlank(returnValue, original);
    }
}
