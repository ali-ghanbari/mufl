package edu.utdallas.mufl;

public class TypeMessageFailureDescription extends FailureDescription {
    public TypeMessageFailureDescription(final String firstLine) {
        this.setDigestedInfo(firstLine);
    }
}
