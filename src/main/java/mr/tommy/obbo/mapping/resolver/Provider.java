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

    class ProviderImpl implements Provider {
        private Supplier<String> value;
        private RetentionType type;

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

    @Contract(value = "_, _ -> new", pure = true)
    static @NotNull Provider of(String string, RetentionType type) {
        return new ProviderImpl(string, type);
    }

    @Contract(value = "_, _ -> new", pure = true)
    static @NotNull Provider of(Supplier<String> string, RetentionType type) {
        return new ProviderImpl(string, type);
    }

    @Contract(value = "_ -> new", pure = true)
    static @NotNull Provider of(String string) {
        return new ProviderImpl(string, RetentionType.CACHED);
    }

    @Contract(value = "_ -> new", pure = true)
    static @NotNull Provider of(Supplier<String> string) {
        return new ProviderImpl(string, RetentionType.LAZY);
    }
}
