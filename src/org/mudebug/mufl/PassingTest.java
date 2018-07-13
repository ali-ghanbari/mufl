package org.mudebug.mufl;

public final class PassingTest extends TestCase {
    public PassingTest(String qualifiedName) {
        super(qualifiedName);
    }

    public PassingTest(String declaringClass, String testName) {
        super(declaringClass, testName);
    }

    @Override
    public void computeInfluencers() {
        for (final Mutation mutation : cover) {
            if (mutation.getFailureDetails(this) != null) {
                addInfluencer(mutation);
                mutation.addPassingImpact(this);
            }
        }
    }

}
