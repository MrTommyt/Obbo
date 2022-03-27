package mr.tommy.obbo.reflection;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a method descriptor. Used to cache the methods
 * inside hash maps using their information. Making use of the
 * {@link #hashCode()} to retrieve the hash, based on the param
 * types.
 *
 * <p>
 * The {@link #hashCode()} implementation takes into account the
 * name and parameter to create the hash making it useful for
 * checking the information from {@link java.util.HashMap hashMaps}
 * easily.
 */
public class MethodDescriptor {
    //The name of the method.
    private final String name;
    //The parameter type classes of this method descriptor.
    private final Class<?>[] paramTypes;
    //Cached hash code. Since this class is immutable, this will
    // always remain the same so no need to constantly check the
    // hash code more than once.
    private boolean hashed;
    private int _hash;

    /**
     * Creates a method descriptor with the given name and
     * parameter types.
     *
     * @param name of the method.
     * @param paramTypes of the method descriptor.
     */
    public MethodDescriptor(String name, Class<?>... paramTypes) {
        this.name = name;
        this.paramTypes = paramTypes;
    }

    /**
     * Gets the name of the method this instance is describing.
     *
     * @return the name of this method descriptor.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the parameter types of the method this instance is
     * describing.
     *
     * @return the parameter types of this method descriptor.
     */
    public Class<?>[] getParamTypes() {
        return paramTypes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MethodDescriptor)) return false;
        MethodDescriptor that = (MethodDescriptor) o;
        return getName().equals(that.getName())
                && Arrays.equals(getParamTypes(), that.getParamTypes());
    }

    /**
     * @return the hashCode of this method.
     */
    private int hashCode0() {
        int result = Objects.hash(getName());
        result = 31 * result + Arrays.hashCode(getParamTypes());
        hashed = true;
        return result;
    }

    @Override
    public int hashCode() {
        return hashed ? _hash : (_hash = hashCode0());
    }

    /**
     * The builder of
     */
    public static class Builder {
        private String name;
        private Class<?>[] paramTypes;

        public Builder(String name) {
            this.name = name;
        }

        /**
         * Sets the parameter types of the method descriptor going to
         * be built.
         *
         * @param paramTypes of the method descriptor which is going
         *                   to be built.
         *
         * @return this builder instance.
         */
        public Builder parameterTypes(Class<?>... paramTypes) {
            this.paramTypes = paramTypes;
            return this;
        }

        /**
         * Sets the name of this method.
         *
         * @param name the name to be set for the method descriptor.
         *
         * @return this builder instance.
         */
        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        /**
         * Builds the method descriptor based on the fields provided to
         * the builder.
         *
         * @return a new instance of the method descriptor using the fields
         * stored in here.
         */
        public MethodDescriptor build() {
            return new MethodDescriptor(name, paramTypes);
        }
    }

    /**
     * Creates a new Method Descriptor builder based on the name
     * given.
     *
     * @param name of the method describing.
     *
     * @return a builder of the method descriptor.
     */
    @Contract("_ -> new")
    public static @NotNull Builder builder(String name) {
        return new Builder(name);
    }

    /**
     * Creates a new Method Descriptor builder based on the name
     * given.
     *
     * @param name of the method describing.
     * @param params parameters of the method.
     *
     * @return a builder of the method descriptor.
     */
    public static @NotNull MethodDescriptor of(String name, Class<?> ...params) {
        return new Builder(name).parameterTypes(params).build();
    }
}
