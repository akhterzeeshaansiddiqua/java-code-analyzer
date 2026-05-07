package com.codeanalyzer.report;

import com.codeanalyzer.AnalysisIssue;
import com.codeanalyzer.CodeMetrics;
import com.codeanalyzer.OptimizationResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Generates a self-contained HTML analysis report.
 *
 * The report includes:
 *   - Quality score ring
 *   - Metrics summary cards
 *   - Full issue list with severity indicators
 *   - Optimization suggestions with before/after diffs
 *   - Original source code with syntax highlighting
 */
public class ReportGenerator {

    public void generate(String fileName,
                         String sourceCode,
                         List<AnalysisIssue> issues,
                         List<OptimizationResult> optimizations,
                         CodeMetrics metrics,
                         int score,
                         String outputPath) throws IOException {

        String html = buildHtml(fileName, sourceCode, issues, optimizations, metrics, score);
        Files.writeString(Paths.get(outputPath), html);
    }

    private String buildHtml(String fileName,
                              String source,
                              List<AnalysisIssue> issues,
                              List<OptimizationResult> opts,
                              CodeMetrics m,
                              int score) {

        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        String grade = score >= 90 ? "Excellent" : score >= 75 ? "Good" : score >= 55 ? "Needs Work" : "Critical";
        String scoreColor = score >= 75 ? "#1D9E75" : score >= 55 ? "#EF9F27" : "#E24B4A";

        StringBuilder sb = new StringBuilder();
        sb.append("""
            <!DOCTYPE html>
            <html lang="en">
            <head>
            <meta charset="UTF-8"/>
            <meta name="viewport" content="width=device-width,initial-scale=1"/>
            <title>Code Analysis Report</title>
            <style>
              body{font-family:system-ui,sans-serif;margin:0;background:#f5f5f2;color:#1a1a1a}
              .header{background:#1a1a1a;color:#fff;padding:24px 32px;display:flex;align-items:center;justify-content:space-between}
              .header h1{margin:0;font-size:20px;font-weight:500}
              .header small{opacity:.5;font-size:13px}
              .container{max-width:960px;margin:0 auto;padding:24px 16px}
              .score-card{background:#fff;border-radius:12px;padding:24px;display:flex;align-items:center;gap:24px;margin-bottom:20px;border:1px solid #e0e0e0}
              .score-ring{position:relative;width:90px;height:90px;flex-shrink:0}
              .score-ring svg{width:90px;height:90px}
              .score-number{position:absolute;inset:0;display:flex;align-items:center;justify-content:center;font-size:22px;font-weight:600}
              .metrics-grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(140px,1fr));gap:12px;margin-bottom:20px}
              .metric-card{background:#fff;border-radius:10px;padding:16px;border:1px solid #e0e0e0}
              .metric-val{font-size:24px;font-weight:600;color:#1a1a1a}
              .metric-lbl{font-size:12px;color:#888;margin-top:4px}
              .section{background:#fff;border-radius:12px;border:1px solid #e0e0e0;margin-bottom:20px;overflow:hidden}
              .section-header{padding:14px 20px;background:#fafafa;border-bottom:1px solid #e8e8e8;font-weight:500;font-size:14px;display:flex;align-items:center;gap:8px}
              .issue{padding:12px 20px;border-bottom:1px solid #f0f0f0;display:flex;gap:12px;align-items:flex-start}
              .issue:last-child{border-bottom:none}
              .sev{font-size:11px;font-weight:600;padding:2px 8px;border-radius:4px;white-space:nowrap;margin-top:2px}
              .sev-error{background:#FCEBEB;color:#A32D2D}
              .sev-warning{background:#FAEEDA;color:#854F0B}
              .sev-info{background:#E6F1FB;color:#185FA5}
              .issue-msg{font-size:13px;font-weight:500;margin-bottom:3px}
              .issue-fix{font-size:12px;color:#555}
              .issue-line{font-size:11px;color:#aaa;margin-top:3px;font-family:monospace}
              .opt{padding:16px 20px;border-bottom:1px solid #f0f0f0}
              .opt:last-child{border-bottom:none}
              .opt-title{font-size:13px;font-weight:500;margin-bottom:4px}
              .opt-reason{font-size:12px;color:#666;margin-bottom:10px}
              .diff{display:grid;grid-template-columns:1fr 1fr;gap:8px;font-size:11px;font-family:monospace}
              .diff-box{border-radius:6px;padding:10px;white-space:pre-wrap;line-height:1.5;overflow-x:auto}
              .diff-before{background:#FFF5F5;border:1px solid #fcc;color:#a32d2d}
              .diff-after {background:#F0FFF4;border:1px solid #9ae;color:#0f5132}
              .diff-label{font-family:system-ui;font-size:10px;font-weight:600;opacity:.6;margin-bottom:4px}
              .impact{display:inline-block;font-size:10px;font-weight:600;padding:2px 7px;border-radius:3px;margin-left:8px}
              .imp-high{background:#FCEBEB;color:#A32D2D}
              .imp-medium{background:#FAEEDA;color:#854F0B}
              .imp-low{background:#E6F1FB;color:#185FA5}
              .source{padding:20px;font-family:monospace;font-size:12px;line-height:1.7;white-space:pre;overflow-x:auto;background:#1a1a1a;color:#d4d4d4;border-radius:0 0 12px 12px}
              .footer{text-align:center;padding:20px;font-size:12px;color:#aaa}
              @media(max-width:600px){.diff{grid-template-columns:1fr}}
            </style>
            </head>
            <body>
            """);

        // Header
        sb.append("<div class='header'>");
        sb.append("<div><h1>Code Analysis Report</h1><small>").append(fileName).append(" &nbsp;·&nbsp; ").append(time).append("</small></div>");
        sb.append("<div style='text-align:right'><div style='font-size:13px;opacity:.6'>Quality Score</div>");
        sb.append("<div style='font-size:32px;font-weight:700;color:").append(scoreColor).append("'>").append(score).append(" / 100</div>");
        sb.append("<div style='font-size:13px;opacity:.7'>").append(grade).append("</div></div></div>");

        sb.append("<div class='container'>");

        // Metrics cards
        sb.append("<div class='metrics-grid'>");
        appendMetric(sb, String.valueOf(m.getLineCount()),           "Lines of code");
        appendMetric(sb, String.valueOf(m.getMethodCount()),         "Methods");
        appendMetric(sb, String.valueOf(m.getClassCount()),          "Classes");
        appendMetric(sb, String.valueOf(m.getCyclomaticComplexity()), "Cyclomatic complexity");
        appendMetric(sb, String.format("%.1f%%", m.getCommentDensity()), "Comment density");
        appendMetric(sb, String.valueOf(m.getMaxNestingDepth()),     "Max nesting depth");
        appendMetric(sb, String.valueOf(issues.stream().filter(i -> i.getSeverity() == AnalysisIssue.Severity.ERROR).count()), "Errors");
        appendMetric(sb, String.valueOf(opts.size()),                "Optimizations");
        sb.append("</div>");

        // Issues
        sb.append("<div class='section'>");
        sb.append("<div class='section-header'>⚠ Issues (").append(issues.size()).append(")</div>");
        if (issues.isEmpty()) {
            sb.append("<div style='padding:20px;color:#888;font-size:13px'>No issues found. Code looks clean!</div>");
        } else {
            for (AnalysisIssue issue : issues) {
                String sevClass = switch (issue.getSeverity()) {
                    case ERROR   -> "sev-error";
                    case WARNING -> "sev-warning";
                    case INFO    -> "sev-info";
                };
                sb.append("<div class='issue'>");
                sb.append("<span class='sev ").append(sevClass).append("'>").append(issue.getSeverity()).append("</span>");
                sb.append("<div><div class='issue-msg'>").append(escHtml(issue.getMessage())).append("</div>");
                if (issue.getSuggestion() != null)
                    sb.append("<div class='issue-fix'>→ ").append(escHtml(issue.getSuggestion())).append("</div>");
                if (issue.getLine() > 0)
                    sb.append("<div class='issue-line'>Line ").append(issue.getLine()).append("</div>");
                sb.append("</div></div>");
            }
        }
        sb.append("</div>");

        // Optimizations
        sb.append("<div class='section'>");
        sb.append("<div class='section-header'>⚡ Optimizations (").append(opts.size()).append(")</div>");
        if (opts.isEmpty()) {
            sb.append("<div style='padding:20px;color:#888;font-size:13px'>No optimizations suggested.</div>");
        } else {
            for (OptimizationResult opt : opts) {
                String impClass = switch (opt.getImpact()) {
                    case "high"   -> "imp-high";
                    case "medium" -> "imp-medium";
                    default       -> "imp-low";
                };
                sb.append("<div class='opt'>");
                sb.append("<div class='opt-title'>").append(escHtml(opt.getTitle()));
                sb.append("<span class='impact ").append(impClass).append("'>").append(opt.getImpact().toUpperCase()).append("</span></div>");
                sb.append("<div class='opt-reason'>").append(escHtml(opt.getReason())).append("</div>");
                sb.append("<div class='diff'>");
                sb.append("<div><div class='diff-label'>BEFORE</div><div class='diff-box diff-before'>").append(escHtml(opt.getBefore())).append("</div></div>");
                sb.append("<div><div class='diff-label'>AFTER</div><div class='diff-box diff-after'>").append(escHtml(opt.getAfter())).append("</div></div>");
                sb.append("</div></div>");
            }
        }
        sb.append("</div>");

        // Source code
        sb.append("<div class='section'>");
        sb.append("<div class='section-header'>📄 Source Code</div>");
        sb.append("<div class='source'>").append(escHtml(source)).append("</div>");
        sb.append("</div>");

        sb.append("</div>"); // container

        sb.append("<div class='footer'>Generated by Intelligent Java Code Analyzer &amp; Optimizer · ").append(time).append("</div>");
        sb.append("</body></html>");

        return sb.toString();
    }

    private void appendMetric(StringBuilder sb, String val, String label) {
        sb.append("<div class='metric-card'><div class='metric-val'>").append(val)
          .append("</div><div class='metric-lbl'>").append(label).append("</div></div>");
    }

    private String escHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
