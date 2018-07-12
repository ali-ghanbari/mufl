package org.mudebug.mufl;

import java.util.List;

public final class TestCaseFactory {
    private TestCaseFactory() {
        
    }
    
    public static FailingTest createFailingTestCase(String qualifiedName, String firstLine, List<String> trace) {
        final FailureDescription desc = 
                FailureDescriptorFactory.createFailureDescription(Config.LEVEL, firstLine, trace);
        return new FailingTest(qualifiedName, desc);
    }
    
    public static FailingTest createFailingTestCase(String declaringClass, String testName,
            String firstLine, List<String> trace) {
        final FailureDescription desc = 
                FailureDescriptorFactory.createFailureDescription(Config.LEVEL, firstLine, trace);
        return new FailingTest(declaringClass, testName, desc);
    }
    
    public static PassingTest createPassingTestCase(String qualifiedName) {
        return new PassingTest(qualifiedName);
    }
    
    public static PassingTest createPassingTestCase(String declaringClass, String testName) {
        return new PassingTest(declaringClass, testName);
    }
}
