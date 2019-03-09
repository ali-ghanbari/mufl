package edu.utdallas.mufl;

import java.util.Arrays;

public final class TestCaseFactory {
    private TestCaseFactory() {

    }

    public static PassingTestCase createPassingTestCase(ClassName declaringClassName,
                                                        String testCaseName) {
        return new PassingTestCase(declaringClassName, testCaseName);
    }

    public static PassingTestCase createPassingTestCase(String declaringClassJavaName,
                                                        String testCaseName) {
        final ClassName declaringClassName = ClassName.fromJavaName(declaringClassJavaName);
        return createPassingTestCase(declaringClassName, testCaseName);
    }

    public static FailingTestCase createFailingTestCase(ClassName declaringClassName,
                                                        String testCaseName,
                                                        String[] exceptionStackTraceLines) {
        if (exceptionStackTraceLines == null || exceptionStackTraceLines.length == 0) {
            throw new IllegalArgumentException("non-null, non-empty exception trace expected");
        }
        final StringBuilder firstLineSB = new StringBuilder();
        int i = 0;
        while(!exceptionStackTraceLines[i].trim().startsWith("at ")) {
            firstLineSB.append(' ');
            firstLineSB.append(exceptionStackTraceLines[i]);
            i++;
        }
        firstLineSB.deleteCharAt(0); // skip the starting space character
        final String firstLine = firstLineSB.toString();
        exceptionStackTraceLines = Arrays.copyOfRange(exceptionStackTraceLines,
                i, exceptionStackTraceLines.length);
        final FailureDescription failureDescription =
                Config.FAILURE_DESCRIPTION_FACTORY.createFor(firstLine,
                        exceptionStackTraceLines);
        return new FailingTestCase(declaringClassName, testCaseName, failureDescription);
    }

    public static FailingTestCase createFailingTestCase(String declaringClassJavaName,
                                                        String testCaseName,
                                                        String[] exceptionStackTraceLines) {
        final ClassName declaringClassName = ClassName.fromJavaName(declaringClassJavaName);
        return createFailingTestCase(declaringClassName, testCaseName, exceptionStackTraceLines);
    }
}
