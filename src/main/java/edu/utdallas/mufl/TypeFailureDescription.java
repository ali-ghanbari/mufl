package edu.utdallas.mufl;

/**
 * @author ali
 */
public class TypeFailureDescription extends FailureDescription {
    public TypeFailureDescription(String firstLine) {
        final int indexOfColon = firstLine.indexOf(':');
        if (indexOfColon >= 0) {
            firstLine = firstLine.substring(0, indexOfColon);;
        }
        this.setDigestedInfo(firstLine);
    }
}
