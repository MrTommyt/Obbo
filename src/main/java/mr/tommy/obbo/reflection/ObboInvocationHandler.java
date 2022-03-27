package mr.tommy.obbo.reflection;

import mr.tommy.obbo.Obbo;
import mr.tommy.obbo.entity.Proxy;
import mr.tommy.obbo.mapping.Resolver;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.StringJoiner;

/**
 * The invocation handler implementation used for the Java Proxy
 * Pattern when a method is invoked from the Proxy instance.
 *
 * <p>
 * This is going to be using the provided {@link #resolver} to
 * resolve the original method from the method invoked from the
 * Proxy instance.
 *
 * @see Resolver
 */
public class ObboInvocationHandler implements InvocationHandler {
    //The resolver of the methods and class names
    private final Resolver resolver;
    //The class of the Proxy interface wrapping the target,
    // all the invoke calls from this invocation handler are
    // meant to be called from this wrapping interface class.
    private final Class<?> wrappingInterface;
    //The class data of the proxied class in the Proxy. Using
    // the Proxy annotation to specify it.
    private final ClassData proxiedClassData;
    //The target object which is wrapped inside the Proxy class.
    // this is where all the final method calls are going to be
    // invoked from.
    private final Object target;
    private final Obbo obbo;

    /**
     * Creates a new instance of the Invocation handler used by the
     * Obbo to resolve methods that can be obfuscated.
     *
     * @param obbo              which is going to be used when a new instance
     *                          (given from example a return method) needs to
     *                          be wrapped
     * @param resolver          used for this Invocation handler to resolve
     *                          the method original name.
     * @param wrappingInterface from where the methods are going
     *                          to be called.
     * @param target            where the parsed method will be invoked from
     */
    public ObboInvocationHandler(Obbo obbo, @NotNull Resolver resolver, Class<?> wrappingInterface, Object target) {
        this.obbo = obbo;
        this.resolver = resolver;
        this.wrappingInterface = wrappingInterface;
        ClassData classData = ClassData.of(wrappingInterface);
        Proxy proxyInfo = classData.annotation(Proxy.class);
        this.proxiedClassData = resolver.resolveClass(proxyInfo.value(), target.getClass().getClassLoader());
        this.target = target;
    }

    /**
     * Creates a new instance of the Invocation handler used by the
     * Obbo to resolve methods that can be obfuscated.
     *
     * @param obbo              which is going to be used when a new instance
     *                          (given from example a return method) needs to
     *                          be wrapped.
     * @param resolver          used for this Invocation handler to resolve
     *                          the method original name.
     * @param wrappingInterface from where the methods are going
     *                          to be called.
     * @param target            where the parsed method will be invoked from
     *                          when resolved.
     * @param loader            to load the classloader.
     */
    public ObboInvocationHandler(Obbo obbo, @NotNull Resolver resolver, Class<?> wrappingInterface, Object target, ClassLoader loader) {
        this.resolver = resolver;
        this.wrappingInterface = wrappingInterface;
        this.obbo = obbo;
        ClassData classData = ClassData.of(wrappingInterface);
        Proxy proxyInfo = classData.annotation(Proxy.class);
        this.proxiedClassData = resolver.resolveClass(proxyInfo.value(), loader);
        this.target = target;
    }

    private Class<?>[] fixParameters(Class<?>[] params) {
        for (int i = 0; i < params.length; i++) {
            Class<?> param = params[i];
            ClassData data = ClassData.of(param);
            Proxy annotation = data.annotation(Proxy.class);
            if (annotation == null)
                continue;

            params[i] = resolver.resolveClass(annotation.value()).getCls();
        }
        return params;
    }

    @Override
    public Object invoke(Object proxy, @NotNull Method method, Object[] args) throws Throwable {
        Method proxyMethod = resolver.resolveMethod(
            proxiedClassData.getCls(),
            wrappingInterface,
            method.getName(),
            fixParameters(method.getParameterTypes())
        );

        if (proxyMethod == null) {
            StringJoiner joiner = new StringJoiner(", ");
            if (args != null)
                for (Object arg : args) joiner.add(arg.getClass().getSimpleName());
            throw new NoSuchMethodError(String.format("method %s(%s) not found on %s(%s)",
                method.getName(),
                joiner,
                target.getClass().getSimpleName(),
                wrappingInterface.getSimpleName()
            ));
        }

        Object result = proxyMethod.invoke(target, args);
        Class<?> rType = method.getReturnType();
        ClassData data = ClassData.of(rType);
        Proxy annotation = data.annotation(Proxy.class);
        if (annotation == null) {
            return result;
        }

        return obbo.wrap(rType, result);
    }
}
