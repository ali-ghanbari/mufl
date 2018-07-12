package org.mudebug.mufl;

public abstract class FailureDescription {
    private final String info;

    public FailureDescription(final String info) {
        this.info = info;
    }
    
    public String getInfo() {
        return info;
    }
    
    @Override
    public String toString() {
        return info;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((info == null) ? 0 : info.hashCode());
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
        FailureDescription other = (FailureDescription) obj;
        if (info == null) {
            if (other.info != null) {
                return false;
            }
        } else if (!info.equals(other.info)) {
            return false;
        }
        return true;
    }
}
