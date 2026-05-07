package com.codeanalyzer.metrics;

import com.codeanalyzer.CodeMetrics;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Computes software quality metrics from Java source code.
 *
 * Metrics computed:
 *   - Line count (total, blank, comment, code)
 *   - Method count and average method length
 *   - Class count
 *   - Cyclomatic complexity (McCabe)
 *   - Comment density (%)
 *   - Maximum nesting depth
 *   - TODO/FIXME count
 *   - Import count and field count
 */
public class MetricsCalculator {

    private static final Pattern METHOD_PATTERN =
        Pattern.compile("\\b(public|private|protected)\\s+[\\w<>\\[\\]]+\\s+\\w+\\s*\\(");
    private static final Pattern CLASS_PATTERN =
        Pattern.compile("\\bclass\\s+\\w+");
    private static final Pattern DECISION_PATTERN =
        Pattern.compile("\\b(if|for|while|case|catch|&&|\\|\\|)\\b");
    private static final Pattern IMPORT_PATTERN =
        Pattern.compile("^\\s*import\\s+");
    private static final Pattern FIELD_PATTERN =
        Pattern.compile("^\\s*(private|protected|public)\\s+(?!class|interface|enum)\\w[\\w<>\\[\\]]*\\s+\\w+\\s*[=;]");

    public CodeMetrics calculate(String source) {
        String[] lines = source.split("\n");

        int lineCount      = lines.length;
        int commentLines   = 0;
        int methodCount    = 0;
        int classCount     = 0;
        int todoCount      = 0;
        int importCount    = 0;
        int fieldCount     = 0;
        int totalMethodLen = 0;
        int currentMethodLen = 0;
        boolean inMethod   = false;
        boolean inBlock    = false;
        int braceDepth     = 0;
        int methodStartDepth = 0;
        int maxDepth       = 0;
        int currentDepth   = 0;

        for (String line : lines) {
            String trim = line.trim();

            // Comment counting
            if (trim.startsWith("//") || trim.startsWith("*") || trim.startsWith("/*")) {
                commentLines++;
            }

            // TODO/FIXME
            if (trim.contains("// TODO") || trim.contains("// FIXME")) todoCount++;

            // Imports
            if (IMPORT_PATTERN.matcher(trim).find()) importCount++;

            // Fields
            if (FIELD_PATTERN.matcher(trim).find()) fieldCount++;

            // Methods
            Matcher mMethod = METHOD_PATTERN.matcher(line);
            if (mMethod.find() && !trim.contains("abstract ")) {
                methodCount++;
                if (inMethod) totalMethodLen += currentMethodLen;
                currentMethodLen = 0;
                inMethod = true;
                methodStartDepth = braceDepth;
            }

            // Nesting depth via brace counting
            for (char c : line.toCharArray()) {
                if (c == '{') { braceDepth++; currentDepth++; }
                if (c == '}') { braceDepth = Math.max(0, braceDepth - 1); currentDepth = Math.max(0, currentDepth - 1); }
            }
            maxDepth = Math.max(maxDepth, currentDepth);

            if (inMethod) currentMethodLen++;
        }
        if (inMethod) totalMethodLen += currentMethodLen;

        // Classes
        Matcher mClass = CLASS_PATTERN.matcher(source);
        while (mClass.find()) classCount++;

        // Cyclomatic complexity: 1 + number of decision points
        Matcher mDecision = DECISION_PATTERN.matcher(source);
        int decisions = 0;
        while (mDecision.find()) decisions++;
        int cyclomaticComplexity = 1 + decisions;

        double commentDensity = lineCount > 0 ? (commentLines * 100.0 / lineCount) : 0.0;
        double avgMethodLen   = methodCount > 0 ? (totalMethodLen * 1.0 / methodCount) : 0.0;

        return new CodeMetrics.Builder()
            .lineCount(lineCount)
            .methodCount(methodCount)
            .classCount(classCount)
            .cyclomaticComplexity(Math.min(cyclomaticComplexity, 50))
            .commentDensity(Math.round(commentDensity * 10.0) / 10.0)
            .maxNestingDepth(maxDepth)
            .avgMethodLength(Math.round(avgMethodLen * 10.0) / 10.0)
            .todoCount(todoCount)
            .importCount(importCount)
            .fieldCount(fieldCount)
            .build();
    }
}
