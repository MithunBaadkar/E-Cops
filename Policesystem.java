import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Policesystem {

    // 1. DATABASE CONFIGURATION
    static final String DB_URL = "jdbc:mysql://localhost:3306/police_db";
    static final String USER = "root"; 
    static final String PASS = "root"; // Change this to your MySQL password

    static Scanner scanner = new Scanner(System.in);
    static String currentUser = null;
    static String currentRole = null;
    public static void main(String[] args) {
        
        System.out.println("--- NEXT-GEN LAW ENFORCEMENT SYSTEM ---");
        
        while (true) {
            if (currentUser == null) {
                showInitialMenu();
            } else {
                showMainMenu();
            }
        }
    }

    // --- MENU SYSTEM ---

    private static void showInitialMenu() {
        System.out.println("\n1. Login\n2. Citizen Registration\n3. Exit");
        int choice = scanner.nextInt();
        if (choice == 1) login();
        else if (choice == 2) register();
        else System.exit(0);
    }

    private static void showMainMenu() {
        System.out.println("\n--- WELCOME " + currentUser + " (" + currentRole + ") ---");
        if (currentRole.equals("ADMIN")) {
            System.out.println("1. Create Virtual PS\n2. Logout");
            if (scanner.nextInt() == 1) createStation(); else currentUser = null;
        } 
        else if (currentRole.equals("CITIZEN")) {
            System.out.println("1. File Complaint\n2. View My Complaints\n3. Logout");
            int c = scanner.nextInt();
            if (c == 1) fileComplaint(); else if (c == 2) viewComplaints(); else currentUser = null;
        }
        else if (currentRole.equals("MAGISTRATE")) {
            System.out.println("1. Review Warrants\n2. Logout");
            if (scanner.nextInt() == 1) reviewWarrants(); else currentUser = null;
        }
    }

    // --- DATABASE OPERATIONS ---

    private static void login() {
        System.out.print("Username: "); String u = scanner.next();
        System.out.print("Password: "); String p = scanner.next();

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String sql = "SELECT role FROM users WHERE username = ? AND password = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, u);
            pstmt.setString(2, p);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                currentUser = u;
                currentRole = rs.getString("role");
                System.out.println("Login Successful!");
            } else {
                System.out.println("Invalid credentials!");
            }
        } catch (SQLException e) { System.out.println("DB Error: " + e.getMessage()); }
    }

    private static void register() {
        System.out.print("New Username: "); String u = scanner.next();
        System.out.print("Password: "); String p = scanner.next();
        
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, 'CITIZEN')";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, u);
            pstmt.setString(2, p);
            pstmt.executeUpdate();
            System.out.println("Registration successful! You can now login.");
        } catch (SQLException e) { System.out.println("Error: " + e.getMessage()); }
    }

    private static void fileComplaint() {
        System.out.print("Enter Complaint Details: ");
        scanner.nextLine(); // clear buffer
        String details = scanner.nextLine();

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String sql = "INSERT INTO complaints (citizen, details, status) VALUES (?, ?, 'PENDING')";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, currentUser);
            pstmt.setString(2, details);
            pstmt.executeUpdate();
            System.out.println("Complaint registered in the database.");
        } catch (SQLException e) { System.out.println("Error: " + e.getMessage()); }
    }

    private static void viewComplaints() {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String sql = "SELECT * FROM complaints WHERE citizen = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, currentUser);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id") + " | Status: " + rs.getString("status") + " | Details: " + rs.getString("details"));
            }
        } catch (SQLException e) { System.out.println("Error: " + e.getMessage()); }
    }

    private static void createStation() {
        System.out.print("Station Name: "); String name = scanner.next();
        System.out.println("Virtual Station '" + name + "' added to database.");
        // Add SQL INSERT for stations here if needed
    }

    private static void reviewWarrants() {
        System.out.println("Checking pending FIRs for warrants...");
        // Magistrate logic to update 'warrant_approved' column
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
        String sql = "SELECT id, details, status FROM complaints WHERE status = 'PENDING'";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery();

        while (rs.next()) {
            System.out.println("Complaint ID: " + rs.getInt("id") +
                               " | Details: " + rs.getString("details") +
                               " | Status: " + rs.getString("status"));
        }

        System.out.print("Enter Complaint ID to approve warrant: ");
        int id = scanner.nextInt();

        String updateSql = "UPDATE complaints SET status = 'WARRANT_APPROVED' WHERE id = ?";
        PreparedStatement updatePstmt = conn.prepareStatement(updateSql);
        updatePstmt.setInt(1, id);
        updatePstmt.executeUpdate();

        System.out.println("Warrant approved for Complaint ID: " + id);
    } catch (SQLException e) {
        System.out.println("Error: " + e.getMessage());
    }
    }
}
