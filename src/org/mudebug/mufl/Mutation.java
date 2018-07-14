package org.mudebug.mufl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Mutation {
    private final Method mutatedMethod;
    private FailingTest[] failingImpacts;
    private PassingTest[] passingImpacts;
    private final Map<String, FailureDescription> failureDetails;

    public Mutation(Method mutatedMethod) {
        this.mutatedMethod = mutatedMethod;
        this.failingImpacts = new FailingTest[0];
        this.passingImpacts = new PassingTest[0];
        this.failureDetails = new HashMap<>();
    }
    
    public void addKillingTest(final String qualifiedName, final FailureDescription desc) {
        failureDetails.put(qualifiedName, desc);
    }
    
    public boolean doesKill(final TestCase t) {
        return failureDetails.containsKey(t.getQualifiedName());
    }
    
    public FailureDescription getFailureDetails(final TestCase t) {
        return failureDetails.get(t.getQualifiedName());
    }

    public Method getMutatedMethod() {
        return mutatedMethod;
    }
    
    public int getSizeOfFailingImpacts() {
        return failingImpacts.length;
    }
    
    public int getSizeOfPassingImpacts() {
        return passingImpacts.length;
    }
    
    public void addFailingImpact(final FailingTest failingTest) {
        FailingTest[] failingImpactsExt = new FailingTest[this.failingImpacts.length + 1];
        System.arraycopy(this.failingImpacts, 0, failingImpactsExt, 0, this.failingImpacts.length);
        failingImpactsExt[this.failingImpacts.length] = failingTest;
        this.failingImpacts = failingImpactsExt;
    }
    
    public void addPassingImpact(final PassingTest passingTest) {
        PassingTest[] passingImpactsExt = new PassingTest[passingImpacts.length + 1];
        System.arraycopy(passingImpacts, 0, passingImpactsExt, 0, passingImpacts.length);
        passingImpactsExt[passingImpacts.length] = passingTest;
        this.passingImpacts = passingImpactsExt;
    }
    
    public double getOldSusp() {
        final double tfe = getSizeOfFailingImpacts();
        final double tpe = getSizeOfPassingImpacts();
        if (tfe > 0 || tpe > 0) {
            final double tf = TestsPool.v().sizeOfFailingTests();
            return tfe / Math.sqrt((tfe + tpe) * tf);
        }
        return 0;
    }
    
    public double getNewSusp() {
        final double tfe = Arrays.stream(failingImpacts).mapToDouble(TestCase::getWeight).sum();
        final double tpe = Arrays.stream(passingImpacts).mapToDouble(TestCase::getWeight).sum();
        if (tfe > 0.D || tpe > 0.D) {
            final double tf = TestsPool.v().getFailingTests().stream().mapToDouble(TestCase::getWeight).sum();
            return tfe / Math.sqrt((tfe + tpe) * tf);
        }
        return 0;
    }
}
