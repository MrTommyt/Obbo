package mr.tommy.obbo.reflection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

/**
 * Instance which is going to be used to wrap the method in order
 * to allow caching some values of it and perform as well as possible
 */
public class CachedMethod {
    //Encapsulated method from where to access the method
    // metadata instead of calling it separately every time
    private final Method method;
    //Cache maps
    private boolean cachedAnnotations = false;
    private final Map<Class<? extends Annotation>, Annotation> annotationMap =
        Collections.synchronizedMap(new WeakHashMap<>());

    /**
     * Creates a new cached method with the given method.
     *
     * @param method to be wrapped.
     */
    CachedMethod(@NotNull Method method) {
        this.method = method;
    }

    /**
     * Gets the {@link #method} wrapped inside this class.
     *
     * @return the method this class represents.
     */
    @NotNull
    public Method getMethod() {
        return method;
    }

    /**
     * Ensures the annotations are loaded properly before continue
     */
    private void ensureCachedAnnotations() {
        synchronized (this) {
            if (cachedAnnotations) {
                return;
            }
            for (Annotation annotation : method.getDeclaredAnnotations()) {
                annotationMap.put(annotation.annotationType(), annotation);
            }
            cachedAnnotations = true;
        }
    }

    /**
     * Gets the given annotation inside this method, if none then
     * null is returned.
     *
     * @param cls of the annotation to retrieve.
     * @param <A> type of the annotation.
     *
     * @return the given annotation if found. None if not.
     */
    @Nullable
    public <A extends Annotation> A getAnnotation(Class<A> cls) {
        ensureCachedAnnotations();
        Annotation ann = annotationMap.get(cls);
        if (ann == null) return null;
        return cls.cast(ann);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CachedMethod)) return false;
        CachedMethod that = (CachedMethod) o;
        return getMethod().equals(that.getMethod());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMethod());
    }
}
