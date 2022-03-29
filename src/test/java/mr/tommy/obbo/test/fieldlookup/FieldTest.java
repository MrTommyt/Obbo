package mr.tommy.obbo.test.fieldlookup;

import mr.tommy.obbo.Obbo;
import mr.tommy.obbo.mapping.resolver.Provider;
import mr.tommy.obbo.mapping.resolver.RetentionType;
import mr.tommy.obbo.mapping.resolver.json.JsonResolver;
import mr.tommy.obbo.test.fieldlookup.v1.C1;
import mr.tommy.obbo.test.fieldlookup.v2.C2;
import org.junit.Assert;

import java.io.InputStream;
import java.io.InputStreamReader;

public class FieldTest {
    interface ProviderImpl extends Provider {
        @Override
        default RetentionType retentionType() {
            return RetentionType.LAZY;
        }
    }

    @org.junit.Test
    public void get() {
        var ref = new Object() {
            int i = 1;
        };
        InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream("resolver.json");
        Assert.assertNotNull(stream);
        InputStreamReader reader = new InputStreamReader(stream);
        Assert.assertNotNull(reader);
        JsonResolver resolver = JsonResolver.of(reader);
        Obbo obbo = new Obbo(resolver);
        ProviderImpl pv = () -> "v" + ref.i;
        ProviderImpl pc = () -> "C" + ref.i;
        ProviderImpl pi = () -> String.valueOf(ref.i);
        resolver.registerProvider("v", pv);
        resolver.registerProvider("c", pc);
        resolver.registerProvider("i", pi);

        assertGet(obbo.wrap(Wrapper.class, new C1()), ref.i);
        ref.i = 2;
        assertGet(obbo.wrap(Wrapper.class, new C2()), ref.i);
    }

    @org.junit.Test
    public void set() {
        var ref = new Object() {
            int i = 1;
        };
        InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream("resolver.json");
        Assert.assertNotNull(stream);
        InputStreamReader reader = new InputStreamReader(stream);
        Assert.assertNotNull(reader);
        JsonResolver resolver = JsonResolver.of(reader);
        Obbo obbo = new Obbo(resolver);
        ProviderImpl pv = () -> "v" + ref.i;
        ProviderImpl pc = () -> "C" + ref.i;
        ProviderImpl pi = () -> String.valueOf(ref.i);
        resolver.registerProvider("v", pv);
        resolver.registerProvider("c", pc);
        resolver.registerProvider("i", pi);

        assertSet(obbo.wrap(Wrapper.class, new C1()), 2);
        ref.i = 2;
        assertSet(obbo.wrap(Wrapper.class, new C2()), 1);
    }

    public void assertGet(Wrapper wrapper, int i) {
        Assert.assertEquals(wrapper.i(), i);
    }

    public void assertSet(Wrapper wrapper, int i) {
        wrapper.i(i);
        assertGet(wrapper, i);
    }
}
