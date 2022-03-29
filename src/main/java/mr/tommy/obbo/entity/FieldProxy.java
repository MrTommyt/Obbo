package mr.tommy.obbo.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Looks up for the value of the field with the given
 * {@link #value() name}. Inside a {@link Proxy} class.
 *
 * <p>
 * This Annotation is meant to be used in a method with no
 * parameters to get the value of the field or with one parameter
 * to set the value of the first argument to the field named the
 * same as the method name or {@link #value()} if this is
 * different of the default value.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldProxy {

    /**
     * Value of the field to be using, if this is empty then
     * the method name will be used to look up for the field
     * name as well.
     *
     * @return the field name to look up for.
     */
    String value() default "";

}
