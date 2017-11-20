package tigase.iot.framework.devices.annotations;

import java.lang.annotation.*;

/**
 * Annotation defines that a field annotated with <code>@ConfigField</code> annotation is not visible in a configuration
 * form.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Hidden {

}