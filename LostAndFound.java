import java.util.*;
import java.io.*;
import java.sql.*;

interface ReportOperations {
    void addReportToDB(PersonReport report);
    void viewAllReportsFromDB();
    void searchByIdFromDB(int id);
    void updateStatusInDB(int id, String newStatus);
    void deleteReportFromDB(int id);
    void saveToFile();
    void readFromFile();
}

class InvalidAgeException extends Exception {
    InvalidAgeException(String message) {
        super(message);
    }
}

class DBConnection {
    static Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/lost_found_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        String user = "root";
        String password = "Harshal@456";  

        return DriverManager.getConnection(url, user, password);
    }
}

class Person {
    protected String name;
    protected int age;
    protected String gender;
    protected String address;
    protected long phoneNo;

    Person(String name, int age, String gender, String address, long phoneNo) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.address = address;
        this.phoneNo = phoneNo;
    }
}

class PersonReport extends Person {
    protected int reportId;
    protected String reportDate;
    protected String location;
    protected String description;
    protected String status;

    static String organization = "Lost & Found";

    PersonReport(int reportId, String name, int age, String gender,
                 String address, long phoneNo, String reportDate,
                 String location, String description, String status) {
        super(name, age, gender, address, phoneNo);
        this.reportId = reportId;
        this.reportDate = reportDate;
        this.location = location;
        this.description = description;
        this.status = status;
    }

    void display() {
        System.out.println("======================================");
        System.out.println("System       : " + organization);
        System.out.println("Report ID    : " + reportId);
        System.out.println("Name         : " + name);
        System.out.println("Age          : " + age);
        System.out.println("Gender       : " + gender);
        System.out.println("Address      : " + address);
        System.out.println("Phone No     : " + phoneNo);
        System.out.println("Report Date  : " + reportDate);
        System.out.println("Location     : " + location);
        System.out.println("Description  : " + description);
        System.out.println("Status       : " + status);
        System.out.println("======================================");
    }

    String toFileString() {
        return reportId + "," + name + "," + age + "," + gender + "," +
               address + "," + phoneNo + "," + reportDate + "," +
               location + "," + description + "," + status;
    }
}

class MissingReport extends PersonReport {
    MissingReport(int reportId, String name, int age, String gender,
                  String address, long phoneNo, String reportDate,
                  String location, String description) {
        super(reportId, name, age, gender, address, phoneNo, reportDate, location, description, "Missing");
    }
}

class ReportManager implements ReportOperations {

    public void addReportToDB(PersonReport report) {
        String query = "{CALL sp_add_report(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";

        try (Connection con = DBConnection.getConnection();
             CallableStatement pst = con.prepareCall(query)) {

            pst.setString(1, report.name);
            pst.setInt(2, report.age);
            pst.setString(3, report.gender);
            pst.setString(4, report.address);
            pst.setLong(5, report.phoneNo);
            pst.setString(6, report.reportDate);
            pst.setString(7, report.location);
            pst.setString(8, report.description);
            pst.setString(9, "Console User");
            pst.setString(10, String.valueOf(report.phoneNo));
            pst.setString(11, "Other");
            pst.setString(12, report.status);
            pst.registerOutParameter(13, Types.INTEGER);

            pst.execute();
            System.out.println("Report saved in database successfully. New Report ID: " + pst.getInt(13));

        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Report ID already exists. Use a unique Report ID.");
        } catch (Exception e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    public void viewAllReportsFromDB() {
        String query = "SELECT * FROM reports";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {

            boolean found = false;

            while (rs.next()) {
                found = true;
                System.out.println("======================================");
                System.out.println("System       : Lost & Found");
                System.out.println("Report ID    : " + rs.getInt("report_id"));
                System.out.println("Name         : " + rs.getString("name"));
                System.out.println("Age          : " + rs.getInt("age"));
                System.out.println("Gender       : " + rs.getString("gender"));
                System.out.println("Address      : " + rs.getString("address"));
                System.out.println("Phone No     : " + rs.getLong("phone_no"));
                System.out.println("Report Date  : " + rs.getString("report_date"));
                System.out.println("Location     : " + rs.getString("location"));
                System.out.println("Description  : " + rs.getString("description"));
                System.out.println("Status       : " + rs.getString("status"));
                System.out.println("======================================");
            }

            if (!found) {
                System.out.println("No reports available.");
            }

        } catch (Exception e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    public void searchByIdFromDB(int id) {
        String query = "SELECT * FROM reports WHERE report_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(query)) {

            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                System.out.println("======================================");
                System.out.println("System       : Lost & Found");
                System.out.println("Report ID    : " + rs.getInt("report_id"));
                System.out.println("Name         : " + rs.getString("name"));
                System.out.println("Age          : " + rs.getInt("age"));
                System.out.println("Gender       : " + rs.getString("gender"));
                System.out.println("Address      : " + rs.getString("address"));
                System.out.println("Phone No     : " + rs.getLong("phone_no"));
                System.out.println("Report Date  : " + rs.getString("report_date"));
                System.out.println("Location     : " + rs.getString("location"));
                System.out.println("Description  : " + rs.getString("description"));
                System.out.println("Status       : " + rs.getString("status"));
                System.out.println("======================================");
            } else {
                System.out.println("Report with ID " + id + " not found.");
            }

        } catch (Exception e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    public void updateStatusInDB(int id, String newStatus) {
        String query = "{CALL sp_update_report_status(?, ?, ?)}";

        try (Connection con = DBConnection.getConnection();
             CallableStatement pst = con.prepareCall(query)) {

            pst.setInt(1, id);
            pst.setString(2, newStatus);
            pst.setString(3, "Updated from Java console");

            pst.execute();
            System.out.println("Status update request completed.");

        } catch (Exception e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    public void deleteReportFromDB(int id) {
        String query = "{CALL sp_delete_report(?)}";

        try (Connection con = DBConnection.getConnection();
             CallableStatement pst = con.prepareCall(query)) {

            pst.setInt(1, id);
            pst.execute();
            System.out.println("Delete request completed.");

        } catch (Exception e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    public void saveToFile() {
        String query = "SELECT * FROM reports";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(query);
             ResultSet rs = pst.executeQuery();
             FileWriter fw = new FileWriter("reports.txt")) {

            while (rs.next()) {
                String line = rs.getInt("report_id") + "," +
                              rs.getString("name") + "," +
                              rs.getInt("age") + "," +
                              rs.getString("gender") + "," +
                              rs.getString("address") + "," +
                              rs.getLong("phone_no") + "," +
                              rs.getString("report_date") + "," +
                              rs.getString("location") + "," +
                              rs.getString("description") + "," +
                              rs.getString("status");
                fw.write(line + "\n");
            }

            System.out.println("Reports saved to file successfully.");

        } catch (Exception e) {
            System.out.println("File writing error: " + e.getMessage());
        }
    }

    public void readFromFile() {
        try (FileReader fr = new FileReader("reports.txt");
             BufferedReader br = new BufferedReader(fr)) {

            String line;
            System.out.println("\n===== Reports Stored in File =====");

            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");

                if (data.length == 10) {
                    System.out.println("======================================");
                    System.out.println("Report ID    : " + data[0]);
                    System.out.println("Name         : " + data[1]);
                    System.out.println("Age          : " + data[2]);
                    System.out.println("Gender       : " + data[3]);
                    System.out.println("Address      : " + data[4]);
                    System.out.println("Phone No     : " + data[5]);
                    System.out.println("Report Date  : " + data[6]);
                    System.out.println("Location     : " + data[7]);
                    System.out.println("Description  : " + data[8]);
                    System.out.println("Status       : " + data[9]);
                    System.out.println("======================================");
                }
            }

        } catch (Exception e) {
            System.out.println("File reading error: " + e.getMessage());
        }
    }
}

public class LostAndFound {

    static void validateAge(int age) throws InvalidAgeException {
        if (age <= 0 || age > 120) {
            throw new InvalidAgeException("Invalid age entered. Age must be between 1 and 120.");
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        ReportManager manager = new ReportManager();
        int choice = -1;

        do {
            try {
                System.out.println("\n========== Lost & Found System ==========");
                System.out.println("1. Add Missing Report");
                System.out.println("2. View All Reports");
                System.out.println("3. Search Report by ID");
                System.out.println("4. Update Report Status");
                System.out.println("5. Delete Report");
                System.out.println("6. Save Reports to File");
                System.out.println("7. Read Reports from File");
                System.out.println("0. Exit");
                System.out.print("Enter your choice: ");
                choice = sc.nextInt();
                sc.nextLine();

                switch (choice) {
                    case 1: {
                        System.out.print("Enter Report ID: ");
                        int id = sc.nextInt();
                        sc.nextLine();

                        System.out.print("Enter Name: ");
                        String name = sc.nextLine();

                        System.out.print("Enter Age: ");
                        int age = sc.nextInt();
                        sc.nextLine();
                        validateAge(age);

                        System.out.print("Enter Gender: ");
                        String gender = sc.nextLine();

                        System.out.print("Enter Address: ");
                        String address = sc.nextLine();

                        System.out.print("Enter Phone Number: ");
                        long phone = sc.nextLong();
                        sc.nextLine();

                        System.out.print("Enter Missing Date: ");
                        String date = sc.nextLine();

                        System.out.print("Enter Missing Location: ");
                        String location = sc.nextLine();

                        System.out.print("Enter Description: ");
                        String description = sc.nextLine();

                        MissingReport mr = new MissingReport(id, name, age, gender, address, phone, date, location, description);
                        manager.addReportToDB(mr);
                        break;
                    }

                    case 2:
                        manager.viewAllReportsFromDB();
                        break;

                    case 3: {
                        System.out.print("Enter Report ID to search: ");
                        int searchId = sc.nextInt();
                        sc.nextLine();
                        manager.searchByIdFromDB(searchId);
                        break;
                    }

                    case 4: {
                        System.out.print("Enter Report ID to update: ");
                        int updateId = sc.nextInt();
                        sc.nextLine();

                        System.out.println("Choose new status:");
                        System.out.println("1. Found");
                        System.out.println("2. Resolved");
                        System.out.print("Enter choice: ");
                        int statusChoice = sc.nextInt();
                        sc.nextLine();

                        if (statusChoice == 1) {
                            manager.updateStatusInDB(updateId, "Found");
                        } else if (statusChoice == 2) {
                            manager.updateStatusInDB(updateId, "Resolved");
                        } else {
                            System.out.println("Invalid status choice.");
                        }
                        break;
                    }

                    case 5: {
                        System.out.print("Enter Report ID to delete: ");
                        int deleteId = sc.nextInt();
                        sc.nextLine();
                        manager.deleteReportFromDB(deleteId);
                        break;
                    }

                    case 6:
                        manager.saveToFile();
                        break;

                    case 7:
                        manager.readFromFile();
                        break;

                    case 0:
                        System.out.println("Exiting program...");
                        break;

                    default:
                        System.out.println("Invalid choice. Please enter a valid option.");
                }

            } catch (InvalidAgeException e) {
                System.out.println("Custom Exception: " + e.getMessage());
            } catch (InputMismatchException e) {
                System.out.println("Exception: Invalid input type entered.");
                sc.nextLine();
                choice = -1;
            } catch (Exception e) {
                System.out.println("Exception: " + e.getMessage());
                choice = -1;
            }

        } while (choice != 0);

        sc.close();
    }
}
