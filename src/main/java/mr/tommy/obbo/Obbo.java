package mr.tommy.obbo;

import mr.tommy.obbo.mapping.Resolver;
import mr.tommy.obbo.mapping.resolver.Provider;
import mr.tommy.obbo.reflection.ClassData;
import mr.tommy.obbo.reflection.ObboInvocationHandler;
import mr.tommy.obbo.util.Utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.StringJoiner;

/**
 * This is the main class of the Project. If you want to start using
 * Obbo, you should start creating an instance of Obbo for your project.
 *
 * <p>
 * This class lets you {@link #wrap(Class, Object) wrap} an object inside
 * an interface controlling its behavior. This is very useful when working on
 * projects that are obfuscated making you don't want to work directly with
 * names or packages that can vary between versions.
 *
 * <p>
 * When wrapping a class, you will be able to interact with the class using
 * the interface class provided in the {@link #wrap(Class, Object) wrap
 * method} and the {@link Proxy Java Proxy Pattern} so take into account
 * that some platforms like Android for example, may not support this library
 * at all.
 *
 * <p>
 * Methods will be resolved using the {@link Resolver}. <strong>Note that
 * the interface must be {@link mr.tommy.obbo.entity.Proxy annotated} declaring
 * the Class it is meant to proxy</strong>. This name also supports resolution
 * by the {@link Resolver}. If you want to make your own resolution for some
 * variable names you can implement the {@link mr.tommy.obbo.mapping.resolver.Provider
 * provider interface} and register it using the
 * {@link mr.tommy.obbo.entity.ProviderRegistry#registerProvider(String, Provider)
 * ProviderRegistry method} located in the {@link Resolver#getRegistry()}.
 *
 * @see Proxy
 * @see Resolver
 * @see Provider
 */
public class Obbo {
    //The resolver this instance is going to be using for
    // parsing the Classes and Method names.
    private final Resolver resolver;

    /**
     * Creates a new Obbo instance.
     *
     * @param resolver which is going to resolve the variables used when declaring
     *                 different Classes and Method mappings.
     */
    public Obbo(Resolver resolver) {
        this.resolver = resolver;
    }

    /**
     * Wraps the passed object as a target inside the wrapping interface provided.
     *
     * <p>
     * this method uses their respective wrapping interface class'
     * {@link ClassLoader} for the {@link Proxy proxy instance} and the target
     * class' {@link ClassLoader} for the invocation handler.
     *
     * @param wrappingInterface the interface to use as proxy and control the
     *                          target with.
     * @param target            instance to wrap inside the interface and where the methods
     *                          will end up being called from.
     * @param <I>               the interface which is going to act as the target instance's
     *                          controller.
     * @return a {@link Proxy} of the target as the wrapping interface provided
     * encapsulating all their methods.
     */
    public <I> I wrap(Class<I> wrappingInterface, Object target) {
        Object instance = Proxy.newProxyInstance(wrappingInterface.getClassLoader(), new Class[]{wrappingInterface},
            new ObboInvocationHandler(this, resolver, wrappingInterface, target));
        return wrappingInterface.cast(instance);
    }

    /**
     * Wraps the passed object as a target inside the wrapping interface provided.
     *
     * @param loader            to load the Proxy instance from.
     * @param wrappingInterface the interface to use as proxy and control the
     *                          target with.
     * @param target            instance to wrap inside the interface and where the methods
     *                          will end up being called from.
     * @param <I>               the interface which is going to act as the target instance's
     *                          controller.
     * @return a {@link Proxy} of the target as the wrapping interface provided
     * encapsulating all their methods.
     */
    public <I> I wrap(ClassLoader loader, Class<I> wrappingInterface, Object target) {
        Object instance = Proxy.newProxyInstance(loader, new Class[]{wrappingInterface},
            new ObboInvocationHandler(this, resolver, wrappingInterface, target, loader));
        return wrappingInterface.cast(instance);
    }

    /**
     * Wraps the passed object as a target inside the wrapping interface provided.
     *
     * @param loader             to load the Proxy instance from.
     * @param handlerClassloader to load the invocation handler instance from.
     * @param wrappingInterface  the interface to use as proxy and control the
     *                           target with.
     * @param target             instance to wrap inside the interface and where the methods
     *                           will end up being called from.
     * @param <I>                the interface which is going to act as the target instance's
     *                           controller.
     * @return a {@link Proxy} of the target as the wrapping interface provided
     * encapsulating all their methods.
     */
    public <I> I wrap(ClassLoader loader, ClassLoader handlerClassloader, Class<I> wrappingInterface, Object target) {
        Object instance = Proxy.newProxyInstance(loader, new Class[]{wrappingInterface},
            new ObboInvocationHandler(this, resolver, wrappingInterface, target, handlerClassloader));
        return wrappingInterface.cast(instance);
    }

    /**
     * Creates a new instance of the proxied class by the wrapping interface and
     * returned wrapped inside the given wrapping interface.
     *
     * @param wrappingInterface class to wrap the new instance to and to get the
     *                          internal proxied class from the
     *                          {@link mr.tommy.obbo.entity.Proxy proxy annotation}.
     *                          an {@link IllegalArgumentException} will be thrown
     *                          if the wrapping interface does not have the proxy
     *                          annotation.
     * @param paramTypes        of the constructor to be using to look it from.
     * @param args              to call the constructor.
     * @param <I>               the wrapping interface type to wrap the returned
     *                          instance created from the constructor to.
     * @return a new instance of the proxied class by the wrapping interface encapsulated
     * inside the wrapping interface.
     * @throws IllegalArgumentException if the wrapping interface does not have the
     *                                  {@link mr.tommy.obbo.entity.Proxy proxy annotation}.
     * @throws RuntimeException         if the constructor with the given parameter types
     *                                  was not found.
     * @throws IllegalStateException    if an error occurs when instantiating the class
     *                                  from the constructor
     * @see mr.tommy.obbo.entity.Proxy
     */
    public <I> I newInstance(Class<I> wrappingInterface, Class<?>[] paramTypes, Object... args) {
        //Get proxied class
        ClassData classData = ClassData.of(wrappingInterface);
        mr.tommy.obbo.entity.Proxy proxyInfo = classData.annotation(mr.tommy.obbo.entity.Proxy.class);
        if (proxyInfo == null) {
            throw new IllegalArgumentException("Wrapping interface does not have the proxy annotation");
        }

        //Get the constructor from the given paramTypes
        Utils.fixParameters(paramTypes, resolver);
        ClassData proxiedClass = resolver.resolveClass(proxyInfo.value());
        Constructor<?> constructor = proxiedClass.constructor(paramTypes);
        if (constructor == null) {
            StringJoiner joiner = new StringJoiner(", ");
            throw new RuntimeException(
                String.format("constructor(%s) not found when instantiating %s", joiner, wrappingInterface));
        }

        //Attempts to instantiate the given instance using its constructor,
        // runtime exception will be thrown if turns out not being possible.
        Object instance;
        try {
            constructor.setAccessible(true);
            instance = constructor.newInstance(args);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Instance could not be instantiated", e);
        }
        return wrap(wrappingInterface, instance);
    }
}
