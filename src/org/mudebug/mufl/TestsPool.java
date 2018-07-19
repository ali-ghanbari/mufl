package org.mudebug.mufl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public final class TestsPool implements Iterable<TestCase> {
    private static TestsPool instance = null;
    private final Map<String, PassingTest> passingTests;
    private final Map<String, FailingTest> failingTests;
    
    private TestsPool () {
        this.passingTests = new HashMap<>();
        this.failingTests = new HashMap<>();
    }
    
    public static TestsPool v() {
        if (instance == null) {
            instance = new TestsPool();
        }
        return instance;
    }
    
    public void clear() {
        passingTests.clear();
        failingTests.clear();
    }
    
    public void computeInfluencer() {
        passingTests.values().forEach(PassingTest::computeInfluencers);
        failingTests.values().forEach(FailingTest::computeInfluencers);
    }
    
    public int sizeOfFailingTests() {
        return failingTests.size();
    }
    
    public int sizeOfPassingTests() {
        return passingTests.size();
    }
    
    public TestCase getTestByName(final String qualifiedName) {
        final FailingTest ft = failingTests.get(qualifiedName);
        if (ft != null) {
            return ft;
        }
        PassingTest pt = passingTests.get(qualifiedName);
        if (pt == null) {
            pt = TestCaseFactory.createPassingTestCase(qualifiedName);
            passingTests.put(qualifiedName, pt);
        }
        return pt;
    }
    
    public TestCase getTestByName(final String declaringClass, final String testName) {
        return getTestByName(String.format("%s.%s", declaringClass, testName));
    }
    
    public void populate(final File allTests, final File originallyFailingTests) {
        final Map<String, Pair<String, List<String>>> rawInfo = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(allTests))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.startsWith("^^^")) {
                    final int lp = line.indexOf('(');
                    final String qualifiedName = line.substring(0, lp);
                    if (line.contains("[STACKTRACE]")) {
                        final int rp = line.indexOf(')', lp);
                        final String failureDetails = line.substring(rp + ") false ".length());
                        final String[] parts = failureDetails.split("\\s\\[STACKTRACE\\]\\s");
                        final List<String> trace = Arrays.asList(parts[1].split("\\s"));
                        rawInfo.put(qualifiedName, new ImmutablePair<>(parts[0], trace));
                    }
//                    } else {
//                        final PassingTest passingTest = TestCaseFactory.createPassingTestCase(qualifiedName);
//                        passingTests.put(qualifiedName, passingTest);
//                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try (BufferedReader br = new BufferedReader(new FileReader(originallyFailingTests))) {
            String line;
            while((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    final String[] sa = line.split("::");
                    final String qualifiedName = sa[0] + "." + sa[1];
                    final Pair<String, List<String>> fip = rawInfo.get(qualifiedName);
                    if (fip != null) {
                        final FailingTest failingTest = 
                                TestCaseFactory.createFailingTestCase(sa[0], sa[1], fip.getLeft(), fip.getRight());
                        failingTests.put(qualifiedName, failingTest);
                    }
                }
            }
         } catch (Exception e) {
             e.printStackTrace();
         }
    }

    public Collection<? extends TestCase> getPassingTests() {
        return passingTests.values();
    }

    public Collection<? extends TestCase> getFailingTests() {
        return failingTests.values();
    }

    @Override
    public Iterator<TestCase> iterator() {
        return new Iterator<TestCase>() {
            private final Iterator<FailingTest> it1 = TestsPool.this.failingTests.values().iterator();
            private final Iterator<PassingTest> it2 = TestsPool.this.passingTests.values().iterator();
            
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean hasNext() {
                return it1.hasNext() || it2.hasNext();
            }

            @Override
            public TestCase next() {
                if (it1.hasNext()) {
                    return it1.next();
                }
                return it2.next();
            }
        };
    }
}
