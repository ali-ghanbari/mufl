package org.mudebug.mufl;

import java.util.List;

public enum FailureDescriptorFactory {
    KIND("Ochiaitype1.txt"),
    TYPE("Ochiaitype2.txt"),
    TYPE_MESSAGE("Ochiaitype1.txt"),
    FULL("Ochiaitype1.txt");
    
    private final String fileName;
    
    public static FailureDescription createFailureDescription(FailureDescriptorFactory level,
            String firstLine,
            List<String> trace) {
        switch (level) {
        case KIND:
            return KindFailureDescription.forDescription(firstLine, trace);
        case TYPE:
            return TypeFailureDescription.forDescription(firstLine, trace);
        case TYPE_MESSAGE:
            return TypeMessageFailureDescription.forDescription(firstLine, trace);
        case FULL:
            return FullFailureDescriptor.fromDescriptor(firstLine, trace);
        default:
            throw new IllegalArgumentException();
        }
    }

    private FailureDescriptorFactory(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
