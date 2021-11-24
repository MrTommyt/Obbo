package mr.tommy.obbo.entity;

import mr.tommy.obbo.mapping.resolver.Provider;

import java.util.HashMap;
import java.util.Map;

/**
 * Class where the code-created providers are going to be registered and retrieved
 * in each instance of {@link mr.tommy.obbo.mapping.Resolver}.
 */
public class ProviderRegistry {
    //Internal map of the providers registered.
    private final Map<String, Provider> providers = new HashMap<>();

    /**
     * Gets the provided registered for that variable name.
     *
     * @param name of the variable associated with the given provider.
     *
     * @return the provider registered at to the given variable key.
     */
    public Provider getRegisteredProvider(String name) {
        return providers.get(name);
    }

    /**
     * Registered the given provided with the specified variable name.
     *
     * @param name of the variable to link the provider with
     * @param provider which is going to be registered.
     */
    public void registerProvider(String name, Provider provider) {
        providers.put(name, provider);
    }
}
