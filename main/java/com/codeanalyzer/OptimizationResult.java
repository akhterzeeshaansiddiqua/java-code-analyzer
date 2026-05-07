package com.codeanalyzer;

/**
 * Represents a single code optimization suggestion with before/after snippets.
 */
public class OptimizationResult {

    private final String title;
    private final String reason;
    private final String before;
    private final String after;
    private final String impact;   // "high", "medium", "low"
    private final String category; // "performance", "security", "style"
    private final boolean autoFixable;

    public OptimizationResult(String title, String reason,
                               String before, String after,
                               String impact, String category,
                               boolean autoFixable) {
        this.title        = title;
        this.reason       = reason;
        this.before       = before;
        this.after        = after;
        this.impact       = impact;
        this.category     = category;
        this.autoFixable  = autoFixable;
    }

    public String  getTitle()       { return title;       }
    public String  getReason()      { return reason;      }
    public String  getBefore()      { return before;      }
    public String  getAfter()       { return after;       }
    public String  getImpact()      { return impact;      }
    public String  getCategory()    { return category;    }
    public boolean isAutoFixable()  { return autoFixable; }
}
