package mr.tommy.obbo.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used for the Proxy interfaces to specify which class they are actually proxying.
 * <strong>This annotation is mandatory</strong> in order to make the process work
 * correctly
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Proxy {
    /**
     * @return the value of the class the interface is meant to proxy, supports
     * {@link mr.tommy.obbo.mapping.Resolver resolver mapping}.
     */
    String value();
}
