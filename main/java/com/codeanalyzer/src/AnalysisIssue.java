package com.codeanalyzer;

/**
 * Represents a single static analysis finding in the source code.
 */
public class AnalysisIssue {

    public enum Severity { ERROR, WARNING, INFO }

    private final Severity severity;
    private final String   message;
    private final String   suggestion;
    private final int      line;
    private final String   category;

    public AnalysisIssue(Severity severity, String message, String suggestion, int line, String category) {
        this.severity   = severity;
        this.message    = message;
        this.suggestion = suggestion;
        this.line       = line;
        this.category   = category;
    }

    public Severity getSeverity()   { return severity;   }
    public String   getMessage()    { return message;    }
    public String   getSuggestion() { return suggestion; }
    public int      getLine()       { return line;       }
    public String   getCategory()   { return category;   }

    @Override
    public String toString() {
        return String.format("[%s] Line %d: %s", severity, line, message);
    }
}
