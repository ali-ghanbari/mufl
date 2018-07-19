package org.mudebug.mufl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.FileUtils;

public class Driver {
    
    public static void main(String[] args) {
        final String progName = args[0];
        final int verCount = Integer.parseInt(args[1]);
        final String programsBasePath = args[2];
        final String dynamicInfoBasePath = args[3];

        for (int progVer = 1; progVer <= verCount; progVer++) {
            final File allTests = FileUtils.getFile(dynamicInfoBasePath,
                    "MutationData",
                    progName,
                    "coverage-test",
                    String.format("%d.txt", progVer));
            final File originallyFailingTests = FileUtils.getFile(dynamicInfoBasePath,
                    "FailingTests",
                    progName,
                    String.format("%d.txt", progVer));
            final File mutations = FileUtils.getFile(programsBasePath,
                    progName,
                    Integer.toString(progVer),
                    "target",
                    "simpr-reports",
                    "mutations.gz");
            
            doStep("initializing", () -> {TestsPool.v().clear();});
            doStep("populating test case pool", () -> TestsPool.v().populate(allTests, originallyFailingTests));
            doStep("processing mutations", () -> loadMutations(mutations));
            doStep("computing influencers", () -> TestsPool.v().computeInfluencer());
            final File outMutations = FileUtils.getFile(programsBasePath,
                    progName,
                    Integer.toString(progVer),
                    "target",
                    "simpr-reports",
                    "susp-mutations.gz");
            PrintWriter pw = null;
            try (GZIPOutputStream gzos = new GZIPOutputStream(new FileOutputStream(outMutations))) {
                pw = new PrintWriter(new OutputStreamWriter(gzos), false);
                for (final TestCase t : TestsPool.v()) {
                    for (final Mutation m : t.getCover()) {
                        pw.println(m.getFileName());
                        pw.println(m.getLineNumber());
                        pw.println(m.getMutatedClassName());
                        pw.println(m.getMutatedMethodName());
                        pw.println(m.getMutatedMethodDesc());
                        pw.println(m.getIndex());
                        pw.println(m.getMutatorName());
                        pw.println(m.getOchiaiSusp());
                        pw.println("------");
                        pw.flush();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (pw != null) {
                pw.close();
            }
        }
    }
    
    private static void doStep(String message, Runnable r) {
        final String ANSI_GREEN = "\u001B[32m";
        final String ANSI_RESET = "\u001B[0m";
        System.out.printf("%-50s%s", message + "...", "[    ]");
        r.run();
        System.out.println(ANSI_GREEN + "\b\b\b\b\bDONE" + ANSI_RESET);
    }
    
    private static void loadMutations(final File mutations) {
        try (GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(mutations))) {
            final BufferedReader br = new BufferedReader(new InputStreamReader(gzis));
            String line;
            Mutation mutation = null;
            Set<TestCase> coveringTests = null;
            while((line = br.readLine()) != null) {
                line = line.trim();
                if (line.equals("------ MUTATION")) {
                    final String mutatedFileName = br.readLine();
                    final int lineNumber = Integer.parseInt(br.readLine());
                    final String mutatedClassName = br.readLine();
                    final String mutatedMethodName = br.readLine();
                    final String mutatedMethodDesc = br.readLine();
                    final int index = Integer.parseInt(br.readLine());
                    final String mutatorName = br.readLine();
                    br.readLine(); // mutator description
                    coveringTests = new HashSet<>();
                    while (!(line = br.readLine()).equals("------")) {
                        line = line.trim();
                        final String qualifiedName = line.substring(0, line.indexOf('('));
                        final TestCase coveringTest = TestsPool.v().getTestByName(qualifiedName);
                        coveringTests.add(coveringTest);
                    }
                    mutation = new Mutation(mutatedFileName,
                            lineNumber,
                            mutatedClassName,
                            mutatedMethodName,
                            mutatedMethodDesc,
                            index,
                            mutatorName);
                    for (final TestCase t : coveringTests) {
                        t.addCover(mutation);
                    }
                } else if (line.equals("------ FAILURE")) {
                    if (mutation == null) {
                        while (!br.readLine().equals("------")); // skip the failure
                    } else {
                        line = br.readLine().trim();
                        final String killingTestName = line.substring(0, line.indexOf('('));
                        final String firstLine = br.readLine().trim();
                        final ArrayList<String> trace = new ArrayList<>();
                        while (!(line = br.readLine()).equals("------")) {
                            line = line.trim();
                            trace.add(line);
                        }
                        trace.trimToSize();
                        final TestCase killingTest = TestsPool.v().getTestByName(killingTestName);
                        if (killingTest != null) {
                            assert(coveringTests.contains(killingTest));
                            final FailureDescription fd = 
                                    FailureDescriptorFactory.createFailureDescription(Config.LEVEL, firstLine, trace);
                            mutation.addKillingTest(killingTestName, fd);
                        }
                    }
                } else if (line.equals("------ STATUS")) {
                    while (!br.readLine().equals("------")); // skip detection status
                } else {
                    throw new RuntimeException(line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
