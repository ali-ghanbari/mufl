package org.mudebug.mufl;

public abstract class TestCase {
    protected final String declaringClass;
    protected final String testName;
    protected Mutation[] influencers;
    
    protected TestCase(String qualifiedName) {
        final int lastDot = qualifiedName.lastIndexOf('.');
        this.declaringClass = qualifiedName.substring(0, lastDot);
        this.testName = qualifiedName.substring(1 + lastDot);
        this.influencers = new Mutation[0];
    }
    
    protected TestCase(String declaringClass, String testName) {
        this.declaringClass = declaringClass;
        this.testName = testName;
        this.influencers = new Mutation[0];
    }
    
    public void addInfluencer(final Mutation mutation) {
        Mutation[] influencersExt = new Mutation[influencers.length + 1];
        System.arraycopy(influencers, 0, influencersExt, 0, influencers.length);
        influencersExt[influencers.length] = mutation;
        this.influencers = influencersExt;
    }
    
    public boolean equals(String declaringClass, String testName) {
        return declaringClass.equals(this.declaringClass)
                && testName.equals(this.testName);
    }
    
    public String getQualifiedName() {
        return String.format("%s.%s", declaringClass, testName);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((declaringClass == null) ? 0 : declaringClass.hashCode());
        result = prime * result + ((testName == null) ? 0 : testName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TestCase other = (TestCase) obj;
        if (declaringClass == null) {
            if (other.declaringClass != null) {
                return false;
            }
        } else if (!declaringClass.equals(other.declaringClass)) {
            return false;
        }
        if (testName == null) {
            if (other.testName != null) {
                return false;
            }
        } else if (!testName.equals(other.testName)) {
            return false;
        }
        return true;
    }

    public String getDeclaringClass() {
        return declaringClass;
    }

    public String getTestName() {
        return testName;
    }

    public Mutation[] getInfluencers() {
        return influencers;
    }
}
