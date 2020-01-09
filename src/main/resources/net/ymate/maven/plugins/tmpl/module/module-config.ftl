package ${packageName};

import net.ymate.platform.core.beans.annotation.Ignored;
import net.ymate.platform.core.support.IInitialization;

/**
 * I${moduleName?cap_first}Config generated By ModuleMojo on ${.now?string("yyyy/MM/dd HH:mm")}
 *
 * @author YMP (https://www.ymate.net/)
 */
@Ignored
public interface I${moduleName?cap_first}Config extends IInitialization<I${moduleName?cap_first}> {

    String ENABLED = "enabled";

    /**
     * 模块是否已启用, 默认值: true
     *
     * @return 返回false表示禁用
     */
    boolean isEnabled();
}