import java.sql.*;
import java.util.*;

/**
 * Sample.java — a deliberately flawed Java file used to demonstrate
 * the Intelligent Code Analyzer & Optimizer.
 *
 * Run: java -jar code-analyzer.jar Sample.java --report --verbose
 */
public class Sample {

    // Non-thread-safe singleton
    private static Sample instance;
    private ArrayList<String> users = new ArrayList<String>();
    private Connection conn;

    public static Sample getInstance() {
        if (instance == null) {
            instance = new Sample();
        }
        return instance;
    }

    // SQL Injection vulnerability
    public String getUserById(String userId) {
        String query = "SELECT * FROM users WHERE id = " + userId;
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            String result = "";
            while (rs.next()) {
                result = result + rs.getString("name") + ","; // String concat in loop
            }
            return result;
        } catch (Exception e) {    // Overly broad catch
            e.printStackTrace();   // Not production-safe
            return null;           // Null return in catch
        }
    }

    // String comparison with == (bug)
    public boolean validateUser(String username, String password) {
        if (username == "admin" && password == "admin123") {  // == on strings!
            return true;
        }
        return false;
    }

    // O(n²) removeDuplicates
    public void removeDuplicates() {
        ArrayList<String> unique = new ArrayList<String>();
        for (String s : users) {
            if (!unique.contains(s)) {  // O(n) contains in a loop
                unique.add(s);
            }
        }
        users = unique;
    }

    // Deep nesting + index loop
    public void processUsers(List<String> ids) {
        for (int i = 0; i < ids.size(); i++) {
            String data = getUserById(ids.get(i));
            if (data != null) {
                if (data.length() > 0) {   // Use isBlank()
                    System.out.println("Processing: " + data);  // System.out
                    // TODO: add real processing logic
                }
            }
        }
    }

    // Hardcoded credentials
    private String dbPassword = "SuperSecret123";
    private String dbHost     = "192.168.1.100";  // Hardcoded IP

    public static void main(String[] args) {
        Sample s = Sample.getInstance();
        s.users.add("Alice");
        s.users.add("Bob");
        s.users.add("Alice");
        s.removeDuplicates();
        System.out.println(s.users);
    }
}
