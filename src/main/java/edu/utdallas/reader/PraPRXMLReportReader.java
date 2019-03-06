package edu.utdallas.reader;

import org.apache.commons.text.StringEscapeUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

public final class PraPRXMLReportReader {
    private final BufferedReader br;

    public PraPRXMLReportReader(final String xmlFileName) throws Exception {
        this(new File(xmlFileName));
    }

    public PraPRXMLReportReader(final File xmlFile) throws Exception {
        final InputStream gzis = new GZIPInputStream(new FileInputStream(xmlFile));
        br = new BufferedReader(new InputStreamReader(gzis));
        br.readLine(); // ignore <?xml version="1.0" encoding="UTF-8"?>
        br.readLine(); // ignore <mutations>
    }

    private String getStatus(final String headerElement) {
        final String[] attribs = headerElement.split("\\s");
        final String statusAssignment = attribs[2];
        final int len = statusAssignment.length();
        return statusAssignment.substring(8, len - 1);
    }

    private String extractContents(final String element,
                                   final String startTag,
                                   final String closingTag) {
        final int indexOfClosingTag = element.indexOf(closingTag);
        return element.substring(startTag.length(), indexOfClosingTag);
    }

    private String getSourceFileName(final String sourceFileElement) {
        return extractContents(sourceFileElement,
                "<sourceFile>",
                "</sourceFile>");
    }

    private String getMutatedClassJavaName(final String mutatedClassElement) {
        return extractContents(mutatedClassElement,
                "<mutatedClass>",
                "</mutatedClass>");
    }

    private final String sanitizeXML(final String str) {
        return StringEscapeUtils.unescapeXml(str);
    }

    private String getMutatedMethodName(final String mutatedMethodNameElement) {
        final String rawXML = extractContents(mutatedMethodNameElement,
                "<mutatedMethod>",
                "</mutatedMethod>");
        return sanitizeXML(rawXML);
    }

    private String getMutatedMethodDescription(final String mutatedMethodDescElement) {
        return extractContents(mutatedMethodDescElement,
                "<methodDescription>",
                "</methodDescription>");
    }

    private int getLineNumber(final String lineNumberElement) {
        return Integer.parseInt(extractContents(lineNumberElement,
                "<lineNumber>",
                "</lineNumber>"));
    }

    private String getMutatorName(final String mutatorNameElement) {
        return extractContents(mutatorNameElement,
                "<mutator>",
                "</mutator>");
    }

    private int getIndex(final String indexElement) {
        return Integer.parseInt(extractContents(indexElement,
                "<index>",
                "</index>"));
    }

    private String[] getCoveringTests(final String coveringTestsElement) {
        return extractContents(coveringTestsElement,
                "<coveringTests>",
                "</coveringTests>").split(",\\s");
    }

    private String[] getKillingTests(final String killingTestsElement) {
        return extractContents(killingTestsElement,
                "<killingTests>",
                "</killingTests>").split(",\\s");
    }

    private double getSuspiciousnessValue(final String suspValueElement) {
        return Double.parseDouble(extractContents(suspValueElement,
                "<suspValue>",
                "</suspValue>"));
    }

    private String getMutationDescription(final String mutationDescElement) {
        final String rawXML = extractContents(mutationDescElement,
                "<description>",
                "</description>");
        return sanitizeXML(rawXML);
    }

    public void start(final MutationVisitor visitor) throws Exception {
        String line;
        while ((line = br.readLine()) != null) {
            if (line.startsWith("</mutations>")) {
                return; // end of the file
            }
            /* getting element header */
            final String status;
            {
                final int indexOfSourceFileTag = line.indexOf("<sourceFile>");
                final String headerElement = line.substring(0, indexOfSourceFileTag);
                status = getStatus(headerElement);
                line = line.substring(indexOfSourceFileTag);
            }
            /* getting source file name */
            final String sourceFileName;
            {
                final int indexOfMutatedClassTag = line.indexOf("<mutatedClass>");
                final String sourceFileElement = line.substring(0, indexOfMutatedClassTag);
                sourceFileName = getSourceFileName(sourceFileElement);
                line = line.substring(indexOfMutatedClassTag);
            }
            /* getting mutated class (java) name */
            final String mutatedClassJavaName;
            {
                final int indexOfMutatedMethodTag = line.indexOf("<mutatedMethod>");
                final String mutatedClassElement = line.substring(0, indexOfMutatedMethodTag);
                mutatedClassJavaName = getMutatedClassJavaName(mutatedClassElement);
                line = line.substring(indexOfMutatedMethodTag);
            }
            /* get mutated method name */
            final String mutatedMethodName;
            {
                final int indexOfMethodDescTag = line.indexOf("<methodDescription>");
                final String mutatedMethodNameElement = line.substring(0, indexOfMethodDescTag);
                mutatedMethodName = getMutatedMethodName(mutatedMethodNameElement);
                line = line.substring(indexOfMethodDescTag);
            }
            /* get mutated method descriptor */
            final String mutatedMethodDescriptor;
            {
                final int indexOfLineNumberTag = line.indexOf("<lineNumber>");
                final String mutatedMethodDescElement = line.substring(0, indexOfLineNumberTag);
                mutatedMethodDescriptor = getMutatedMethodDescription(mutatedMethodDescElement);
                line = line.substring(indexOfLineNumberTag);
            }
            /* get mutated line number */
            final int lineNumber;
            {
                final int indexOfMutatorNameTag = line.indexOf("<mutator>");
                final String lineNumberElement = line.substring(0, indexOfMutatorNameTag);
                lineNumber = getLineNumber(lineNumberElement);
                line = line.substring(indexOfMutatorNameTag);
            }
            /* get mutator name */
            final String mutatorName;
            {
                final int indexOfIndexTag = line.indexOf("<index>");
                final String mutatorNameElement = line.substring(0, indexOfIndexTag);
                mutatorName = getMutatorName(mutatorNameElement);
                line = line.substring(indexOfIndexTag);
            }
            /* get index */
            final int index;
            {
                final int indexOfBlockTag = line.indexOf("<block>");
                final String indexElement = line.substring(0, indexOfBlockTag);
                index = getIndex(indexElement);
                line = line.substring(indexOfBlockTag);
            }
            /* get covering tests */
            final String[] coveringTests;
            {
                int indexOfKillingTestsTag = line.indexOf("<killingTests>");
                if (indexOfKillingTestsTag < 0) {
                    indexOfKillingTestsTag = line.indexOf("<killingTests/>");
                }
                final String coveringTestsElement = line.substring(0, indexOfKillingTestsTag);
                coveringTests = getCoveringTests(coveringTestsElement);
                line = line.substring(indexOfKillingTestsTag);
            }
            /* get killing tests */
            final String[] killingTests;
            {
                final int indexOfKillingTestsTag = line.indexOf("<killingTests>");
                if (indexOfKillingTestsTag < 0) {
                    killingTests = new String[0];
                    final int indexOfSuspValueTag = line.indexOf("<suspValue>");
                    line = line.substring(indexOfSuspValueTag);
                } else {
                    final int indexOfSuspValueTag = line.indexOf("<suspValue>");
                    final String killingTestsElement = line.substring(0, indexOfSuspValueTag);
                    killingTests = getKillingTests(killingTestsElement);
                    line = line.substring(indexOfSuspValueTag);
                }
            }
            final double suspiciousnessValue;
            {
                final int indexOfMutationDescTag = line.indexOf("<description>");
                final String suspValueElement = line.substring(0, indexOfMutationDescTag);
                suspiciousnessValue = getSuspiciousnessValue(suspValueElement);
                line = line.substring(indexOfMutationDescTag);
            }
            final String mutationDescription;
            {
                final int indexOfClosingTag = line.indexOf("</mutation>");
                final String mutationDescriptionElement = line.substring(0, indexOfClosingTag);
                mutationDescription = getMutationDescription(mutationDescriptionElement);
            }
            visitor.visit(status,
                    sourceFileName,
                    mutatedClassJavaName,
                    mutatedMethodName,
                    mutatedMethodDescriptor,
                    lineNumber,
                    mutatorName,
                    index,
                    coveringTests,
                    killingTests,
                    suspiciousnessValue,
                    mutationDescription);
        }
    }
}
