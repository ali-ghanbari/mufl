package edu.utdallas.mufl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestCasePool {
    private static TestCasePool instance;
    private final Map<String, PassingTestCase> passingTestCaseMap;
    private final Map<String, FailingTestCase> failingTestCaseMap;

    static {
        instance = null;
    }

    private TestCasePool() {
        this.passingTestCaseMap = new HashMap<>();
        this.failingTestCaseMap = new HashMap<>();
    }

    public synchronized static TestCasePool v() {
        if (instance == null) {
            instance = new TestCasePool();
        }
        return instance;
    }

    public void populateFailingTestCases(File failingTests) throws IOException {
        final File failureInfoBasePath = failingTests.getParentFile();
        final String baseName = failingTests.getName();
        final List<String> failingTestsList = Files.readAllLines(failingTests.toPath());
        int exceptionNo = 1;
        for (String qualifiedName : failingTestsList) {
            qualifiedName = qualifiedName.trim();
            final String[] nameParts = qualifiedName.split("::");
            final String declaringClassJavaName = nameParts[0];
            final String testCaseName = nameParts[1];
            final String exceptionFileName = String.format("%s.%d", baseName, exceptionNo);
            final File exceptionTraceFile = new File(failureInfoBasePath, exceptionFileName);
            final String[] exceptionTraceLines = Files.lines(exceptionTraceFile.toPath())
                    .toArray(String[]::new);
            final FailingTestCase failingTestCase =
                    TestCaseFactory.createFailingTestCase(declaringClassJavaName,
                            testCaseName, exceptionTraceLines);
            this.failingTestCaseMap.put(qualifiedName, failingTestCase);
            exceptionNo++;
        }
    }

    public TestCase getTestCaseByName(String declaringClassJavaName,
                                      String testCaseName) {
        final String qualifiedName =
                String.format("%s::%s", declaringClassJavaName, testCaseName);
        TestCase testCase = this.failingTestCaseMap.get(qualifiedName);
        if (testCase != null) {
            return testCase;
        }
        return this.passingTestCaseMap.compute(qualifiedName,
                (k, v) -> v == null ? TestCaseFactory.createPassingTestCase(declaringClassJavaName, testCaseName) : v);
    }
}
