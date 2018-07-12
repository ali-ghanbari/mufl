package org.mudebug.mufl;

import java.util.List;

public final class TypeFailureDescription extends FailureDescription {
    private TypeFailureDescription(String info) {
        super(info);
    }
    
    private static String getInfo(String firstLine, List<String> trace) {
        final int indexOfColon = firstLine.indexOf(':');
        if (indexOfColon < 0) {
            return firstLine;
        }
        return firstLine.substring(0, indexOfColon);
    }
    
    public static TypeFailureDescription forDescription(String firstLine, List<String> trace) {
        return new TypeFailureDescription(getInfo(firstLine, trace));
    }
}
