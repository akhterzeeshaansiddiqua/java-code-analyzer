package com.codeanalyzer;

import com.codeanalyzer.analyzer.CodeAnalyzer;
import com.codeanalyzer.metrics.MetricsCalculator;
import com.codeanalyzer.optimizer.CodeOptimizer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Code Analyzer, Metrics Calculator, and Optimizer.
 */
class CodeAnalyzerTest {

    private final CodeAnalyzer     analyzer  = new CodeAnalyzer();
    private final MetricsCalculator metrics  = new MetricsCalculator();
    private final CodeOptimizer    optimizer = new CodeOptimizer();

    // -----------------------------------------------------------------------
    // CodeAnalyzer tests
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("SQL injection via string concatenation is flagged as ERROR")
    void testSqlInjectionDetected() {
        String code = """
            String query = "SELECT * FROM users WHERE id = " + userId;
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            """;
        List<AnalysisIssue> issues = analyzer.analyze(code);
        assertTrue(issues.stream().anyMatch(i ->
            i.getSeverity() == AnalysisIssue.Severity.ERROR &&
            i.getMessage().contains("SQL Injection")));
    }

    @Test
    @DisplayName("String compared with == is flagged as ERROR")
    void testStringEqualityDetected() {
        String code = "if (username == \"admin\") { return true; }";
        List<AnalysisIssue> issues = analyzer.analyze(code);
        assertTrue(issues.stream().anyMatch(i ->
            i.getSeverity() == AnalysisIssue.Severity.ERROR &&
            i.getMessage().contains("String compared with ==")));
    }

    @Test
    @DisplayName("Hardcoded password is flagged as ERROR")
    void testHardcodedCredentialDetected() {
        String code = "String password = \"admin123\";";
        List<AnalysisIssue> issues = analyzer.analyze(code);
        assertTrue(issues.stream().anyMatch(i ->
            i.getSeverity() == AnalysisIssue.Severity.ERROR &&
            i.getMessage().contains("Hardcoded credential")));
    }

    @Test
    @DisplayName("Empty catch block is flagged as ERROR")
    void testEmptyCatchDetected() {
        String code = """
            try {
                doSomething();
            } catch (Exception e) {}
            """;
        List<AnalysisIssue> issues = analyzer.analyze(code);
        assertTrue(issues.stream().anyMatch(i ->
            i.getSeverity() == AnalysisIssue.Severity.ERROR &&
            i.getMessage().contains("Empty catch block")));
    }

    @Test
    @DisplayName("System.out.println usage is flagged as INFO")
    void testSystemOutFlagged() {
        String code = "System.out.println(\"hello\");";
        List<AnalysisIssue> issues = analyzer.analyze(code);
        assertTrue(issues.stream().anyMatch(i ->
            i.getSeverity() == AnalysisIssue.Severity.INFO &&
            i.getMessage().contains("System.out")));
    }

    @Test
    @DisplayName("Clean code produces no issues")
    void testCleanCodeNoIssues() {
        String code = """
            public class Clean {
                private static final Logger logger = LoggerFactory.getLogger(Clean.class);
                private List<String> items = new ArrayList<>();

                public List<String> getItems() {
                    return Collections.unmodifiableList(items);
                }
            }
            """;
        List<AnalysisIssue> issues = analyzer.analyze(code);
        long errors = issues.stream()
            .filter(i -> i.getSeverity() == AnalysisIssue.Severity.ERROR)
            .count();
        assertEquals(0, errors, "Clean code should have no ERROR-level issues");
    }

    // -----------------------------------------------------------------------
    // MetricsCalculator tests
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Line count is accurate")
    void testLineCount() {
        String code = "line1\nline2\nline3\n";
        CodeMetrics m = metrics.calculate(code);
        assertEquals(3, m.getLineCount());
    }

    @Test
    @DisplayName("Method count is accurate")
    void testMethodCount() {
        String code = """
            public class Foo {
                public void methodA() {}
                private String methodB(int x) { return ""; }
                protected int methodC() { return 0; }
            }
            """;
        CodeMetrics m = metrics.calculate(code);
        assertEquals(3, m.getMethodCount());
    }

    @Test
    @DisplayName("Class count is accurate")
    void testClassCount() {
        String code = "class A {}\nclass B {}\n";
        CodeMetrics m = metrics.calculate(code);
        assertEquals(2, m.getClassCount());
    }

    @Test
    @DisplayName("Cyclomatic complexity counts decision points")
    void testCyclomaticComplexity() {
        String code = """
            if (a) { }
            if (b) { }
            while (c) { }
            for (int i=0;i<10;i++) { }
            """;
        CodeMetrics m = metrics.calculate(code);
        assertTrue(m.getCyclomaticComplexity() >= 5,
            "Should count at least 4 decision points + 1 base");
    }

    @Test
    @DisplayName("TODO count is accurate")
    void testTodoCount() {
        String code = "// TODO fix this\n// TODO and this\nint x = 1;\n";
        CodeMetrics m = metrics.calculate(code);
        assertEquals(2, m.getTodoCount());
    }

    // -----------------------------------------------------------------------
    // CodeOptimizer tests
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("StringBuilder optimization suggested for loop + concat")
    void testStringBuilderSuggestion() {
        String code = """
            String result = "";
            while (rs.next()) {
                result = result + rs.getString("name") + ",";
            }
            """;
        List<OptimizationResult> opts = optimizer.suggest(code);
        assertTrue(opts.stream().anyMatch(o -> o.getTitle().contains("StringBuilder")));
    }

    @Test
    @DisplayName("PreparedStatement optimization suggested for SQL concat")
    void testPreparedStatementSuggestion() {
        String code = """
            Statement stmt = conn.createStatement();
            String query = "SELECT * FROM users WHERE id = " + userId;
            """;
        List<OptimizationResult> opts = optimizer.suggest(code);
        assertTrue(opts.stream().anyMatch(o -> o.getTitle().contains("PreparedStatement")));
    }

    @Test
    @DisplayName("Auto-fixable optimizations are applied by applyFixes()")
    void testAutoFix() {
        String code = "if (name.length() > 0) { list.add(name); }";
        List<OptimizationResult> opts = optimizer.suggest(code);
        String fixed = optimizer.applyFixes(code, opts);
        assertNotEquals(code, fixed, "applyFixes should modify the code for auto-fixable optimizations");
    }

    @Test
    @DisplayName("Empty code produces no optimizations")
    void testEmptyCodeNoOptimizations() {
        List<OptimizationResult> opts = optimizer.suggest("");
        assertTrue(opts.isEmpty());
    }
}
