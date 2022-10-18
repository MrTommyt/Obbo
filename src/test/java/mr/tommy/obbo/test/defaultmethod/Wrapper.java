package mr.tommy.obbo.test.defaultmethod;

import mr.tommy.obbo.entity.Proxy;

@Proxy("@base@.defaultmethod.@v@.@c@")
public interface Wrapper {
    @Proxy("method@i@")
    int method();

    default int defaultMethod() {
        System.out.println("Calling from default!");
        return method();
    }
}
