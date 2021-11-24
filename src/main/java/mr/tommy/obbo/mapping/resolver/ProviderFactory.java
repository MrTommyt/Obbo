package mr.tommy.obbo.mapping.resolver;

import mr.tommy.obbo.mapping.Resolver;

/**
 * Factory which is going to create {@link Provider
 * providers} based on the value given in the
 * {@link #from(Object)} method.
 *
 * @param <T> value which is going to create the
 *           provider from.
 *
 */
public abstract class ProviderFactory<T> {
    //The resolver of this instance.
    private final Resolver resolver;

    /**
     * Provider Factory constructor, used only when creating
     * a subclass of this one.
     *
     * @param resolver which is going to be used when creating
     *                 a provider instance of the resolver.
     */
    protected ProviderFactory(Resolver resolver) {
        this.resolver = resolver;
    }

    /**
     * Is meant to create a provider based on the given type
     * as parameter.
     *
     * @param type which is going to be used when creating the
     *             provider.
     *
     * @return the provider from the given object.
     */
    public abstract Provider from(T type);

    /**
     * Gets the resolver of this instance, used when creating
     * this factory.
     *
     * @return the resolver of this instance.
     */
    public Resolver getResolver() {
        return resolver;
    }
}
