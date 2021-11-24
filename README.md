# Obbo

It's a helpful library to work with obfuscated or mapped
classes. It makes use of interfaces and the proxy
pattern to work with no changes needed to make at your
code.

## Description
This library is meant to provide access to some instance
class members without needing to actually have access to
the class nor have to deal with complicated or performance
expensive reflection.

It also provides a mapper to for those methods that can be 
obfuscated between different versions and unify them using
interfaces abstracting their behavior in a single method
in an interface making use of the [Java Proxy Pattern](
https://docs.oracle.com/javase/8/docs/technotes/guides/reflection/proxy.html).

## Usage
You first need to set up a Resolver for the Obbo instance
to resolve the variable values, the default resolver is the 
[JsonResolver](
/src/main/java/mr/tommy/obbo/mapping/resolver/json/JsonResolver.java)
and declare creating then the Obbo instance:

```java
import mr.tommy.obbo.Obbo;
import mr.tommy.obbo.mapping.resolver.Provider;
import mr.tommy.obbo.mapping.resolver.json.JsonResolver;

public class Main {
    public Obbo getObbo() {
        JsonResolver resolver = JsonResolver.of(somewhere.getReader());
        //Work with the resolver if necessary.
        // like registering a custom provider for a variable.
        resolver.registerProvider("variable", Provider.of("value"));
        return new Obbo(resolver);
    }
}
```

You need to create an interface that is going to be used by 
the proxy pattern to call the methods from. Is mandatory to
put the Proxy annotation to the wrapper interface pointing 
to the actual class that the interface is trying to proxy.
**Supports variables**:

```java
import mr.tommy.obbo.entity.Proxy;

@Proxy("mr.tommy.@variable@.@cls@")
public interface Wrapper {
    void method();
}
```

## Examples