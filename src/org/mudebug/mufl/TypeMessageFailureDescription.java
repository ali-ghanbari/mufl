package org.mudebug.mufl;

import java.util.List;

public final class TypeMessageFailureDescription extends FailureDescription {
    private TypeMessageFailureDescription(String info) {
        super(info);
    }
    
    private static String getInfo(String firstLine, List<String> trace) {
        return firstLine;
    }
    
    public static TypeMessageFailureDescription forDescription(String firstLine, List<String> trace) {
        return new TypeMessageFailureDescription(getInfo(firstLine, trace));
    }

}
