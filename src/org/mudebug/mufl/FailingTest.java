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

    @Override
    public void computeInfluencers() {
        for (final Mutation mutation : cover) {
            final FailureDescription fd = mutation.getFailureDetails(this);
            if (fd == null || !fd.equals(failureDesc)) {
                addInfluencer(mutation);
                mutation.addFailingImpact(this);
            }
        }
    }
}
