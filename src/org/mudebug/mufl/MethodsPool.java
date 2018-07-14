package org.mudebug.mufl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
    
    public void clear() {
        pool.clear();
    }
    
    public Method getMethodByName(final String fullName) {
        return pool.get(fullName);
    }
    
    public void populate(final File allMethods) {
        try (BufferedReader br = new BufferedReader(new FileReader(allMethods))) {
            br.lines().map(l -> l.substring(0, l.lastIndexOf(':'))).distinct()
                .forEach(mfn -> {
                    final int indexOfColon = mfn.indexOf(':');
                    final String declaringClass = mfn.substring(0, indexOfColon);
                    final int indexOfLP = mfn.indexOf('(');
                    final String methodName = mfn.substring(1 + indexOfColon, indexOfLP);
                    final String methodDesc = mfn.substring(indexOfLP);
                    final String fullName = mfn.replace(':', '.');
                    assert(fullName.equals(String.format("%s.%s%s", declaringClass, methodName, methodDesc)));
                    final Method method = new Method(declaringClass, methodName, methodDesc);
                    MethodsPool.this.pool.put(fullName, method);
                });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
