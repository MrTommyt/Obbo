package mr.tommy.obbo.mapping.resolver;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Represents a provider of a value, used mostly with the
 * variables when resolving an obfuscated name.
 */
public interface Provider {

    /**
     * Gets the value this is meant to provide. This
     * is linked to a specific variable and every time that
     * variable is found, this method is going to be called
     * and replaced with the variable's invocation.
     *
     * @return the value meant to be provided by this Provider
     * instance.
     */
    String get();

    /**
     * Represents the retention type this Provider is going to
     * be used, if {@link RetentionType#CACHED} then the
     * {@link #get()} method is only called once and stored in a cache
     * for further requests, if {@link RetentionType#LAZY} otherwise
     * then the {@link #get()} method is going to be called always.
     *
     * @return the Retention policy the provided value must follow.
     */
    RetentionType retentionType();

    /**
     * Basic Provider implementation used for easily create a Provider
     * for a resolver.
     *
     * <p>
     * Use any of the {@link #of(Supplier, RetentionType) of} methods
     * to easily create a Provider implementation with the given
     * parameters.
     */
    class ProviderImpl implements Provider {
        private final Supplier<String> value;
        private final RetentionType type;

        public ProviderImpl(Supplier<String> value, RetentionType type) {
            this.value = value;
            this.type = type;
        }

        public ProviderImpl(String value, RetentionType type) {
            this.value = () -> value;
            this.type = type;
        }

        @Override
        public String get() {
            return value.get();
        }

        @Override
        public RetentionType retentionType() {
            return type;
        }
    }

    /**
     * Creates a provider with the given string supplier and the
     * given RetentionType.
     *
     * @param supplier which will return the string value the
     *                 Provider will always use on {@link #get()}.
     * @param type of the retention of this value.
     *
     * @return the provider of the given supplier and the given
     * retention type.
     */
    @Contract(value = "_, _ -> new", pure = true)
    static @NotNull Provider of(Supplier<String> supplier, RetentionType type) {
        return new ProviderImpl(supplier, type);
    }

    /**
     * Creates a provider of a fixed value with a
     * {@link RetentionType#CACHED cached} retention value.
     *
     * @param string the value this provider will always return.
     *
     * @return the provider of the given fixed value.
     */
    @Contract(value = "_ -> new", pure = true)
    static @NotNull Provider of(String string) {
        return new ProviderImpl(string, RetentionType.CACHED);
    }

    /**
     * Creates a provider with the given string supplier and
     * {@link RetentionType#LAZY lazy} {@link RetentionType}.
     *
     * @param supplier which will return the string value the
     *                 Provider will always use on {@link #get()}.
     *
     * @return the provider of the given supplier and
     * {@link RetentionType#LAZY lazy} {@link RetentionType}.
     */
    @Contract(value = "_ -> new", pure = true)
    static @NotNull Provider of(Supplier<String> supplier) {
        return new ProviderImpl(supplier, RetentionType.LAZY);
    }
}
