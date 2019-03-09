package edu.utdallas.mufl;

public class FailingTestCase extends TestCase {
    private final FailureDescription failureDescription;

    public FailingTestCase(ClassName declaringClass,
                           String testCaseName,
                           FailureDescription failureDescription) {
        super(declaringClass, testCaseName);
        this.failureDescription = failureDescription;
    }
}
