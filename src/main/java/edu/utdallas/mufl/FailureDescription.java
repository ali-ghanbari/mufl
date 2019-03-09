package edu.utdallas.mufl;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.Arrays;

/**
 * Represents a failure
 *
 * @author ali
 */
public abstract class FailureDescription {
    protected byte[] info;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FailureDescription that = (FailureDescription) o;
        return Arrays.equals(this.info, that.info);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.info);
    }

    protected void setDigestedInfo(String info) {
        this.info = DigestUtils.sha1(info);
    }
}
