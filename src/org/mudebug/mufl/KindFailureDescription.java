package org.mudebug.mufl;

import java.util.List;

public final class KindFailureDescription extends FailureDescription {
    private KindFailureDescription() {
        super("");
    }
    
    public static KindFailureDescription forDescription(String firstLine, List<String> trace) {
        return new KindFailureDescription();
    }
}
