package com.codeanalyzer;

/**
 * Holds computed metrics for a Java source file.
 */
public class CodeMetrics {

    private int    lineCount;
    private int    methodCount;
    private int    classCount;
    private int    cyclomaticComplexity;
    private double commentDensity;
    private int    maxNestingDepth;
    private double avgMethodLength;
    private int    todoCount;
    private int    importCount;
    private int    fieldCount;

    // Builder pattern for clean construction
    public static class Builder {
        private final CodeMetrics m = new CodeMetrics();
        public Builder lineCount(int v)             { m.lineCount = v;             return this; }
        public Builder methodCount(int v)           { m.methodCount = v;           return this; }
        public Builder classCount(int v)            { m.classCount = v;            return this; }
        public Builder cyclomaticComplexity(int v)  { m.cyclomaticComplexity = v;  return this; }
        public Builder commentDensity(double v)     { m.commentDensity = v;        return this; }
        public Builder maxNestingDepth(int v)       { m.maxNestingDepth = v;       return this; }
        public Builder avgMethodLength(double v)    { m.avgMethodLength = v;       return this; }
        public Builder todoCount(int v)             { m.todoCount = v;             return this; }
        public Builder importCount(int v)           { m.importCount = v;           return this; }
        public Builder fieldCount(int v)            { m.fieldCount = v;            return this; }
        public CodeMetrics build()                  { return m; }
    }

    public int    getLineCount()            { return lineCount;            }
    public int    getMethodCount()          { return methodCount;          }
    public int    getClassCount()           { return classCount;           }
    public int    getCyclomaticComplexity() { return cyclomaticComplexity; }
    public double getCommentDensity()       { return commentDensity;       }
    public int    getMaxNestingDepth()      { return maxNestingDepth;      }
    public double getAvgMethodLength()      { return avgMethodLength;      }
    public int    getTodoCount()            { return todoCount;            }
    public int    getImportCount()          { return importCount;          }
    public int    getFieldCount()           { return fieldCount;           }
}
