package mr.tommy.obbo.util;

import com.google.gson.Gson;
import mr.tommy.obbo.entity.Proxy;
import mr.tommy.obbo.mapping.Resolver;
import mr.tommy.obbo.reflection.ClassData;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Util class for some things used a lot in the developing of this project.
 */
public class Utils {
    //Global gson instance used for commodity
    private static final Gson gson = new Gson();
    //Global logger instance
    private static final Logger logger = Logger.getLogger("Obbo");

    /**
     * Gets the value of a map and if the value is null or doesn't exist then the supplier
     * is going to be called and the value returned will be stored in the map then
     * returned again by the function.
     *
     * @param map where to get the info from.
     * @param key to try retrieving the value.
     * @param supplier which is going to be called when the value of the map in the
     *                 given key is null.
     * @param <K> the key type of the map and the key which is going to be provided to
     *           retrieve the value.
     * @param <V> the value type of the map, the value retrieved for the map in the
     *           given key and the value returned by the supplier when the value in
     *           the map is null.
     *
     * @return the value in the map at the given key if there is none or null then the
     * value provided by the supplier instead.
     */
    @Nullable
    public static <K, V> V getOrPut(@NotNull Map<K, V> map, @NotNull K key, @NotNull Supplier<V> supplier) {
        V value = map.get(key);
        if (value == null) {
            value = supplier.get();
            map.put(key, value);
        }
        return value;
    }

    /**
     * @return a global Gson instance.
     */
    @NotNull
    public static Gson gson() {
        return gson;
    }

    /**
     * @return the global logger of this library
     */
    @NotNull
    public static Logger logger() {
        return logger;
    }

    /**
     * Fixes the parameters given getting the class they are proxying
     * if they have one
     *
     * @param params to fix.
     * @return the classes we work internally with.
     */
    @Contract("_, _ -> param1")
    public static Class<?>[] fixParameters(@NotNull Class<?>[] params, @NotNull Resolver resolver) {
        for (int i = 0; i < params.length; i++) {
            Class<?> param = params[i];

            //Check if this has a proxy annotation, if it does,
            // then change this parameter to the class they're
            // trying to proxy.
            ClassData data = ClassData.of(param);
            Proxy annotation = data.annotation(Proxy.class);
            if (annotation == null) {
                continue;
            }

            params[i] = resolver.resolveClass(annotation.value()).getCls();
        }
        return params;
    }
}
