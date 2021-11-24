package mr.tommy.obbo.mapping.resolver.json;

import java.util.HashMap;
import java.util.Map;

class ClassInfo {
    private final Map<String, MemberInfo> info = new HashMap<>();
    public ClassInfo(MemberInfo[] members) {
        for (MemberInfo member : members) {
            info.put(member.getMethod(), member);
        }
    }

    public MemberInfo member(String name) {
        return info.get(name);
    }
}
