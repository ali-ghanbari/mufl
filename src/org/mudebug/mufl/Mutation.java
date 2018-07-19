package org.mudebug.mufl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Mutation {
    private final String fileName;
    private final int lineNumber;
    private final String mutatedClassName;
    private final String mutatedMethodName;
    private final String mutatedMethodDesc;
    private final int index;
    private final String mutatorName;
    private FailingTest[] failingImpacts;
    private PassingTest[] passingImpacts;
    private final Map<String, FailureDescription> failureDetails;

    public Mutation(String fileName, int lineNumber, String mutatedClassName, String mutatedMethodName,
            String mutatedMethodDesc, int index, String mutatorName) {
        this.failingImpacts = new FailingTest[0];
        this.passingImpacts = new PassingTest[0];
        this.failureDetails = new HashMap<>();
        this.fileName = fileName;
        this.lineNumber = lineNumber;
        this.mutatedClassName = mutatedClassName;
        this.mutatedMethodName = mutatedMethodName;
        this.mutatedMethodDesc = mutatedMethodDesc;
        this.index = index;
        this.mutatorName = mutatorName;
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
    
    private int tfeNot() {
        final Set<TestCase> allFailingTests = new HashSet<TestCase>(TestsPool.v().getFailingTests());
        allFailingTests.removeAll(Arrays.asList(failingImpacts));
        return allFailingTests.size();
    }
    
    private int tpeNot() {
        final Set<TestCase> allPassingTests = new HashSet<TestCase>(TestsPool.v().getPassingTests());
        allPassingTests.removeAll(Arrays.asList(passingImpacts));
        return allPassingTests.size();
    }
    
    public double getZoltarSusp() {
        final double tfe = getSizeOfFailingImpacts();
        if (tfe > 0) {
            final double tpe = getSizeOfPassingImpacts();
            final double tf = TestsPool.v().sizeOfFailingTests();
            return tfe / (tf + tpe + (10_000 * tfeNot() * tpe) / tfe);
        }
        return 0;
    }
    
    public double getM2Susp() {
        final double tfe = getSizeOfFailingImpacts();
        final double tpe = getSizeOfPassingImpacts();
        return tfe / (tfe + tpeNot() + 2 * tfeNot() + 2 * tpe);
    }
    
    public double getOchiaiSusp() {
        final double tfe = getSizeOfFailingImpacts();
        final double tpe = getSizeOfPassingImpacts();
        if (tfe > 0 || tpe > 0) {
            final double tf = TestsPool.v().sizeOfFailingTests();
            return tfe / Math.sqrt((tfe + tpe) * tf);
        }
        return 0;
    }
    
    public double getKulczynski2Susp() {
        final double tfe = getSizeOfFailingImpacts();
        final double tpe = getSizeOfPassingImpacts();
        final double tf = TestsPool.v().sizeOfFailingTests();
        final double first_part = tfe / tf; // tf is not going to be zero
        final double second_part = (tfe > 0 || tpe > 0) ? tfe / (tfe + tpe) : 0;
        return 0.5D * (first_part + second_part);
    }
    
    public double getOchiai2Susp() {
        final double tfe = getSizeOfFailingImpacts();
        final double tpe = getSizeOfPassingImpacts();
        final double tfeNot = tfeNot();
        final double tpeNot = tpeNot();
        final double denom1 = tfe + tpe;
        final double denom2 = tfeNot + tpeNot;
        final double denom3 = tfe + tpeNot;
        final double denom4 = tfeNot + tpe;
        final double denom = Math.sqrt(denom1 * denom2 * denom3 * denom4);
        if (denom > 0) {
            return (tfe * tpeNot) / denom;
        }
        return 0;
    }
    
    public double getMUSESusp() {
        final double tfe = getSizeOfFailingImpacts();
        final double tpe = getSizeOfPassingImpacts();
        final double tf = TestsPool.v().sizeOfFailingTests();
        final double tp = TestsPool.v().sizeOfPassingTests();
        if (tpe > 0) {
            return tf - tp * tfe / tpe;
        }
        return 0;
    }
    
    public double getTestDistinguishingOchiaiSusp() {
        final double tfe = Arrays.stream(failingImpacts).mapToDouble(TestCase::getWeight).sum();
        final double tpe = Arrays.stream(passingImpacts).mapToDouble(TestCase::getWeight).sum();
        if (tfe > 0.D || tpe > 0.D) {
            final double tf = TestsPool.v().getFailingTests().stream().mapToDouble(TestCase::getWeight).sum();
            return tfe / Math.sqrt((tfe + tpe) * tf);
        }
        return 0;
    }

    public String getFileName() {
        return fileName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getMutatedClassName() {
        return mutatedClassName;
    }

    public String getMutatedMethodName() {
        return mutatedMethodName;
    }

    public String getMutatedMethodDesc() {
        return mutatedMethodDesc;
    }

    public String getMutatorName() {
        return mutatorName;
    }

    public int getIndex() {
        return index;
    }
}
