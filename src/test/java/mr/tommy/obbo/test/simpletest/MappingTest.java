package mr.tommy.obbo.test.simpletest;

import mr.tommy.Obbo;
import mr.tommy.obbo.mapping.resolver.Provider;
import mr.tommy.obbo.mapping.resolver.RetentionType;
import mr.tommy.obbo.mapping.resolver.json.JsonResolver;
import mr.tommy.obbo.test.simpletest.v1.C1;
import mr.tommy.obbo.test.simpletest.v2.C2;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;

public class MappingTest {
    public int i = 1;
    interface ProviderImpl extends Provider {
        @Override
        default RetentionType retentionType() {
            return RetentionType.LAZY;
        }
    }

    @Test
    public void test() {
        InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream("resolver.json");
        Assert.assertNotNull(stream);
        InputStreamReader reader = new InputStreamReader(stream);
        Assert.assertNotNull(reader);
        JsonResolver resolver = JsonResolver.of(reader);
        Obbo obbo = new Obbo(resolver);
        ProviderImpl pv = () -> "v" + i;
        ProviderImpl pc = () -> "C" + i;
        resolver.registerProvider("v", pv);
        resolver.registerProvider("c", pc);

        consumeWrapper(obbo.wrap(Wrapper.class, new C1()));
        i = 2;
        consumeWrapper(obbo.wrap(Wrapper.class, new C2()));
    }

    public void consumeWrapper(Wrapper wrapper) {
        Assert.assertEquals(wrapper.method(), i);
    }
}
