package org.mudebug.mufl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.util.Precision;

public class Driver {
    private static Set<String> buggyMethodNames;
    
    public static void main(String[] args) {
        final String progName = args[0];
        final int verCount = Integer.parseInt(args[1]);
        final String programsBasePath = args[2];
        final String dynamicInfoBasePath = args[3];

        final int[] top1 = new int[SuspTech.TECH_COUNT];
        final int[] top3 = new int[SuspTech.TECH_COUNT];
        final int[] top5 = new int[SuspTech.TECH_COUNT];
        final double[] mar = new double[SuspTech.TECH_COUNT];
        final double[] mfr = new double[SuspTech.TECH_COUNT];
        
        Arrays.fill(top1, 0);
        Arrays.fill(top3, 0);
        Arrays.fill(top5, 0);
        Arrays.fill(mar, 0);
        Arrays.fill(mfr, 0);
        
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
            
            doStep("initializing", () -> {TestsPool.v().clear(); MethodsPool.v().clear();});
            doStep("populating test case pool", () -> TestsPool.v().populate(allTests, originallyFailingTests));
            doStep("populating methods pool", () -> MethodsPool.v().populate(allMethodsFile));
            doStep("processing mutations", () -> loadMutations(mutations));
            doStep("computing influencers", () -> TestsPool.v().computeInfluencer());
            doStep("loading buggy methods", () -> buggyMethodNames = loadBugMethods(bugMethodsFileName));
            
            for (final SuspTech which : SuspTech.values()) {
                doStep(String.format("ranking based on %s", which.techName), () -> {
                    final Set<Method> coveredMethods = loadCoveredMethods(coveredMethodsFileName);
                    calcualteMethodRanks(which, coveredMethods);
                });
            }
            
            
            final double[] inner_mean = new double[SuspTech.TECH_COUNT];
            final int[] min_rank = new int[SuspTech.TECH_COUNT];
            
            Arrays.fill(inner_mean, 0);
            Arrays.fill(min_rank, Integer.MAX_VALUE);
            
            final int[] inner_top1 = new int[SuspTech.TECH_COUNT];
            final int[] inner_top3 = new int[SuspTech.TECH_COUNT];
            final int[] inner_top5 = new int[SuspTech.TECH_COUNT];
            
            Arrays.fill(inner_top1, 0);
            Arrays.fill(inner_top3, 0);
            Arrays.fill(inner_top5, 0);
            
            for (final SuspTech which : SuspTech.values()) {
                final int index = which.ordinal();
                for (final String methName : buggyMethodNames) {
                    final Method meth = MethodsPool.v().getMethodByName(methName);
                    if (meth != null) {
                        final int rank = meth.getRank(which);
                        inner_mean[index] += rank;
                        min_rank[index] = Math.min(rank, min_rank[index]);
                        if (rank == 1) {
                            inner_top1[index] = 1;
                            inner_top3[index] = 1;
                            inner_top5[index] = 1;
                        } else if (rank <= 3) {
                            inner_top3[index] = 1;
                            inner_top5[index] = 1;
                        } else if (rank <= 5) {
                            inner_top5[index] = 1;
                        }
                    } else {
                        min_rank[index] = 0;
                    }
                }
                top1[index] += inner_top1[index];
                top3[index] += inner_top3[index];
                top5[index] += inner_top5[index];
                inner_mean[index] /= buggyMethodNames.size() > 0 ? buggyMethodNames.size() : 1;
                mar[index] += inner_mean[index];
                mfr[index] += buggyMethodNames.size() > 0 ? min_rank[index] : 0;    
            }
        }
        String out = "";
        for (final SuspTech which : SuspTech.values()) {
            final int index = which.ordinal();
            //System.out.println("\n----------------\n");
            //System.out.println(which.techName + ":");
            //System.out.println(String.format("\tTop-1 = %d, Top-3 = %d, Top-5 = %d", top1[index],
            //        top3[index],
            //        top5[index]));
            //System.out.println(String.format("\tMAR = %f, MFR = %f", mar[index] / verCount, mfr[index] / verCount));
            if (which != SuspTech.MUSE || Config.LEVEL == FailureDescriptorFactory.KIND) {
                out += String.format(",%d,%d,%d,%.1f,%.1f", top1[index],
                        top3[index],
                        top5[index],
                        Precision.round(mar[index] / verCount, 1),
                        Precision.round(mfr[index] / verCount, 1));
            } else {
                out += ",-1,-1,-1,-1,-1";
            }
        }
        out = out.substring(1);
        System.out.println(out);
    }
    
    private static void doStep(String message, Runnable r) {
        //final String ANSI_GREEN = "\u001B[32m";
        //final String ANSI_RESET = "\u001B[0m";
        //System.out.printf("%-50s%s", message + "...", "[    ]");
        r.run();
        //System.out.println(ANSI_GREEN + "\b\b\b\b\bDONE" + ANSI_RESET);
    }
    
    public static void calcualteMethodRanks(final SuspTech which, final Set<Method> coveredMethods) {
        final List<List<Method>> groups = coveredMethods.stream()
            .collect(Collectors.groupingBy(m -> m.getSusp(which)))
            .entrySet().stream()
            .sorted((e1, e2) -> Double.compare(e2.getKey(), e1.getKey()))
            .map(Map.Entry::getValue)
            .collect(Collectors.toList());
        int rank = 0;
        for (final List<Method> mg : groups) {
            rank += mg.size();
            for (final Method meth : mg) {
                meth.setRank(which, rank);
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
        return mutatorName.startsWith(Config.MUTATOR_PREFIX);
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
