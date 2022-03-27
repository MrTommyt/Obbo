package mr.tommy.obbo.mapping.resolver.json;

import com.google.gson.JsonElement;
import mr.tommy.obbo.mapping.resolver.Provider;
import mr.tommy.obbo.mapping.resolver.ProviderFactory;
import mr.tommy.obbo.util.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "unused"}) // Gson
public class ResolveInfo {
    private static final Pattern replacePattern = Pattern.compile("@(\\w+)@");
    private Map<String, JsonElement> variables;
    private Map<String, MemberInfo[]> replacements;
    private final Map<String, Provider> providers = new HashMap<>();
    private Map<String, ClassInfo> classInfo;
    private ProviderFactory<JsonElement> factory;

    public String parseClass(String str) {
        Matcher matcher = replacePattern.matcher(str);
        boolean m = matcher.matches();
        return matcher.find() ? parseClass(matcher.replaceAll(this::replacer)) : str;
    }

    public String parseMethod(String method, String cls) {
        ClassInfo cInfo = getClassInfo().get(parseClass(cls));
        if (cInfo != null) {
            MemberInfo member = cInfo.member(method);
            if (member != null) return member.getOriginal();
        }
        Matcher matcher = replacePattern.matcher(method);
        boolean m = matcher.matches();
        return matcher.find() ? parseMethod(matcher.replaceAll(this::replacer), cls) : method;
    }

    private String replacer(MatchResult mr) {
        String group = mr.group(1);
        Provider p = Utils.getOrPut(providers, group, () -> {
            Provider provider = factory.getResolver().getRegistry().getRegisteredProvider(group);
            if (provider != null)
                return provider;

            JsonElement variable = variables.get(group);
            if (variable == null)
                return null;

            provider = factory.from(variable);
            if (provider == null)
                return null;

            providers.put(group, provider);
            return provider;
        });
        return p == null ? group : p.get();
    }

    public String parseField(String name) {
        //TODO: Parse
        return name;
    }

    public ProviderFactory<?> getFactory() {
        return factory;
    }

    public void setFactory(ProviderFactory<JsonElement> factory) {
        this.factory = factory;
    }

    public Map<String, ClassInfo> getClassInfo() {
        if (classInfo != null)
            return classInfo;

        classInfo = new HashMap<>();
        replacements.forEach((s, memberInfos) -> {
            String cls = parseClass(s);
            classInfo.put(cls, new ClassInfo(memberInfos));
        });
        return classInfo;
    }
}
