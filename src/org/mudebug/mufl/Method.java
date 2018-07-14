package org.mudebug.mufl;

import java.util.Arrays;

public class Method extends ProgramElement {
    private final String declaringClass;
    private final String name;
    private final String desc;
    private int rank;

    public Method(final String declaringClass, final String name, final String descriptor) {
        super();
        this.declaringClass = declaringClass;
        this.name = name;
        this.desc = descriptor;
        this.rank = 0;
    }
    
    public String getFullName() {
        return String.format("%s.%s%s", declaringClass, name, desc);
    }
    
    @Override
    public String toString() {
        return String.format("%s.%s%s", declaringClass, name, desc);
    }

    public String getDeclaringClass() {
        return declaringClass;
    }

    public String getName() {
        return name;
    }

    public String getDescriptor() {
        return desc;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((declaringClass == null) ? 0 : declaringClass.hashCode());
        result = prime * result + ((desc == null) ? 0 : desc.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        Method other = (Method) obj;
        if (declaringClass == null) {
            if (other.declaringClass != null) {
                return false;
            }
        } else if (!declaringClass.equals(other.declaringClass)) {
            return false;
        }
        if (desc == null) {
            if (other.desc != null) {
                return false;
            }
        } else if (!desc.equals(other.desc)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }
    
    @Override
    public double getOldSusp() {
        return Arrays.stream(this.mutations).mapToDouble(Mutation::getOldSusp).max().orElse(0D);
    }
    
    @Override
    public double getNewSusp() {
        return Arrays.stream(this.mutations).mapToDouble(Mutation::getNewSusp).max().orElse(0D);
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }
}
