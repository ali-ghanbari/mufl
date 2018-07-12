package org.mudebug.mufl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
    private static Set<Method> coveredMethods;
    private static Set<String> buggyMethodNames;
    
    public static void main(String[] args) {
        final String progName = args[0];
        final int verCount = Integer.parseInt(args[1]);
        final String programsBasePath = args[2];
        final String dynamicInfoBasePath = args[3];
        
        for (int progVer = 1; progVer <= verCount; progVer++) {
            //System.out.println(String.format("processing version %d of %s...", progVer, progName));
            final String allTests = FileUtils.getFile(dynamicInfoBasePath,
                    "MutationData",
                    progName,
                    "coverage-test",
                    String.format("%d.txt", progVer)).getAbsolutePath();
            final String originallyFailingTests = FileUtils.getFile(dynamicInfoBasePath,
                    "FailingTests",
                    progName,
                    String.format("%d.txt", progVer)).getAbsolutePath();
            final String mutations = FileUtils.getFile(programsBasePath,
                    progName,
                    Integer.toString(progVer),
                    "target",
                    "simpr-reports",
                    "mutations.gz").getAbsolutePath();
            final String bugMethodsFileName = FileUtils.getFile(dynamicInfoBasePath,
                    "BugMethod",
                    progName,
                    String.format("%d.txt", progVer)).getAbsolutePath();
            final String coveredMethodsFileName = FileUtils.getFile(dynamicInfoBasePath,
                    "xias-susp-vals",
                    progName,
                    Integer.toString(progVer),
                    Config.LEVEL.getFileName()).getAbsolutePath();
            doStep("populating test case pool", () -> TestsPool.v().populate(allTests, originallyFailingTests));
            doStep("processing mutations", () -> loadMutations(mutations));
            doStep("ranking methods", () -> calcualteMethodRanks(coveredMethods = loadCoveredMethods(coveredMethodsFileName)));
                
            
            try {
                final File base = FileUtils.getFile(progName, Integer.toString(progVer));
                base.mkdirs();
                final File outFile = new File(base, Config.LEVEL.getFileName());
                final PrintWriter pw = new PrintWriter(outFile);
                for (final Method meth : coveredMethods.stream().sorted((m1,m2) -> Double.compare(m2.getOldSusp(), m1.getOldSusp())).collect(Collectors.toList())) {
                    pw.println(meth.getFullName() + " " + meth.getOldSusp());
                }
                pw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            
//            doStep("loading buggy methods", () -> buggyMethodNames = loadBugMethods(bugMethodsFileName));
//            int top1 = 0;
//            int top3 = 0;
//            int top5 = 0;
//            for (final String methName : buggyMethodNames) {
//                final Method meth = MethodsPool.v().getMethodByName(methName);
//                if (meth == null) {
//                    System.out.println("not found" + methName + " in " + progName + "-" + progVer);
//                } else {
////                    System.out.println(String.format("%s %d", meth.getFullName(), meth.getRank()));
//                    final int rank = meth.getRank();
//                    if (rank == 1) {
//                        top1++;
//                        top3++;
//                        top5++;
//                    } else if (rank <= 3) {
//                        top3++;
//                        top5++;
//                    } else if (rank <= 5) {
//                        top5++;
//                    }
//                }
//            }
//            System.out.println(String.format("%s,%d,%d,%d,%d", progName, progVer, top1, top3, top5));
        }
    }
    
    private static void doStep(String message, Runnable r) {
        //System.out.print(message + "...");
        r.run();
        //System.out.println("\tDONE");
    }
    
    public static void calcualteMethodRanks(final Set<Method> coveredMethods) {
        final List<List<Method>> groups = coveredMethods.stream()
            .collect(Collectors.groupingBy(Method::getOldSusp))
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
    
    private static Set<Method> loadCoveredMethods(final String coveredMethodsFileName) {
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
    
    private static Set<String> loadBugMethods(final String bugMethodsFileName) {
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
    
    private static void loadMutations(final String mutations) {
        try (GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(mutations))) {
            final BufferedReader br = new BufferedReader(new InputStreamReader(gzis));
            String line;
            Mutation mutation = null;
            while((line = br.readLine()) != null) {
                line = line.trim();
                if (line.equals("------ MUTATION")) {
                    if (mutation != null) {
                        for (final TestCase ft : TestsPool.v().getFailingTests()) {
                            if (mutation.isCoveredBy(ft) && !mutation.getKillingTests().contains(ft)) {
                                ft.addInfluencer(mutation);
                                mutation.addFailingImpact((FailingTest) ft);
                            }
                        }
                    }
                    br.readLine(); // mutated file name
                    br.readLine(); // mutated line number
                    final String mutatedClass = br.readLine();
                    final String mutatedMethodName = br.readLine();
                    final String mutatedMethodDesc = br.readLine();
                    br.readLine(); // mutator name
                    br.readLine(); // mutator description
                    final Set<TestCase> coveringTests = new HashSet<>(); 
                    while (!(line = br.readLine()).equals("------")) {
                        line = line.trim();
                        final String qualifiedName = line.substring(0, line.indexOf('('));
                        final TestCase coveringTest = TestsPool.v().getTestByName(qualifiedName);
                        coveringTests.add(coveringTest);
                    }
                    Method mutatedMethod = MethodsPool.v().getMethodByName(String.format("%s.%s%s", mutatedClass, mutatedMethodName, mutatedMethodDesc));
                    if (mutatedMethod == null) {
                        mutatedMethod = new Method(mutatedClass, mutatedMethodName, mutatedMethodDesc);
                    }
                    mutation = new Mutation(mutatedMethod, coveringTests);
                    mutatedMethod.addMutation(mutation);
                    MethodsPool.v().addToPool(mutatedMethod);
                } else if (line.equals("------ FAILURE")) {
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
                        mutation.addKillingTests(killingTest);
                        if (killingTest instanceof PassingTest) { // killing test used to be a passing test
                            killingTest.addInfluencer(mutation);
                            mutation.addPassingImpact((PassingTest) killingTest);
                        } else { // killing test used to be a failing test
                            final FailureDescription fd = 
                                    FailureDescriptorFactory.createFailureDescription(Config.LEVEL, firstLine, trace);
                            if (!((FailingTest) killingTest).getFailureDescription().equals(fd)) {
                                killingTest.addInfluencer(mutation);
                                mutation.addFailingImpact((FailingTest) killingTest);
                            }
                        }
                    }
                }
            }
            for (final TestCase ft : TestsPool.v().getFailingTests()) {
                if (mutation.isCoveredBy(ft) && !mutation.getKillingTests().contains(ft)) {
                    ft.addInfluencer(mutation);
                    mutation.addFailingImpact((FailingTest) ft);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
