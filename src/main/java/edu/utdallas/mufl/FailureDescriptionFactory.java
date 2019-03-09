package edu.utdallas.mufl;

public enum FailureDescriptionFactory {
    KIND,
    TYPE,
    TYPE_MESSAGE,
    FULL;

    public FailureDescription createFor(String firstLine,
                                        String[] exceptionStackTraceLines) {
        if (this != KIND && firstLine == null) {
            throw new IllegalArgumentException("non-null first line expected");
        }
        if (this == FULL && exceptionStackTraceLines == null) {
            throw new IllegalArgumentException("non-null exception trace expected");
        }
        switch (this) {
            case KIND:
                return new KindFailureDescription();
            case TYPE:
                return new TypeFailureDescription(firstLine);
            case TYPE_MESSAGE:
                return new TypeMessageFailureDescription(firstLine);
            case FULL:
            default:
                return new FullFailureDescription(firstLine, exceptionStackTraceLines);
        }
    }
}
