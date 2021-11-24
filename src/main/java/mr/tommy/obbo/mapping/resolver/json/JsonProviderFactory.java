package mr.tommy.obbo.mapping.resolver.json;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import mr.tommy.obbo.mapping.Resolver;
import mr.tommy.obbo.mapping.resolver.Provider;
import mr.tommy.obbo.mapping.resolver.ProviderFactory;
import mr.tommy.obbo.mapping.resolver.RetentionType;
import mr.tommy.obbo.reflection.ClassData;
import mr.tommy.obbo.reflection.MethodDescriptor;
import mr.tommy.obbo.util.Utils;

import java.lang.reflect.InvocationTargetException;
import java.util.stream.Stream;

public class JsonProviderFactory extends ProviderFactory<JsonElement> {
    private final Resolver resolver;

    public JsonProviderFactory(Resolver resolver) {
        super(resolver);
        this.resolver = resolver;
    }

    private static class ConstantProvider implements Provider {
        private final String value;

        private ConstantProvider(String value) {
            this.value = value;
        }

        @Override
        public String get() {
            return value;
        }

        @Override
        public RetentionType retentionType() {
            return RetentionType.CACHED;
        }
    }

    private enum DelegateType {
        // Default
        @SerializedName("provider") PROVIDER,
        // Probably going to be removed?
        @SerializedName("registered") REGISTERED,
        @SerializedName("static") STATIC_METHOD
    }

    private class DelegatedProvider implements Provider {
        private String provider;
        private RetentionType retention = RetentionType.CACHED;
        private String value;
        private DelegateType type = DelegateType.PROVIDER;
        private String[] params;
        private String _value;
        private Class<?>[] _params;

        private String get0() {
            ClassData data = ClassData.of(resolver.resolveClass(provider));
            if (type == DelegateType.REGISTERED) {
                Provider provider = resolver.getRegistry().getRegisteredProvider(value);
                if (provider == null)
                    return null;

                return provider.get();
            } else if (type == DelegateType.PROVIDER) {
                try {
                    Provider instance = (Provider) data.constructor().newInstance();
                    return instance.get();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            } else if (type == DelegateType.STATIC_METHOD) {
                try {
                    return (String) data.method(MethodDescriptor.builder(value)
                            .parameterTypes(params())
                            .build()).invoke(null);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        private Class<?>[] params() {
            return _params != null ? _params : (_params = Stream.of(params)
                    .map(p -> ClassData.of(p).getCls())
                    .toArray(Class[]::new));
        }

        @Override
        public String get() {
            return retention != RetentionType.CACHED || _value == null
                    ? (_value = get0()) : _value;
        }

        @Override
        public RetentionType retentionType() {
            return retention;
        }
    }

    @Override
    public Provider from(JsonElement element) {
        if (element.isJsonPrimitive())
            return new ConstantProvider(element.getAsString());

        return Utils.gson().fromJson(element, DelegatedProvider.class);
    }
}
