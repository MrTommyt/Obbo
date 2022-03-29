package mr.tommy.obbo.mapping;

import mr.tommy.obbo.entity.ProviderRegistry;
import mr.tommy.obbo.entity.Proxy;
import mr.tommy.obbo.reflection.CachedMethod;
import mr.tommy.obbo.reflection.ClassData;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Represents a Resolver of the variable names used in different
 * places like the {@link Proxy#value()}.
 *
 * <p>
 * This class is one of the important axis of this project since
 * the principal role of deobfuscation is done by this class
 * resolving the Class & Method names taking into account the
 * variables resolved.
 */
public interface Resolver {
    /**
     * Resolves the {@link ClassData} of the parsed class name.
     *
     * @param className of the class who is going to be used.
     *                  is meant to Support variables inside them.
     *
     * @return the Class Data of the parsed class name given.
     */
    ClassData resolveClass(String className);

    /**
     * Resolves the {@link ClassData} of the parsed class name.
     *
     * @param className of the class who is going to be used.
     *                  is meant to Support variables inside them.
     *
     * @return the Class Data of the parsed class name given.
     */
    ClassData resolveClass(String className, ClassLoader loader);

    /**
     * Resolves the method of the target class with the given parameters
     * using the providers and
     *
     * @param targetClass owning the method which is going to be called.
     * @param wrappingInterface class which is Proxying the target
     *                          class and where the method was requested
     *                          to be called from.
     * @param methodName of the method which is going to be called. By
     *                   default, is the method's name which was called
     *                   from the wrapping interface using the
     *                   {@link mr.tommy.obbo.reflection.ObboInvocationHandler}
     * @param params given when calling the method of the wrapping
     *               interface.
     *
     * @return the method of the given method name resolved with the
     * passed parameters.
     */
    CachedMethod resolveMethod(Class<?> targetClass, Class<?> wrappingInterface, String methodName, Class<?>... params);

    /**
     * Resolves the field of the given field name from the class.
     * Supports variable resolution.
     *
     * @param cls where to resolve the field from.
     * @param field which is going to be used to retrieve from the
     *              given class. Having also into account that the
     *              field supports variable resolution.
     *
     * @return the field of the given class.
     */
    Field resolveField(Class<?> cls, String field);

    /**
     * Gets the provider registry which is going to be used for
     * this resolver. Used to register code-created
     * {@link mr.tommy.obbo.mapping.resolver.Provider providers}.
     *
     * @return the ProviderRegistry used by this resolver
     */
    ProviderRegistry getRegistry();
}
