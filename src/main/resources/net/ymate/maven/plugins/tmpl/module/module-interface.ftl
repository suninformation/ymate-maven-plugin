package ${packageName};

import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.beans.annotation.Ignored;
import net.ymate.platform.core.support.IDestroyable;
import net.ymate.platform.core.support.IInitialization;

/**
 * I${moduleName?cap_first} generated By ModuleMojo on ${.now?string("yyyy/MM/dd HH:mm")}
 *
 * @author YMP (https://www.ymate.net/)
 */
@Ignored
public interface I${moduleName?cap_first} extends IInitialization<IApplication>, IDestroyable {

    String MODULE_NAME = "module.${moduleName?lower_case}";

    /**
     * 获取所属应用容器
     *
     * @return 返回所属应用容器实例
     */
    IApplication getOwner();

    /**
     * 获取配置
     *
     * @return 返回配置对象
     */
    I${moduleName?cap_first}Config getConfig();
}
