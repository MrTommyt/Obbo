package mr.tommy.obbo.test.fieldlookup;

import mr.tommy.obbo.entity.FieldProxy;
import mr.tommy.obbo.entity.Proxy;

@Proxy("@base@.fieldlookup.@v@.@c@")
public interface Wrapper {
    @FieldProxy("i@i@")
    int i();

    @FieldProxy("i@i@")
    void i(int i);
}
