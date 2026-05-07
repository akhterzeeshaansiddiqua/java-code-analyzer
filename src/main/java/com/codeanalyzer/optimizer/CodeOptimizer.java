package com.codeanalyzer.optimizer;

import com.codeanalyzer.OptimizationResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Suggests code optimizations and applies safe automated fixes.
 *
 * Each optimization includes:
 *   - A human-readable title and reason
 *   - A before/after code snippet pair
 *   - An impact level (high / medium / low)
 *   - A category (performance / security / style)
 *   - Whether it can be safely auto-applied
 */
public class CodeOptimizer {

    public List<OptimizationResult> suggest(String source) {
        List<OptimizationResult> results = new ArrayList<>();

        checkStringBuilderInLoop(source, results);
        checkDiamondOperator(source, results);
        checkListInterface(source, results);
        checkHashSetForContains(source, results);
        checkForEachLoop(source, results);
        checkPreparedStatement(source, results);
        checkNestedConditionals(source, results);
        checkStringIsEmpty(source, results);
        checkSingletonThreadSafety(source, results);
        checkExceptionLogging(source, results);

        return results;
    }

    /**
     * Apply all auto-fixable optimizations sequentially to the source code.
     */
    public String applyFixes(String source, List<OptimizationResult> opts) {
        String result = source;
        for (OptimizationResult opt : opts) {
            if (opt.isAutoFixable() && result.contains(opt.getBefore())) {
                result = result.replace(opt.getBefore(), opt.getAfter());
            }
        }
        return result;
    }

    // -----------------------------------------------------------------------
    // Individual optimization checks
    // -----------------------------------------------------------------------

    private void checkStringBuilderInLoop(String src, List<OptimizationResult> out) {
        if ((src.contains("result = result +") || src.contains("+= \""))
                && (src.contains("for ") || src.contains("while "))) {
            out.add(new OptimizationResult(
                "Replace String + in loop with StringBuilder",
                "Eliminates O(n²) String allocation; significantly improves performance for large loops",
                "String result = \"\";\nwhile (rs.next()) {\n    result = result + rs.getString(\"name\") + \",\";\n}",
                "StringBuilder sb = new StringBuilder();\nwhile (rs.next()) {\n    sb.append(rs.getString(\"name\")).append(\",\");\n}\nString result = sb.toString();",
                "high", "performance", false
            ));
        }
    }

    private void checkDiamondOperator(String src, List<OptimizationResult> out) {
        if (src.matches("(?s).*new\\s+ArrayList<\\w+>\\(\\).*")) {
            out.add(new OptimizationResult(
                "Use diamond operator <> for generic instantiation",
                "Removes redundant type parameter; type is inferred from the declaration (Java 7+)",
                "new ArrayList<String>()",
                "new ArrayList<>()",
                "low", "style", true
            ));
        }
    }

    private void checkListInterface(String src, List<OptimizationResult> out) {
        if (src.matches("(?s).*(private|public|protected)\\s+ArrayList<.*")) {
            out.add(new OptimizationResult(
                "Program to the List<T> interface instead of ArrayList<T>",
                "Decouples code from concrete implementation; allows swapping to LinkedList etc. without changing callers",
                "private ArrayList<String> students = new ArrayList<>();",
                "private List<String> students = new ArrayList<>();",
                "medium", "style", false
            ));
        }
    }

    private void checkHashSetForContains(String src, List<OptimizationResult> out) {
        if (src.contains(".contains(") && (src.contains("ArrayList") || src.contains("List<"))
                && !src.contains("HashSet") && !src.contains("Set<")) {
            out.add(new OptimizationResult(
                "Use LinkedHashSet to remove duplicates in O(n)",
                "List.contains() inside a loop is O(n²). A Set gives O(1) lookups and O(n) overall",
                "ArrayList<String> unique = new ArrayList<>();\nfor (String s : students) {\n    if (!unique.contains(s)) unique.add(s);\n}\nstudents = unique;",
                "Set<String> seen = new LinkedHashSet<>(students);\nstudents = new ArrayList<>(seen);",
                "high", "performance", false
            ));
        }
    }

    private void checkForEachLoop(String src, List<OptimizationResult> out) {
        if (src.matches("(?s).*for\\s*\\(int\\s+i\\s*=\\s*0.*\\.size\\(\\).*")) {
            out.add(new OptimizationResult(
                "Simplify index loop to enhanced for-each",
                "More readable; avoids repeated .size() evaluation; works with any Iterable",
                "for (int i = 0; i < list.size(); i++) {\n    String item = list.get(i);\n    // use item\n}",
                "for (String item : list) {\n    // use item\n}",
                "low", "style", false
            ));
        }
    }

    private void checkPreparedStatement(String src, List<OptimizationResult> out) {
        if (src.contains("createStatement") && (src.contains("+ userId") || src.contains("+ id"))) {
            out.add(new OptimizationResult(
                "Use PreparedStatement to prevent SQL Injection",
                "Parameterized queries eliminate injection risk and improve DB query plan caching",
                "String query = \"SELECT * FROM users WHERE id = \" + userId;\nStatement stmt = conn.createStatement();\nResultSet rs = stmt.executeQuery(query);",
                "String query = \"SELECT * FROM users WHERE id = ?\";\nPreparedStatement ps = conn.prepareStatement(query);\nps.setString(1, userId);\nResultSet rs = ps.executeQuery();",
                "high", "security", false
            ));
        }
    }

    private void checkNestedConditionals(String src, List<OptimizationResult> out) {
        String[] lines = src.split("\n");
        for (int i = 0; i < lines.length - 3; i++) {
            if (lines[i].trim().startsWith("if (") && lines[i + 1].trim().startsWith("if (")) {
                out.add(new OptimizationResult(
                    "Merge nested if conditions into a single guard clause",
                    "Reduces nesting depth, improves readability, and enables early return pattern",
                    "if (name != null) {\n    if (name.length() > 0) {\n        students.add(name);\n    }\n}",
                    "if (name != null && !name.isBlank()) {\n    students.add(name);\n}",
                    "medium", "style", false
                ));
                break;
            }
        }
    }

    private void checkStringIsEmpty(String src, List<OptimizationResult> out) {
        if (src.contains(".length() > 0") || src.contains(".length() == 0")) {
            out.add(new OptimizationResult(
                "Use String.isBlank() or isEmpty() instead of length() check",
                "More expressive; isBlank() also handles whitespace-only strings (Java 11+)",
                "if (name.length() > 0)",
                "if (!name.isBlank())",
                "low", "style", true
            ));
        }
    }

    private void checkSingletonThreadSafety(String src, List<OptimizationResult> out) {
        if (src.contains("getInstance") && src.contains("instance == null")
                && !src.contains("synchronized") && !src.contains("volatile")) {
            out.add(new OptimizationResult(
                "Make Singleton thread-safe with double-checked locking",
                "Prevents multiple instance creation in concurrent environments without full synchronization overhead",
                "private static UserService instance;\n\npublic static UserService getInstance() {\n    if (instance == null) {\n        instance = new UserService();\n    }\n    return instance;\n}",
                "private static volatile UserService instance;\n\npublic static UserService getInstance() {\n    if (instance == null) {\n        synchronized (UserService.class) {\n            if (instance == null) {\n                instance = new UserService();\n            }\n        }\n    }\n    return instance;\n}",
                "high", "security", false
            ));
        }
    }

    private void checkExceptionLogging(String src, List<OptimizationResult> out) {
        if (src.contains("printStackTrace()")) {
            out.add(new OptimizationResult(
                "Replace printStackTrace() with SLF4J logger",
                "Structured logging is configurable, filterable, and suitable for production environments",
                "} catch (Exception e) {\n    e.printStackTrace();\n    return null;\n}",
                "} catch (Exception e) {\n    logger.error(\"Failed to execute query\", e);\n    throw new ServiceException(\"Data retrieval failed\", e);\n}",
                "medium", "exceptions", false
            ));
        }
    }
}
