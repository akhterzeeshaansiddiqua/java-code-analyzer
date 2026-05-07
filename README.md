# ☕ Intelligent Java Code Analyzer & Optimizer

> A command-line static analysis tool for Java source files that detects security vulnerabilities, performance anti-patterns, and style violations — then suggests concrete, actionable optimizations.

---

## 📌 Overview

**Java Code Analyzer** performs a full multi-category static analysis pass on any `.java` file and produces:

- 🔒 **Security checks** — SQL injection, hardcoded credentials, unsafe singletons, string `==` comparisons
- ⚡ **Performance checks** — String concat in loops, O(n²) `List.contains()`, missing diamond operator
- 🐛 **Bug detection** — Empty catch blocks, null returns, floating-point `==`, mutable field exposure
- 🎨 **Style checks** — `System.out` usage, TODO comments, magic numbers, deep nesting
- 📊 **Metrics** — Cyclomatic complexity, comment density, nesting depth, method count, and more
- 💡 **Optimization suggestions** — Before/after code snippets with impact ratings
- 📄 **HTML report** — Self-contained report with diff viewer and source code viewer

---

## 🗂️ Project Structure

```
java-code-analyzer/
├── pom.xml
├── Sample.java                        ← Intentionally flawed demo file
├── src/
│   ├── main/java/com/codeanalyzer/
│   │   ├── Main.java                  ← CLI entry point
│   │   ├── AnalysisIssue.java         ← Issue model (severity, message, line)
│   │   ├── CodeMetrics.java           ← Metrics model (builder pattern)
│   │   ├── OptimizationResult.java    ← Optimization model (before/after/impact)
│   │   ├── analyzer/
│   │   │   └── CodeAnalyzer.java      ← 6-category static analysis engine
│   │   ├── metrics/
│   │   │   └── MetricsCalculator.java ← McCabe complexity, comment density, etc.
│   │   ├── optimizer/
│   │   │   └── CodeOptimizer.java     ← 10 optimization rules + auto-fix
│   │   └── report/
│   │       └── ReportGenerator.java   ← Self-contained HTML report generator
│   └── test/java/com/codeanalyzer/
│       └── CodeAnalyzerTest.java      ← 15 JUnit 5 unit tests
```

---

## ⚙️ Build & Run

### Prerequisites
- Java 17+
- Maven 3.8+

### Build

```bash
git clone https://github.com/YOUR_USERNAME/java-code-analyzer.git
cd java-code-analyzer
mvn clean package -q
```

This produces `target/code-analyzer.jar`.

### Run

```bash
# Basic analysis
java -jar target/code-analyzer.jar Sample.java

# With verbose metrics
java -jar target/code-analyzer.jar Sample.java --verbose

# Generate HTML report
java -jar target/code-analyzer.jar Sample.java --report

# Apply auto-fixable optimizations
java -jar target/code-analyzer.jar Sample.java --fix

# All options together
java -jar target/code-analyzer.jar Sample.java --report --fix --verbose
```

### Run Tests

```bash
mvn test
```

---

## 🖥️ Sample Output

```
============================================================
  Intelligent Java Code Analyzer & Optimizer
  File: Sample.java
============================================================

--- Code Metrics ---
  Lines of code      : 68
  Methods            : 5
  Classes            : 1
  Cyclomatic complex.: 12
  Comment density    : 8.8%

--- Issues (9) ---
  [ERROR]  Line 25:  SQL Injection: query built with string concatenation
             → Use PreparedStatement with '?' placeholders instead
  [ERROR]  Line 38:  String compared with == (reference equality, not value equality)
             → Use .equals() or Objects.equals() for string comparison
  [ERROR]  Line 38:  Hardcoded credential detected (password/secret in source code)
             → Move credentials to environment variables or a secrets manager
  [WARN]   Line 32:  Overly broad catch clause: catching Exception/Throwable
             → Catch the specific exception type(s) you expect
  [WARN]   Line 33:  e.printStackTrace() outputs to stderr only
             → Use a logger: logger.error("message", e);
  ...

--- Optimizations (7) ---
  [HIGH]   Replace String + in loop with StringBuilder
           Eliminates O(n²) String allocation; significantly improves performance
  [HIGH]   Use PreparedStatement to prevent SQL Injection
           Parameterized queries eliminate injection risk ...
  ...

============================================================
  Quality Score: 28 / 100   (Critical Issues)
============================================================
```

---

## 📊 Analysis Categories

| Category | Checks |
|----------|--------|
| Security | SQL injection, hardcoded credentials, unsafe singleton, string `==`, hardcoded IPs |
| Performance | String concat in loops, `List.contains()` in loops, raw generics, index vs for-each |
| Bugs | Empty catch, null return in catch, float `==`, mutable field exposure |
| Style | `System.out`, TODO/FIXME, deep nesting, magic numbers |
| Exceptions | Empty catch, broad catch, `printStackTrace()` |
| Best Practices | Missing `@Override`, non-final static fields |

---

## 💡 Optimization Suggestions

Each suggestion includes:
- A human-readable **title** and **reason**
- A **before/after** code snippet pair
- An **impact rating** (`high` / `medium` / `low`)
- A **category** (`performance` / `security` / `style` / `exceptions`)
- Whether it is **auto-fixable** via `--fix`

---

## 🔮 Future Scope

- [ ] AST-based analysis using JavaParser for richer accuracy
- [ ] IDE plugin (IntelliJ / VS Code extension)
- [ ] CI/CD integration (GitHub Actions / GitLab CI)
- [ ] Configuration file (`.analyzer.yml`) for custom rules
- [ ] JSON/XML report output for pipeline integration
- [ ] Custom rule API for project-specific checks

---

## 📄 License

MIT License — see [LICENSE](LICENSE)
