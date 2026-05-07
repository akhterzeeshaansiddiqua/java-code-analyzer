package com.codeanalyzer;

import com.codeanalyzer.analyzer.CodeAnalyzer;
import com.codeanalyzer.metrics.MetricsCalculator;
import com.codeanalyzer.optimizer.CodeOptimizer;
import com.codeanalyzer.report.ReportGenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Intelligent Java Code Analyzer & Optimizer
 *
 * Entry point for the CLI tool. Accepts a Java source file path,
 * runs full static analysis, computes metrics, suggests optimizations,
 * and generates an HTML report.
 *
 * Usage:
 *   java -jar code-analyzer.jar <path/to/File.java> [--report] [--fix]
 */
public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            printUsage();
            System.exit(1);
        }

        String filePath = args[0];
        boolean generateReport = containsFlag(args, "--report");
        boolean autoFix        = containsFlag(args, "--fix");
        boolean verbose        = containsFlag(args, "--verbose");

        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            System.err.println("[ERROR] File not found: " + filePath);
            System.exit(1);
        }

        String sourceCode = Files.readString(path);
        String fileName   = path.getFileName().toString();

        System.out.println("=".repeat(60));
        System.out.println("  Intelligent Java Code Analyzer & Optimizer");
        System.out.println("  File: " + fileName);
        System.out.println("=".repeat(60));

        // Run analysis pipeline
        CodeAnalyzer    analyzer  = new CodeAnalyzer();
        MetricsCalculator metrics = new MetricsCalculator();
        CodeOptimizer   optimizer = new CodeOptimizer();

        List<AnalysisIssue>      issues         = analyzer.analyze(sourceCode);
        CodeMetrics              codeMetrics    = metrics.calculate(sourceCode);
        List<OptimizationResult> optimizations  = optimizer.suggest(sourceCode);

        // Print results to console
        printMetrics(codeMetrics, verbose);
        printIssues(issues);
        printOptimizations(optimizations);

        // Quality score
        int score = calculateScore(issues, codeMetrics);
        System.out.println("\n" + "=".repeat(60));
        System.out.printf("  Quality Score: %d / 100   (%s)%n", score, getGrade(score));
        System.out.println("=".repeat(60));

        // Optional: generate HTML report
        if (generateReport) {
            ReportGenerator generator = new ReportGenerator();
            String reportPath = filePath.replace(".java", "_report.html");
            generator.generate(fileName, sourceCode, issues, optimizations, codeMetrics, score, reportPath);
            System.out.println("\n[INFO] Report saved to: " + reportPath);
        }

        // Optional: apply safe auto-fixes
        if (autoFix) {
            String fixed = optimizer.applyFixes(sourceCode, optimizations);
            String fixedPath = filePath.replace(".java", "_optimized.java");
            Files.writeString(Paths.get(fixedPath), fixed);
            System.out.println("[INFO] Optimized file saved to: " + fixedPath);
        }
    }

    private static void printMetrics(CodeMetrics m, boolean verbose) {
        System.out.println("\n--- Code Metrics ---");
        System.out.printf("  Lines of code      : %d%n", m.getLineCount());
        System.out.printf("  Methods            : %d%n", m.getMethodCount());
        System.out.printf("  Classes            : %d%n", m.getClassCount());
        System.out.printf("  Cyclomatic complex.: %d%n", m.getCyclomaticComplexity());
        System.out.printf("  Comment density    : %.1f%%%n", m.getCommentDensity());
        if (verbose) {
            System.out.printf("  Max nesting depth  : %d%n", m.getMaxNestingDepth());
            System.out.printf("  Avg method length  : %.1f lines%n", m.getAvgMethodLength());
            System.out.printf("  TODO count         : %d%n", m.getTodoCount());
        }
    }

    private static void printIssues(List<AnalysisIssue> issues) {
        if (issues.isEmpty()) {
            System.out.println("\n--- Issues: None found ✓ ---");
            return;
        }
        System.out.println("\n--- Issues (" + issues.size() + ") ---");
        for (AnalysisIssue issue : issues) {
            String icon = switch (issue.getSeverity()) {
                case ERROR   -> "[ERROR]";
                case WARNING -> "[WARN] ";
                case INFO    -> "[INFO] ";
            };
            System.out.printf("  %s Line %-4s %s%n",
                icon,
                issue.getLine() > 0 ? issue.getLine() + ":" : "?:  ",
                issue.getMessage());
            if (issue.getSuggestion() != null) {
                System.out.printf("          → %s%n", issue.getSuggestion());
            }
        }
    }

    private static void printOptimizations(List<OptimizationResult> opts) {
        if (opts.isEmpty()) {
            System.out.println("\n--- Optimizations: None suggested ---");
            return;
        }
        System.out.println("\n--- Optimizations (" + opts.size() + ") ---");
        for (OptimizationResult opt : opts) {
            System.out.printf("  [%s] %s%n", opt.getImpact().toUpperCase(), opt.getTitle());
            System.out.printf("          %s%n", opt.getReason());
        }
    }

    private static int calculateScore(List<AnalysisIssue> issues, CodeMetrics metrics) {
        int score = 100;
        for (AnalysisIssue issue : issues) {
            score -= switch (issue.getSeverity()) {
                case ERROR   -> 18;
                case WARNING -> 8;
                case INFO    -> 3;
            };
        }
        if (metrics.getCyclomaticComplexity() > 10) score -= 5;
        if (metrics.getCommentDensity() < 5.0)      score -= 5;
        return Math.max(0, Math.min(100, score));
    }

    private static String getGrade(int score) {
        if (score >= 90) return "Excellent";
        if (score >= 75) return "Good";
        if (score >= 55) return "Needs Work";
        return "Critical Issues";
    }

    private static boolean containsFlag(String[] args, String flag) {
        for (String arg : args) if (arg.equals(flag)) return true;
        return false;
    }

    private static void printUsage() {
        System.out.println("Usage: java -jar code-analyzer.jar <File.java> [options]");
        System.out.println("Options:");
        System.out.println("  --report    Generate HTML report");
        System.out.println("  --fix       Apply safe auto-fixes and save optimized file");
        System.out.println("  --verbose   Show extended metrics");
    }
}
