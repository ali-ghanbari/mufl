package org.mudebug.mufl;

import java.util.function.Function;

final class Config {
    public final static String GROUP_ID = System.getProperty("mufl.group.id", null);
    public final static FailureDescriptorFactory LEVEL = 
            FailureDescriptorFactory.valueOf(System.getProperty("mufl.level", "kind").toUpperCase());
    public final static Function<Method, Double> SUSP_FUNCTION = 
            System.getProperty("mufl.susp.func", "old").toLowerCase().equals("old") ? 
                    Method::getOldSusp : Method::getNewSusp;
}
