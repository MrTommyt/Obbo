package mr.tommy.obbo.util;

import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Util class for some things used a lot in the developing of this project.
 */
public class Utils {
    //Global gson instance used for commodity
    private static final Gson gson = new Gson();

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
    public static <K, V> V getOrPut(@NotNull Map<K, V> map, K key, Supplier<V> supplier) {
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
    public static Gson gson() {
        return gson;
    }
}
