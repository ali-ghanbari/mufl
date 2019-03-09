package edu.utdallas.mufl;

public final class Config {
    public static final String GROUP_ID;
    public static final FailureDescriptionFactory FAILURE_DESCRIPTION_FACTORY;

    static {
        final String level = System.getProperty("mufl.level", "KIND");
        FAILURE_DESCRIPTION_FACTORY = FailureDescriptionFactory.valueOf(level.toUpperCase());
        GROUP_ID = System.getProperty("mufl.groupId", "");
    }
}
