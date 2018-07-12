package org.mudebug.mufl;

import java.util.List;
import java.util.stream.Collectors;

public final class FullFailureDescriptor extends FailureDescription {
    private FullFailureDescriptor(String info) {
        super(info);
    }
    
    private static String getInfo(String firstLine, List<String> trace) {
        final int afterAt = 3;
        final String stackTrace = trace.stream()
            .map(String::trim)
            .map(s -> s.substring(afterAt))
            .filter(s -> s.startsWith(Config.GROUP_ID))
            .collect(Collectors.joining(","));
        return firstLine + " : " + stackTrace;
    }
    
    public static FullFailureDescriptor fromDescriptor(String firstLine, List<String> trace) {
        return new FullFailureDescriptor(getInfo(firstLine, trace));
    }
}
