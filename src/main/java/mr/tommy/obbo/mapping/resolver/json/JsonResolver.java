package mr.tommy.obbo.mapping.resolver.json;

import mr.tommy.obbo.entity.ProviderRegistry;
import mr.tommy.obbo.mapping.Resolver;
import mr.tommy.obbo.mapping.resolver.Provider;
import mr.tommy.obbo.reflection.ClassData;
import mr.tommy.obbo.reflection.MethodDescriptor;
import mr.tommy.obbo.util.Utils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Main resolver of this project. Is meant to load all the Resolver info
 * based on JSON data.
 *
 * <p>
 * This resolver is going to be called mostly when trying to resolve the
 * {@link Provider providers} of the variables using the {@link JsonProviderFactory}.
 * //TODO: More doc.
 *
 * <pre>
 *     {
 *         "variables": { // Variables are used enclosed between two @
 *              "var1": "value",
 *              "var2": "@var1@.other_stuff", // Can be used on other variables as well.
 *         },
 *         "replacements": { // Replacements going to be used by the {@link Resolver}
 *              "@var1@-@var2@": [ // Variables can be used there as well
 *                  {
 *                      "method": "interfaceMethod", // Interface method name
 *                      "original": "actualMethod" // Actual method name
 *                  }
 *              ]
 *         }
 *     }
 * </pre>
 *
 * @see JsonProviderFactory
 * @see Resolver
 * @see Provider
 */
public class JsonResolver implements Resolver {
    //The resolver info gotten from loading the Json information
    // inside a ResolveInfo class using Gson.
    private final ResolveInfo info;
    //The Provider Factory of this Resolver using both
    // ConstantProviders and DelegatedProviders to provide
    // information when variable values are forced to resolve.
    private final JsonProviderFactory factory = new JsonProviderFactory(this);
    //The Provider Registry of the code-created Providers.
    private final ProviderRegistry registry = new ProviderRegistry();

    /**
     * Creates a JSON resolver from a {@link ResolveInfo} loaded from
     * a {@link com.google.gson.Gson}.
     *
     * @param info contained in the JSON.
     */
    private JsonResolver(ResolveInfo info) {
        this.info = info;
        info.setFactory(factory);
    }

    /**
     * Resolves the ClassData based on the class name provided. Using
     * the {@link ResolveInfo#parseClass(String)} method to parse the class
     * name provided, keeping in mind that this method is prone to receive
     * yet unparsed String with non-resolved variables which the {@link #info}
     * is meant to figure out based on the info on the JSON used.
     *
     * <p>
     * Uses as well the {@link ClassData} to work with the cached Class members
     * instead of fetching them everytime which is considered a very expensive
     * operation.
     *
     * @param className of the class who is going to ge resolved. This param can
     *                  be a yet unparsed String with non-resolved variables
     *                  still in there.
     *
     * @return the ClassData of the given class name, null if not found.
     */
    @Override
    public ClassData resolveClass(String className) {
        return ClassData.of(info.parseClass(className));
    }

    /**
     * Resolves the method using the {@link #info given data} and the
     * {@link ResolveInfo#parseMethod(String, String)}.
     *
     * <p>
     * This method is cached by using the {@link ClassData} where Methods are
     * stored as how they are found the first time they are retrieved.
     *
     * @param targetClass to retrieve the information from.
     * @param wrappingInterface of the interface currently proxying the
     *                          target instance and from where the method
     *                          was requested to be called.
     * @param methodName of the method called from the wrapping interface.
     * @param params the params of the method in the wrapping interface,
     *               which, for obvious reasons should match the base method,
     *               so it's not necessary to ask to specify that.
     *
     * @return the Method of the target class parsed using the information
     * of the method name and the target class given to the ResolverInfo
     * instance.
     */
    @Override
    public Method resolveMethod(Class<?> targetClass, Class<?> wrappingInterface, String methodName, Class<?>... params) {
        ClassData data = ClassData.of(targetClass);
        return data.method(MethodDescriptor.builder(info.parseMethod(methodName, data.getName()))
                .parameterTypes(params).build());
    }

    /**
     * Resolves the field of the given class using the
     *
     * @param cls from where the field is going to be retrieved.
     * @param field from where to get the field from the class when
     *              parsed using the {@link ResolveInfo#parseField(String)}.
     *
     * @return the Field of the parsed field name of the given class.
     */
    @Override
    public Field resolveField(Class<?> cls, String field) {
        return ClassData.of(cls).field(info.parseField(field));
    }

    /**
     * @return the registry of this Resolver.
     */
    @Override
    public ProviderRegistry getRegistry() {
        return registry;
    }

    /**
     * Registers a Provider directly into this JSON resolver. Basically
     * shortens the call to the {@link #getRegistry() registry} and
     * registers the provider to the {@link ProviderRegistry} of this class
     *
     * @param name where to link this provider with.
     * @param provider to register.
     */
    public void registerProvider(String name, Provider provider) {
        registry.registerProvider(name, provider);
    }

    /**
     * Gets the Provider registered at the given variable name. Basically
     * shortens the call to the {@link #getRegistry() registry} and
     * retrieves the provider to the {@link ProviderRegistry} of this class
     *
     * @param name of the variable name to get the Provider from.
     *
     * @return the Provider registered at that variable name, null if none.
     */
    public Provider getRegisteredProvider(String name) {
        return registry.getRegisteredProvider(name);
    }

    /**
     * Creates a new Json resolver based on a reader of the JSON used
     * to parse all the information.
     *
     * @param reader of the JSON who is going to be used to parse the
     *               JsonResolver
     *
     * @return the resolver of the JSON provided as reader.
     */
    @Contract("_ -> new")
    public static @NotNull JsonResolver of(Reader reader) {
        return new JsonResolver(Utils.gson().fromJson(reader, ResolveInfo.class));
    }

    /**
     * @return the Provider Factory of this class.
     */
    public JsonProviderFactory getFactory() {
        return factory;
    }
}
