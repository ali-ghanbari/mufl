package org.mudebug.mufl;

import java.util.HashMap;
import java.util.Map;

public final class MethodsPool {
    private static MethodsPool instance = null;
    private final Map<String, Method> pool;
    
    private MethodsPool() {
        this.pool = new HashMap<>();
    }
    
    public static MethodsPool v() {
        if (instance == null) {
            instance = new MethodsPool();
        }
        return instance;
    }
    
    public Method getMethodByName(final String fullName) {
        return pool.get(fullName);
    }
    
    public void addToPool(final Method method) {
        final String fullName = method.getFullName();
        if (pool.get(fullName) == null) {
            pool.put(fullName, method);
        }
    }
}
