package mr.tommy.obbo.reflection;

import mr.tommy.obbo.Obbo;
import mr.tommy.obbo.entity.Proxy;
import mr.tommy.obbo.mapping.Resolver;
import mr.tommy.obbo.util.Utils;
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
    //Used when it's necessary to wrap another instance inside
    // another proxy class.
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
        this.target = target;

        //Get proxied class
        ClassData classData = ClassData.of(wrappingInterface);
        Proxy proxyInfo = classData.annotation(Proxy.class);
        this.proxiedClassData = resolver.resolveClass(proxyInfo.value(), loader);
    }

    @Override
    public Object invoke(Object proxy, @NotNull Method method, Object[] args) throws Throwable {
        Class<?>[] pTypes = method.getParameterTypes();
        Class<?>[] params = Utils.fixParameters(pTypes, resolver);
        String mName = method.getName();

        //Check if the method does actually have a proxy annotation.
        // if it does, change the name of the method to the name used
        // in the annotation.
        CachedMethod cm = this.proxiedClassData.method(MethodDescriptor.of(mName, pTypes));
        Proxy mpAnn = cm.getAnnotation(Proxy.class);
        if (mpAnn != null) {
            mName = mpAnn.value();
        }

        Method proxyMethod = resolver.resolveMethod(
            proxiedClassData.getCls(),
            wrappingInterface,
            mName,
            params
        );

        //Method does not exist, throw no such method error
        if (proxyMethod == null) {
            StringJoiner joiner = new StringJoiner(", ");
            if (args != null)
                for (Object arg : params) joiner.add(arg.getClass().getSimpleName());
            throw new NoSuchMethodError(String.format("method %s(%s) not found on %s(%s)",
                mName,
                joiner,
                target.getClass().getSimpleName(),
                wrappingInterface.getSimpleName()
            ));
        }

        //Store the returned object from this method for being
        // later processed.
        Object result = proxyMethod.invoke(target, args);
        Class<?> rType = method.getReturnType();

        //First check if the return type of the method in the
        // proxy class is another proxy, so we wrap the returned
        // object inside it. If not, just return the normal object
        ClassData data = ClassData.of(rType);
        Proxy annotation = data.annotation(Proxy.class);
        if (annotation == null) {
            return result;
        }

        return obbo.wrap(rType, result);
    }
}
