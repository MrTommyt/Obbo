package mr.tommy.obbo.reflection;

import mr.tommy.obbo.Obbo;
import mr.tommy.obbo.entity.FieldProxy;
import mr.tommy.obbo.entity.Proxy;
import mr.tommy.obbo.mapping.Resolver;
import mr.tommy.obbo.util.Utils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
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
    // invoked from. Null if static
    @Nullable
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
    public ObboInvocationHandler(Obbo obbo, @NotNull Resolver resolver, Class<?> wrappingInterface, @Nullable Object target) {
        this.obbo = obbo;
        this.resolver = resolver;
        this.wrappingInterface = wrappingInterface;
        ClassData classData = ClassData.of(wrappingInterface);
        Proxy proxyInfo = classData.annotation(Proxy.class);
        if (target == null) {
            this.proxiedClassData = resolver.resolveClass(proxyInfo.value());
        } else {
            this.proxiedClassData = resolver.resolveClass(proxyInfo.value(), target.getClass().getClassLoader());
        }
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
    public ObboInvocationHandler(Obbo obbo, @NotNull Resolver resolver, Class<?> wrappingInterface, @Nullable Object target, ClassLoader loader) {
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
        Class<?>[] params = Utils.fixParameters(pTypes.clone(), resolver);
        String mName = method.getName();
        Class<?> rType = method.getReturnType();

        //Get the cached method of the wrapping method to check their
        // annotations.
        CachedMethod cm = ClassData.of(wrappingInterface).method(MethodDescriptor.of(mName, pTypes));

        //Unwrap the arguments in case they are Obbo proxies
        Object[] unwrappedArgs = args == null ? new Object[0] : args.clone();
        for (int i = 0; i < unwrappedArgs.length; i++) {
            Object arg = unwrappedArgs[i];
            if (!java.lang.reflect.Proxy.isProxyClass(arg.getClass()))
                continue;

            InvocationHandler ih = java.lang.reflect.Proxy.getInvocationHandler(arg);
            if (!(ih instanceof ObboInvocationHandler))
                continue;

            ObboInvocationHandler oih = (ObboInvocationHandler) ih;
            unwrappedArgs[i] = oih.target;
        }

        //Check if the method does have a field proxy annotation.
        // if it does, then return the value inside the given field
        // or set it as the first argument given
        FieldProxy fpAnn = cm.getAnnotation(FieldProxy.class);
        if (fpAnn != null) {
            String value = fpAnn.value();
            Field field = value.isBlank() ? proxiedClassData.field(method.getName())
                : resolver.resolveField(proxiedClassData.getCls(), value);

            if (unwrappedArgs.length > 0) {
                Object arg = unwrappedArgs[0];
                field.set(target, arg);
                return arg;
            } else {
                Object result = field.get(target);
                return wrap0(result, rType);
            }
        }

        //Check if the method does actually have a proxy annotation.
        // if it does, change the name of the method to the name used
        // in the annotation.
        Proxy mpAnn = cm.getAnnotation(Proxy.class);
        if (mpAnn != null) {
            mName = mpAnn.value();
        }

        CachedMethod proxyMethod = resolver.resolveMethod(
            proxiedClassData.getCls(),
            wrappingInterface,
            mName,
            params
        );

        //Method does not exist, throw no such method error
        if (proxyMethod == null) {
            throwMethodNotFound(mName, params, unwrappedArgs);
        }

        //Store the returned object from this method for being
        // later processed.
        Object result = proxyMethod.getMethod().invoke(target, unwrappedArgs);
        return wrap0(result, rType);
    }

    @Contract("null, _ -> null")
    private Object wrap0(Object result, Class<?> rType) {
        if (result == null) {
            return null;
        }

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

    @Contract("_, _, _ -> fail")
    private void throwMethodNotFound(String mName, Class<?>[] params, Object[] args) {
        StringJoiner joiner = new StringJoiner(", ");
        if (args != null)
            for (Class<?> arg : params) joiner.add(arg.getSimpleName());
        throw new NoSuchMethodError(String.format("method %s(%s) not found on %s(%s)",
            mName,
            joiner,
            proxiedClassData.getCls().getSimpleName(),
            wrappingInterface.getSimpleName()
        ));
    }
}
