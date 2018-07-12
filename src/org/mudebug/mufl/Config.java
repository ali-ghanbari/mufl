package org.mudebug.mufl;

final class Config {
    public final static String GROUP_ID = System.getProperty("mufl.group.id", null);
    public final static FailureDescriptorFactory LEVEL = 
            FailureDescriptorFactory.valueOf(System.getProperty("mufl.level", "kind").toUpperCase());
}
