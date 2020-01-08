package ${config.packageName}.${config.classSuffix?lower_case};

import net.ymate.platform.core.persistence.base.IEntity;

import java.io.Serializable;

/**
 * BaseEntity generated By EntityMojo on ${lastUpdateTime?string("yyyy/MM/dd HH:mm:ss")}
 *
 * @author YMP (https://www.ymate.net/)
 */
public abstract class BaseEntity<PK extends Serializable> implements IEntity<PK> {

	private static final long serialVersionUID = 1L;

	public BaseEntity() {
	}
}
