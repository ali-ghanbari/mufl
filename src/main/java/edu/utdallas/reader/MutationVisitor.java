package edu.utdallas.reader;

public interface MutationVisitor {
    void visitMutation(String status,
                       String sourceFileName,
                       String mutatedClassJavaName,
                       String mutatedMethodName,
                       String mutatedMethodDesc,
                       int lineNumber,
                       String mutatorName,
                       int index,
                       String[] coveringTests,
                       String[] killingTests,
                       double suspiciousnessValue,
                       String mutationDescription);

    void visitFailureDescription(String firstLine, String stackTrace);
}
