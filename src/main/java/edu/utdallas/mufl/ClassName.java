package edu.utdallas.mufl;

import java.util.Objects;

/**
 * Represents a class
 *
 * @author ali
 */
public class ClassName {
    private final String javaName;

    private ClassName(String javaName) {
        if (javaName.isEmpty()) {
            throw new IllegalArgumentException("non-empty class name expected");
        }
        this.javaName = javaName;
    }

    public static ClassName fromJavaName(String javaName) {
        if (javaName == null) {
            throw new IllegalArgumentException("non-null class name expected");
        }
        javaName = javaName.trim();
        return new ClassName(javaName);
    }

    public static ClassName fromInternalName(String internalName) {
        if (internalName == null) {
            throw new IllegalArgumentException("non-null class name expected");
        }
        final String javaName = internalName.trim().replace('/', '.');
        return new ClassName(javaName);
    }

    public String getJavaName() {
        return this.javaName;
    }

    public String getInternalName() {
        return this.javaName.replace('.', '/');
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ClassName className = (ClassName) o;
        return this.javaName.equals(className.javaName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.javaName);
    }

    @Override
    public String toString() {
        return this.javaName;
    }
}
