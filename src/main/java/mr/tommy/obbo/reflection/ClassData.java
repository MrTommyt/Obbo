package mr.tommy.obbo.reflection;

import mr.tommy.obbo.util.Utils;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Holds the Data of a Class and caches in here all their methods
 * and fields for not constantly fetch them which is a quite
 * expensive operation.
 */
public class ClassData {
    //Global cache holding the class data of all the classes having
    // them cached and easily retrieved by the `of` methods.
    private static final Map<String, Class<?>> classCache = Collections.synchronizedMap(new HashMap<>());
    private static final Map<Class<?>, ClassData> data = Collections.synchronizedMap(new HashMap<>());

    //--- Instance cache ---
    //This is the cache used when retrieving the fields, methods,
    // constructors and annotations. This is for easily checking
    // and storing their information and not needing to constantly
    // retrieving the information using reflection. Just once.
    //
    //Note that, using this, if any member is changed at runtime
    // while the previous member is already stored here, there won't
    // be any way to check this change and the ClassData will just
    // continue using wrong information.
    private final Map<String, Field> fieldMap = Collections.synchronizedMap(new WeakHashMap<>());
    private final Map<Class<? extends Annotation>, Annotation> annotationMap =
            Collections.synchronizedMap(new WeakHashMap<>());
    private final Map<MethodDescriptor, CachedMethod> methodMap = Collections.synchronizedMap(new WeakHashMap<>());
    private final Map<MethodDescriptor, Constructor<?>> constructorMap =
            Collections.synchronizedMap(new WeakHashMap<>());

    //--- Instance fields ---
    private final Class<?> cls;
    private final String name;

    /**
     * Constructor of the class data, private because this is only
     * meant to be retrieved from the already cached class data or
     * created if it doesn't exist or created from the {@link #data}
     * cache as well.
     *
     * @param cls of this Class Data.
     */
    private ClassData(@NotNull Class<?> cls) {
        this.cls = cls;
        this.name = cls.getName();
    }

    /**
     * Gets the class enclosed in this instance.
     *
     * @return the class representing this class data.
     */
    public Class<?> getCls() {
        return cls;
    }

    /**
     * Gets the class data of the given class.
     *
     * @param cls the class used to retrieve the class data from.
     *
     * @return the class data of the given class.
     */
    public static ClassData of(Class<?> cls) {
        synchronized (data) {
            return Utils.getOrPut(data, cls, () -> new ClassData(cls));
        }
    }

    /**
     * Gets the class data of the given class name.
     *
     * @param className used to retrieve the class data from the cache.
     *                  If none, then the {@link Class#forName(String)} is
     *                  used to retrieve the Class from and store it in
     *                  the cache.
     *
     * @return the Class Data of the given className.
     */
    public static ClassData of(String className) {
        synchronized (classCache) {
            Class<?> cls = Utils.getOrPut(classCache, className, () -> {
                try {
                    return Class.forName(className);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    return null;
                }
            });
            return of(cls);
        }
    }

    /**
     * Gets the class data of the given class name.
     *
     * @param className used to retrieve the class data from the cache.
     *                  If none, then the {@link Class#forName(String)} is
     *                  used to retrieve the Class from and store it in
     *                  the cache.
     * @param loader the {@link ClassLoader} of the class to load this from.
     *
     * @return the Class Data of the given className.
     */
    public static ClassData of(String className, ClassLoader loader) {
        synchronized (classCache) {
            Class<?> cls = Utils.getOrPut(classCache, className, () -> {
                try {
                    return Class.forName(className, true, loader);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    return null;
                }
            });
            return of(cls);
        }
    }

    /**
     * Gets the Class data of the given object's class checked from
     * the {@link Object#getClass()} method.
     *
     * @param o the object to get the class data from.
     *
     * @return the Class Data of the given object's class.
     */
    public static ClassData of(@NotNull Object o) {
        return of(o.getClass());
    }

    /**
     * Gets the field at that name. If the field is already cached
     * in the {@link #fieldMap} then the field there will be
     * retrieved, if not then the field is going to be retrieved
     * using reflection and saved into the {@link #fieldMap}.
     *
     * @param name of the field to be retrieved from the Class Data.
     *
     * @return the field at that name.
     */
    public Field field(String name) {
        synchronized (fieldMap) {
            return Utils.getOrPut(fieldMap, name, () -> {
                try {
                    Field field;
                    try {
                        field = cls.getField(name);
                    } catch (NoSuchFieldException e) {
                        field = cls.getDeclaredField(name);
                        field.setAccessible(true);
                    }
                    return field;
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                    return null;
                }
            });
        }
    }

    /**
     * Gets the annotation class present in the class. If the annotation
     * is already cached in the {@link #annotationMap} the annotation
     * there will be retrieved, if not then the annotation is going to
     * be retrieved using reflection and saved into the
     * {@link #annotationMap}.
     *
     * @param cls of the annotation that is going to be retrieved
     *            from the class.
     *
     * @return the field at that name.
     */
    public <A extends Annotation> A annotation(@NotNull Class<A> cls) {
        synchronized (annotationMap) {
            return cls.cast(Utils.getOrPut(annotationMap, cls, () -> {
                try {
                    return this.cls.getDeclaredAnnotation(cls);
                } catch (Exception ignored) {
                    return null;
                }
            }));
        }
    }

    /**
     * Gets the method of this class using the given method descriptor,
     * which will contain the useful information for the reflection to
     * get the Method of this class.
     *
     * <p>
     * If the method is already cached in the {@link #methodMap} then
     * the method there is going to be returned, if not then is going to
     * be retrieved from reflection then stored on the {@link #methodMap}
     *
     * @param descriptor of the method which is going to be used to
     *                   retrieve the method instance using the information
     *                   on the method descriptor.
     *
     * @return the method from the class matching the given method
     * descriptor
     */
    public CachedMethod method(MethodDescriptor descriptor) {
        synchronized (methodMap) {
            return Utils.getOrPut(methodMap, descriptor, () -> {
                Method method;
                try {
                    method = cls.getMethod(descriptor.getName(), descriptor.getParamTypes());
                } catch (NoSuchMethodException e) {
                    try {
                        method = cls.getDeclaredMethod(descriptor.getName(), descriptor.getParamTypes());
                        method.setAccessible(true);
                    } catch (NoSuchMethodException ex) {
                        ex.printStackTrace();
                        return null;
                    }
                }
                return new CachedMethod(method);
            });
        }
    }

    /**
     * Gets the constructor of this class using the given parameter types,
     * which will contain the useful information for the reflection to
     * get the Method of this class.
     *
     * <p>
     * If the constructor is already cached in the {@link #constructorMap}
     * then the constructor there is going to be used from it, if not then
     * is going to be retrieved from reflection then stored on the
     * {@link #constructorMap}.
     *
     * @param paramTypes of the constructor which is going to be used to
     *                   retrieve the method instance using the information
     *                   on the method descriptor.
     *
     * @return the constructor from the class matching the given method
     * descriptor
     */
    public Constructor<?> constructor(Class<?>... paramTypes) {
        synchronized (constructorMap) {
            return Utils.getOrPut(constructorMap, MethodDescriptor.builder("<init>")
                    .parameterTypes(paramTypes)
                    .build(), () -> {
                try {
                    Constructor<?> constructor;
                    try {
                        constructor = cls.getConstructor(paramTypes);
                    } catch (NoSuchMethodException e) {
                        constructor = cls.getDeclaredConstructor(paramTypes);
                        constructor.setAccessible(true);
                    }
                    return constructor;
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                    return null;
                }
            });
        }
    }

    /**
     * Gets the name of the class represented by this instance.
     *
     * @return the name of the class stored in this instance.
     */
    public String getName() {
        return name;
    }
}
