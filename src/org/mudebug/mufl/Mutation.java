package org.mudebug.mufl;

import java.util.HashSet;
import java.util.Set;

public class Mutation {
    private final Method mutatedMethod;
    private FailingTest[] failingImpacts;
    private PassingTest[] passingImpacts;
    private final Set<TestCase> coveringTests;
    private final Set<TestCase> killingTests;

    public Mutation(Method mutatedMethod, Set<TestCase> coveringTests) {
        this.mutatedMethod = mutatedMethod;
        this.failingImpacts = new FailingTest[0];
        this.passingImpacts = new PassingTest[0];
        this.coveringTests = coveringTests;
        this.killingTests = new HashSet<>();
    }
    
    public void addKillingTests(TestCase tc) {
        this.killingTests.add(tc);
    }
    
    public boolean isCoveredBy(TestCase ft) {
        return coveringTests.contains(ft);
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
        final int tfe = getSizeOfFailingImpacts();
        final int tpe = getSizeOfPassingImpacts();
        if (tfe == 0 && tpe == 0) {
            return 0D;
        }
        final int tf = TestsPool.v().sizeOfFailingTests();
        final double num = (double) tfe; 
        final double denom = Math.sqrt(((double) tfe + (double) tpe) * (double) tf);
        return num / denom;
    }

    public Set<TestCase> getKillingTests() {
        return killingTests;
    }
}
