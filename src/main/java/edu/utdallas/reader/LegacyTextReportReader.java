package edu.utdallas.reader;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class LegacyTextReportReader {
    private static final String[] EMPTY_ARRAY = new String[0];

    private final LegacyTextFile ltf;

    public LegacyTextReportReader(File file) throws IOException {
        this(new LegacyTextFile(file));
    }

    public LegacyTextReportReader(LegacyTextFile ltf) {
        this.ltf = ltf;
    }

    public void close() throws Exception {
        this.ltf.close();
    }

    public void start(MutationVisitor visitor) {
        String[] description = EMPTY_ARRAY;
        String line;
        while ((line = this.ltf.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("MutationDetails")) {
                if (description.length > 0) {
                    processMutation(description, visitor);
                    description = EMPTY_ARRAY;
                }
            }
            String[] description_ext = new String[description.length + 1];
            System.arraycopy(description, 0, description_ext, 0, description.length);
            description_ext[description.length] = line;
            description = description_ext;
        }
        if (description.length > 0) { // don't miss the last one!
            processMutation(description, visitor);
        }
    }

    private String constructMethodFullSignature(String description) {
        String[] parts = description.split(",\\s");
        for(int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].substring(parts[i].indexOf('=') + 1);
        }
        return String.format("%s.%s%s", parts[0], parts[1], parts[2]);
    }

    private String getClassName(String methodFullSignature) {
        final int lastIndexOfDot = methodFullSignature.lastIndexOf('.');
        return methodFullSignature.substring(0, lastIndexOfDot);
    }

    private void processMutation(final String[] description, MutationVisitor visitor) {
        final String mutationDescription = description[0];

        if (!mutationDescription.startsWith("MutationDetails")) {
            return;
        }
        if (!(mutationDescription.startsWith("MutationDetails"))) {
            throw new RuntimeException("malformed report");
        }

        final int indexOfMethodDescription = mutationDescription.indexOf("clazz=");

        if (indexOfMethodDescription < 0) {
            throw new RuntimeException("malformed report");
        }

        final String methodDescription = mutationDescription.substring(indexOfMethodDescription,
                mutationDescription.indexOf(']', indexOfMethodDescription));
        final String mutatedMethodFullSignature = constructMethodFullSignature(methodDescription);

        final int indexOfCoverageDescription = mutationDescription.indexOf("testsInOrder=");

        if (indexOfCoverageDescription < 0) {
            throw new RuntimeException("malformed report");
        }

        final String[] coveringTests = mutationDescription
                .substring(mutationDescription.indexOf('[', indexOfCoverageDescription) + 1,
                        mutationDescription.indexOf(']', indexOfCoverageDescription))
                .split(",\\s");

        String[] killingTests = new String[0];

        for (int i = 1; i < description.length; i++) {
            final String testFailureDescription = description[i];

            if (!(testFailureDescription.startsWith("[EXCEPTION]"))) {
                throw new RuntimeException("malformed report");
            }

            int index = testFailureDescription.indexOf(']') + 1;

            /* ignoring the set of white spaces */
            while (Character.isWhitespace(testFailureDescription.charAt(index))) {
                index++;
            }

            int lastIndex = testFailureDescription.indexOf(')');
            int indexOfFalse = testFailureDescription.indexOf(" false ");

            if (lastIndex >= 0 && lastIndex < indexOfFalse) {
                /* adding the killing test name into our array */
                final String killingTestName = testFailureDescription.substring(index, lastIndex);
                killingTests = Arrays.copyOf(killingTests, 1 + killingTests.length);
                killingTests[killingTests.length - 1] = killingTestName;

                int startOfFailureDescription = testFailureDescription.indexOf(')')
                        + 1 /* ignore the right parenthesis itself */;

                /* ignoring the first set of white spaces */
                while (Character.isWhitespace(testFailureDescription.charAt(startOfFailureDescription))) {
                    startOfFailureDescription++;
                }
                /* ignore the substring "false" whose length is 5 */
                startOfFailureDescription += 5;
                /* ignoring the second set of white spaces */
                while (Character.isWhitespace(testFailureDescription.charAt(startOfFailureDescription))) {
                    startOfFailureDescription++;
                }

                final String failureDescription = testFailureDescription.substring(startOfFailureDescription);
                final int indexOf_STACKTRACE_ = failureDescription.indexOf(" [STACKTRACE] ");
                final String firstLine = failureDescription.substring(0, indexOf_STACKTRACE_);
                final String stackTrace = failureDescription.substring(indexOf_STACKTRACE_
                        + 14 /* 14 = " [STACKTRACE] ".length()*/).trim();

                visitor.visitFailureDescription(firstLine, stackTrace);
            }
        }

        final String status = killingTests.length > 0 ? "KILLED" : "SURVIVED";
        final String mutatedClassName = getClassName(mutatedMethodFullSignature);

        visitor.visitMutation(status,
                "src",
                mutatedClassName,
                mutatedMethodFullSignature,
                "desc",
                0,
                "mutatorName",
                0,
                coveringTests,
                killingTests,
                0,
                "desc");
    }
}
