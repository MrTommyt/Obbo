package mr.tommy.obbo;

import mr.tommy.obbo.mapping.Resolver;
import mr.tommy.obbo.mapping.resolver.Provider;
import mr.tommy.obbo.reflection.ObboInvocationHandler;

import java.lang.reflect.Proxy;

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
}
