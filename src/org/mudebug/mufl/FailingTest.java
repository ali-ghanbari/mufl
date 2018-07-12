package org.mudebug.mufl;

public final class FailingTest extends TestCase {
    private final FailureDescription failureDesc;
    
    public FailingTest(String qualifiedName, FailureDescription desc) {
        super(qualifiedName);
        this.failureDesc = desc;
    }
    
    public FailingTest(String declaringClass, String testName, FailureDescription desc) {
        super(declaringClass, testName);
        this.failureDesc = desc;
    }

    public FailureDescription getFailureDescription() {
        return failureDesc;
    }
}
