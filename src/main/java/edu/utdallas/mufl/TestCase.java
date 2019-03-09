package edu.utdallas.mufl;

import java.util.Objects;

public abstract class TestCase {
    private final ClassName declaringClass;
    private final String testCaseName;

    protected TestCase(ClassName declaringClass, String testCaseName) {
        if (declaringClass == null || testCaseName == null) {
            throw new IllegalArgumentException("non-null argument(s) expected");
        }
        testCaseName = testCaseName.trim();
        if (testCaseName.isEmpty()) {
            throw new IllegalArgumentException("non-empty test case name expected");
        }
        this.declaringClass = declaringClass;
        this.testCaseName = testCaseName;
    }

    public ClassName getDeclaringClass() {
        return declaringClass;
    }

    public String getTestCaseName() {
        return testCaseName;
    }

    public String getTestCaseFullyQualifiedName() {
        return String.format("%s::%s", this.declaringClass.toString(), this.testCaseName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TestCase testCase = (TestCase) o;
        return this.declaringClass.equals(testCase.declaringClass) &&
                this.testCaseName.equals(testCase.testCaseName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(declaringClass, testCaseName);
    }

    @Override
    public String toString() {
        return this.getTestCaseFullyQualifiedName();
    }
}
