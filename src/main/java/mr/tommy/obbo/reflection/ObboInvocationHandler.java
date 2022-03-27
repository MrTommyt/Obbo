package mr.tommy.obbo.reflection;

import mr.tommy.obbo.entity.Proxy;
import mr.tommy.obbo.mapping.Resolver;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

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

    /**
     * Creates a new instance of the Invocation handler used by the
     * Obbo to resolve methods that can be obfuscated.
     *
     * @param resolver used for this Invocation handler to resolve
     *                 the method original name.
     * @param wrappingInterface from where the methods are going
     *                          to be called.
     * @param target where the parsed method will be invoked from
     *               when resolved.
     */
    public ObboInvocationHandler(@NotNull Resolver resolver, Class<?> wrappingInterface, Object target) {
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
     * @param resolver used for this Invocation handler to resolve
     *                 the method original name.
     * @param wrappingInterface from where the methods are going
     *                          to be called.
     * @param target where the parsed method will be invoked from
     *               when resolved.
     * @param loader to load the classloader.
     */
    public ObboInvocationHandler(@NotNull Resolver resolver, Class<?> wrappingInterface, Object target, ClassLoader loader) {
        this.resolver = resolver;
        this.wrappingInterface = wrappingInterface;
        ClassData classData = ClassData.of(wrappingInterface);
        Proxy proxyInfo = classData.annotation(Proxy.class);
        this.proxiedClassData = resolver.resolveClass(proxyInfo.value(), loader);
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, @NotNull Method method, Object[] args) throws Throwable {
        Method proxyMethod = resolver.resolveMethod(
                proxiedClassData.getCls(),
                wrappingInterface,
                method.getName(),
                method.getParameterTypes()
        );
        return proxyMethod.invoke(target, args);
    }
}
