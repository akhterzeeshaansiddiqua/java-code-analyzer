package com.codeanalyzer.analyzer;

import com.codeanalyzer.AnalysisIssue;
import com.codeanalyzer.AnalysisIssue.Severity;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Static analysis engine for Java source code.
 *
 * Runs rule-based checks across six categories:
 *   1. Security vulnerabilities
 *   2. Performance anti-patterns
 *   3. Bug-prone patterns
 *   4. Code style violations
 *   5. Exception handling issues
 *   6. Java best practices
 *
 * Each check returns zero or more AnalysisIssue objects with a severity,
 * line number, human-readable message, and fix suggestion.
 */
public class CodeAnalyzer {

    private static final Pattern PATTERN_SQL_CONCAT =
        Pattern.compile("\"\\s*\\+\\s*\\w+|\\w+\\s*\\+\\s*\".*(?i)(select|insert|update|delete|from|where)");
    private static final Pattern PATTERN_STRING_EQ =
        Pattern.compile("==\\s*\"|\"\\ *==");
    private static final Pattern PATTERN_EMPTY_CATCH =
        Pattern.compile("catch\\s*\\(.*\\)\\s*\\{\\s*\\}");
    private static final Pattern PATTERN_SYSTEM_OUT =
        Pattern.compile("System\\.out\\.print");
    private static final Pattern PATTERN_ARRAY_LIST_RAW =
        Pattern.compile("new\\s+ArrayList\\(\\)");
    private static final Pattern PATTERN_HARDCODED_IP =
        Pattern.compile("\"\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\"");

    public List<AnalysisIssue> analyze(String source) {
        List<AnalysisIssue> issues = new ArrayList<>();
        String[] lines = source.split("\n");

        runSecurityChecks(source, lines, issues);
        runPerformanceChecks(source, lines, issues);
        runBugChecks(source, lines, issues);
        runStyleChecks(source, lines, issues);
        runExceptionChecks(source, lines, issues);
        runBestPracticeChecks(source, lines, issues);

        return issues;
    }

    // -----------------------------------------------------------------------
    // Security
    // -----------------------------------------------------------------------
    private void runSecurityChecks(String src, String[] lines, List<AnalysisIssue> out) {

        // SQL Injection
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.contains("createStatement") || line.contains("executeQuery")) {
                if (containsStringConcat(lines, i)) {
                    out.add(new AnalysisIssue(Severity.ERROR,
                        "SQL Injection: query built with string concatenation",
                        "Use PreparedStatement with '?' placeholders instead",
                        i + 1, "security"));
                }
            }
        }

        // String comparison with ==
        for (int i = 0; i < lines.length; i++) {
            if (PATTERN_STRING_EQ.matcher(lines[i]).find() && !lines[i].trim().startsWith("//")) {
                out.add(new AnalysisIssue(Severity.ERROR,
                    "String compared with == (reference equality, not value equality)",
                    "Use .equals() or Objects.equals() for string comparison",
                    i + 1, "security"));
            }
        }

        // Hardcoded credentials
        for (int i = 0; i < lines.length; i++) {
            String lower = lines[i].toLowerCase();
            if ((lower.contains("password") || lower.contains("passwd") || lower.contains("secret"))
                    && lines[i].contains("=") && lines[i].contains("\"")) {
                out.add(new AnalysisIssue(Severity.ERROR,
                    "Hardcoded credential detected (password/secret in source code)",
                    "Move credentials to environment variables or a secrets manager",
                    i + 1, "security"));
            }
        }

        // Hardcoded IP addresses
        for (int i = 0; i < lines.length; i++) {
            if (PATTERN_HARDCODED_IP.matcher(lines[i]).find() && !lines[i].trim().startsWith("//")) {
                out.add(new AnalysisIssue(Severity.WARNING,
                    "Hardcoded IP address found",
                    "Move IP/host configuration to external config or environment variables",
                    i + 1, "security"));
            }
        }

        // Non-thread-safe singleton
        if (src.contains("getInstance") && src.contains("instance == null")
                && !src.contains("synchronized") && !src.contains("volatile")) {
            int line = findLineNumber(lines, "instance == null");
            out.add(new AnalysisIssue(Severity.WARNING,
                "Non-thread-safe Singleton: getInstance() not synchronized",
                "Add synchronized keyword or use double-checked locking with volatile",
                line, "security"));
        }
    }

    // -----------------------------------------------------------------------
    // Performance
    // -----------------------------------------------------------------------
    private void runPerformanceChecks(String src, String[] lines, List<AnalysisIssue> out) {

        // String concatenation in loops
        boolean inLoop = false;
        for (int i = 0; i < lines.length; i++) {
            String trim = lines[i].trim();
            if (trim.startsWith("for ") || trim.startsWith("while ") || trim.startsWith("do {")) {
                inLoop = true;
            }
            if (inLoop && (trim.contains("= result +") || trim.contains("+= \""))) {
                out.add(new AnalysisIssue(Severity.WARNING,
                    "String concatenation with + inside a loop (O(n²) allocations)",
                    "Use StringBuilder.append() to build strings in loops",
                    i + 1, "performance"));
            }
            if (trim.equals("}")) inLoop = false;
        }

        // Raw ArrayList / no diamond operator
        for (int i = 0; i < lines.length; i++) {
            if (PATTERN_ARRAY_LIST_RAW.matcher(lines[i]).find()) {
                out.add(new AnalysisIssue(Severity.INFO,
                    "Raw ArrayList instantiation — missing generic type parameter",
                    "Use new ArrayList<>() with the diamond operator",
                    i + 1, "performance"));
            }
        }

        // ArrayList used where interface type preferred
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains("ArrayList<") && lines[i].contains("ArrayList<")
                    && !lines[i].trim().startsWith("//")
                    && (lines[i].contains("private ") || lines[i].contains("public ") || lines[i].contains("protected "))) {
                out.add(new AnalysisIssue(Severity.INFO,
                    "Field declared as ArrayList<> — prefer the List<> interface type",
                    "Change to List<T> to program to the interface",
                    i + 1, "performance"));
            }
        }

        // List.contains() inside loop (O(n²))
        boolean inLoopCtx = false;
        for (int i = 0; i < lines.length; i++) {
            String trim = lines[i].trim();
            if (trim.startsWith("for ") || trim.startsWith("while ")) inLoopCtx = true;
            if (inLoopCtx && trim.contains(".contains(") && !trim.contains("Set")) {
                out.add(new AnalysisIssue(Severity.WARNING,
                    "List.contains() called inside a loop — O(n²) complexity",
                    "Use a HashSet for O(1) membership checks",
                    i + 1, "performance"));
                break;
            }
            if (trim.equals("}")) inLoopCtx = false;
        }

        // Index-based loop where for-each would suffice
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].matches(".*for\\s*\\(int\\s+\\w+\\s*=\\s*0.*\\.size\\(\\).*")
                    && !lines[i].contains("remove") && !lines[i].contains("set(")) {
                out.add(new AnalysisIssue(Severity.INFO,
                    "Index-based for-loop can be simplified to an enhanced for-each loop",
                    "Use 'for (Type item : collection)' when index is not needed",
                    i + 1, "performance"));
            }
        }
    }

    // -----------------------------------------------------------------------
    // Bugs
    // -----------------------------------------------------------------------
    private void runBugChecks(String src, String[] lines, List<AnalysisIssue> out) {

        // Null return in catch block
        for (int i = 0; i < lines.length; i++) {
            String trim = lines[i].trim();
            if (trim.equals("return null;")) {
                // Check if we're inside a catch block nearby
                for (int j = Math.max(0, i - 5); j < i; j++) {
                    if (lines[j].trim().startsWith("catch")) {
                        out.add(new AnalysisIssue(Severity.WARNING,
                            "Returning null inside a catch block — callers must null-check",
                            "Throw a domain exception or return Optional.empty() instead",
                            i + 1, "bugs"));
                        break;
                    }
                }
            }
        }

        // Mutable field exposed via getter
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].matches(".*public\\s+(List|ArrayList|HashMap|HashSet).*get\\w+.*\\(\\).*")) {
                out.add(new AnalysisIssue(Severity.WARNING,
                    "Mutable collection exposed directly via public getter",
                    "Return Collections.unmodifiableList() or a defensive copy",
                    i + 1, "bugs"));
            }
        }

        // Floating point equality comparison
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].matches(".*\\b(float|double)\\b.*==.*") && !lines[i].trim().startsWith("//")) {
                out.add(new AnalysisIssue(Severity.WARNING,
                    "Floating-point value compared with == (precision error risk)",
                    "Use Math.abs(a - b) < EPSILON for float/double comparisons",
                    i + 1, "bugs"));
            }
        }
    }

    // -----------------------------------------------------------------------
    // Style
    // -----------------------------------------------------------------------
    private void runStyleChecks(String src, String[] lines, List<AnalysisIssue> out) {

        // System.out.println usage
        for (int i = 0; i < lines.length; i++) {
            if (PATTERN_SYSTEM_OUT.matcher(lines[i]).find() && !lines[i].trim().startsWith("//")) {
                out.add(new AnalysisIssue(Severity.INFO,
                    "System.out used for logging — not suitable for production",
                    "Replace with a logging framework (SLF4J + Logback or Log4j2)",
                    i + 1, "style"));
            }
        }

        // TODO / FIXME comments
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains("// TODO") || lines[i].contains("// FIXME")) {
                out.add(new AnalysisIssue(Severity.INFO,
                    "Unresolved TODO/FIXME comment",
                    "Resolve the comment or track it as an issue in your issue tracker",
                    i + 1, "style"));
            }
        }

        // Deep nesting (> 4 levels of indentation)
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int spaces = line.length() - line.stripLeading().length();
            if (spaces >= 20 && !line.trim().isEmpty()) {
                out.add(new AnalysisIssue(Severity.INFO,
                    "Deep nesting detected (> 4 levels) — reduces readability",
                    "Apply early returns, guard clauses, or extract methods to reduce nesting",
                    i + 1, "style"));
                break; // Only report once per file
            }
        }

        // Magic numbers
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].matches(".*[^\\w]\\d{2,}[^\\w].*")
                    && !lines[i].trim().startsWith("//")
                    && !lines[i].contains("Thread.sleep")
                    && !lines[i].toLowerCase().contains("for")) {
                out.add(new AnalysisIssue(Severity.INFO,
                    "Magic number found in expression",
                    "Extract to a named constant: private static final int NAME = value;",
                    i + 1, "style"));
                break;
            }
        }
    }

    // -----------------------------------------------------------------------
    // Exception handling
    // -----------------------------------------------------------------------
    private void runExceptionChecks(String src, String[] lines, List<AnalysisIssue> out) {

        // Empty catch block
        for (int i = 0; i < lines.length - 1; i++) {
            String combined = lines[i].trim() + " " + lines[i + 1].trim();
            if (PATTERN_EMPTY_CATCH.matcher(combined).find()) {
                out.add(new AnalysisIssue(Severity.ERROR,
                    "Empty catch block — exception silently swallowed",
                    "Log the exception or re-throw as a domain exception",
                    i + 1, "exceptions"));
            }
        }

        // Catching Exception or Throwable (overly broad)
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].matches(".*catch\\s*\\(\\s*(Exception|Throwable)\\s+\\w+\\).*")) {
                out.add(new AnalysisIssue(Severity.WARNING,
                    "Overly broad catch clause: catching Exception/Throwable",
                    "Catch the specific exception type(s) you expect",
                    i + 1, "exceptions"));
            }
        }

        // printStackTrace (not production-safe)
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains("printStackTrace()")) {
                out.add(new AnalysisIssue(Severity.WARNING,
                    "e.printStackTrace() outputs to stderr only — not suitable for production",
                    "Use a logger: logger.error(\"message\", e);",
                    i + 1, "exceptions"));
            }
        }
    }

    // -----------------------------------------------------------------------
    // Best practices
    // -----------------------------------------------------------------------
    private void runBestPracticeChecks(String src, String[] lines, List<AnalysisIssue> out) {

        // Missing @Override annotation
        for (int i = 1; i < lines.length; i++) {
            String trim = lines[i].trim();
            if ((trim.startsWith("public ") || trim.startsWith("protected "))
                    && trim.contains("(") && !trim.contains("class ")
                    && (trim.contains("toString()") || trim.contains("equals(") || trim.contains("hashCode()"))) {
                if (!lines[i - 1].trim().contains("@Override")) {
                    out.add(new AnalysisIssue(Severity.INFO,
                        "Missing @Override annotation on overriding method",
                        "Add @Override to let the compiler verify the override",
                        i + 1, "best-practices"));
                }
            }
        }

        // Static field that should be final
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].matches(".*private\\s+static\\s+(?!final)\\w.*=.*;.*")
                    && !lines[i].trim().startsWith("//")) {
                out.add(new AnalysisIssue(Severity.INFO,
                    "Static field is not final — consider making it a constant",
                    "Add 'final' if the field value never changes after initialization",
                    i + 1, "best-practices"));
            }
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------
    private boolean containsStringConcat(String[] lines, int queryLine) {
        for (int i = Math.max(0, queryLine - 3); i <= Math.min(lines.length - 1, queryLine + 3); i++) {
            if (lines[i].contains("+ ") || lines[i].contains(" +")) return true;
        }
        return false;
    }

    private int findLineNumber(String[] lines, String pattern) {
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains(pattern)) return i + 1;
        }
        return -1;
    }
}
