package org.mudebug.mufl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FileUtils;

public class Driver {
    private static Set<String> buggyMethodNames;
    
    public static void main(String[] args) {
        final String progName = args[0];
        final int verCount = Integer.parseInt(args[1]);
        final String programsBasePath = args[2];
        final String dynamicInfoBasePath = args[3];
        
        double mar = 0;
        
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
            final File bugMethodsFileName = FileUtils.getFile(dynamicInfoBasePath,
                    "BugMethod",
                    progName,
                    String.format("%d.txt", progVer));
            final File coveredMethodsFileName = FileUtils.getFile(dynamicInfoBasePath,
                    "xias-susp-vals",
                    progName,
                    Integer.toString(progVer),
                    Config.LEVEL.getFileName());
            final File allMethodsFile = FileUtils.getFile(dynamicInfoBasePath,
                    "AllMethods",
                    progName,
                    String.format("%d.txt", progVer));
            TestsPool.v().clear();
            MethodsPool.v().clear();
            doStep("populating test case pool", () -> TestsPool.v().populate(allTests, originallyFailingTests));
            doStep("populating methods pool", () -> MethodsPool.v().populate(allMethodsFile));
            doStep("processing mutations", () -> loadMutations(mutations));
            TestsPool.v().computeInfluencer();
            doStep("ranking methods", () -> calcualteMethodRanks(loadCoveredMethods(coveredMethodsFileName)));
            doStep("loading buggy methods", () -> buggyMethodNames = loadBugMethods(bugMethodsFileName));
            
            double inner_mean = 0;
            
            int top1 = 0;
            int top3 = 0;
            int top5 = 0;
            for (final String methName : buggyMethodNames) {
                final Method meth = MethodsPool.v().getMethodByName(methName);
                if (meth == null) {
                    System.out.println("not found" + methName + " in " + progName + "-" + progVer);
                } else {
                    final int rank = meth.getRank();
                    inner_mean += rank;
                    if (rank == 1) {
                        top1++;
                        top3++;
                        top5++;
                    } else if (rank <= 3) {
                        top3++;
                        top5++;
                    } else if (rank <= 5) {
                        top5++;
                    }
                }
            }
            
            inner_mean /= buggyMethodNames.size() > 0 ? buggyMethodNames.size() : 1;
            
            mar += inner_mean;
            
            System.out.println(String.format("%s,%d,%d,%d,%d", progName, progVer, top1, top3, top5));
        }
        
        System.out.println(String.format("MAR = %f", mar / verCount));
        
    }
    
    private static void doStep(String message, Runnable r) {
        //System.out.print(message + "...");
        r.run();
        //System.out.println("\tDONE");
    }
    
    public static void calcualteMethodRanks(final Set<Method> coveredMethods) {
        final List<List<Method>> groups = coveredMethods.stream()
            .collect(Collectors.groupingBy(Config.SUSP_FUNCTION))
            .entrySet().stream()
            .sorted((e1, e2) -> Double.compare(e2.getKey(), e1.getKey()))
            .map(Map.Entry::getValue)
            .collect(Collectors.toList());
        int rank = 0;
        for (final List<Method> mg : groups) {
            rank += mg.size();
            for (final Method meth : mg) {
                meth.setRank(rank);
            }
        }
    }
    
    private static Set<Method> loadCoveredMethods(final File coveredMethodsFileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(coveredMethodsFileName))) {
            return br.lines()
                    .map(String::trim)
                    .filter(l -> !l.isEmpty())
                    .map(l -> l.split("\\s"))
                    .map(sa -> sa[0].trim())
                    .map(mfn -> MethodsPool.v().getMethodByName(mfn))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptySet();
        }
    }
    
    private static Set<String> loadBugMethods(final File bugMethodsFileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(bugMethodsFileName))) {
           return br.lines()
                   .map(String::trim)
                   .filter(l -> !l.isEmpty())
                   .map(l -> l.substring("^^^^^^".length()))
                   .map(mn -> mn.replace(':', '.'))
                   .collect(Collectors.toSet());
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptySet();
        }
    }
    
    private static boolean isPITMutator(final String mutatorName) {
        return true; //mutatorName.startsWith("org.pitest");
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
                    br.readLine(); // mutated file name
                    br.readLine(); // mutated line number
                    final String mutatedClass = br.readLine();
                    final String mutatedMethodName = br.readLine();
                    final String mutatedMethodDesc = br.readLine();
                    final String mutatorName = br.readLine(); // mutator name
                    if (isPITMutator(mutatorName)) {
                        br.readLine(); // mutator description
                        coveringTests = new HashSet<>();
                        while (!(line = br.readLine()).equals("------")) {
                            line = line.trim();
                            final String qualifiedName = line.substring(0, line.indexOf('('));
                            final TestCase coveringTest = TestsPool.v().getTestByName(qualifiedName);
                            coveringTests.add(coveringTest);
                        }
                        Method mutatedMethod = MethodsPool.v().getMethodByName(String.format("%s.%s%s", mutatedClass, mutatedMethodName, mutatedMethodDesc));
                        if (mutatedMethod != null) {
                            mutation = new Mutation(mutatedMethod);
                            mutatedMethod.addMutation(mutation);
                            for (final TestCase t : coveringTests) {
                                t.addCover(mutation);
                            }
                        } else {
                            mutation = null;
                        }
                    } else {
                        mutation = null;
                        while (!br.readLine().equals("------"));
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
