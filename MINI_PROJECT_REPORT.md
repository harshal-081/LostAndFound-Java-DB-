# MINI PROJECT REPORT

## Lost and Found Management System

---

## 1. Introduction

The **Lost and Found Management System** is a DBMS-based project developed to manage missing person and lost/found reports in a structured digital form. Instead of maintaining records manually, this system stores all report details in a MySQL database and allows users to perform the required operations through a frontend interface.

The project provides an easy way to maintain report information such as:

1. Report ID  
2. Name of person  
3. Age  
4. Gender  
5. Address  
6. Contact number  
7. Report date  
8. Last seen location  
9. Description  
10. Status  
11. Reporter name  
12. Relationship with person  

The frontend is built using **HTML, CSS, and JavaScript**. It communicates with the **Java backend**, and the backend connects with the **MySQL database** using **JDBC**. The project also uses **stored procedures, triggers, and functions** so that the database can automatically update status history and audit records whenever changes are made from the frontend.

The main objective of this system is to simplify report handling, reduce manual work, and maintain centralized and organized data.

---

## 2. Scope

The scope of the Lost and Found Management System includes:

1. Managing missing and found person reports digitally.  
2. Adding new reports into the system.  
3. Viewing all reports from the database.  
4. Searching reports by ID, name, or location.  
5. Updating report status as Missing, Found, or Resolved.  
6. Deleting reports when required.  
7. Maintaining centralized report data.  
8. Automatically storing status history and audit records.  

### Future Scope

The system can be improved further by adding:

1. User login and authentication  
2. Photo upload for missing person identification  
3. Email or SMS notification system  
4. Police station or authority access  
5. Advanced filtering and reporting  
6. Cloud-based deployment  

---

## 3. Requirements

### Hardware Requirements

| Component | Requirement |
|---|---|
| Processor | Intel i3 or above |
| RAM | 4 GB or above |
| Storage | 500 MB free space |
| System Type | Desktop/Laptop |

### Software Requirements

| Software | Purpose |
|---|---|
| Windows OS | Platform for development and execution |
| Java JDK | Backend development and JDBC |
| MySQL Server | Database management |
| MySQL Connector/J | Java-MySQL connectivity |
| Web Browser | Running frontend |
| VS Code / IDE | Code editing |

### Functional Requirements

1. User should be able to add a new report.  
2. User should be able to view all reports.  
3. User should be able to search reports.  
4. User should be able to update report status.  
5. User should be able to delete reports.  
6. System should store all data in MySQL database.  
7. System should automatically maintain status history and audit log.  

---

## 4. System Overview

The Lost and Found Management System is developed as a frontend-to-database integrated system. It contains a frontend interface, a Java connectivity layer, and a MySQL database layer.

### Working of the System

1. The user opens the frontend in the browser.  
2. The frontend sends requests to the Java backend.  
3. The backend connects to MySQL using JDBC.  
4. Stored procedures perform insert, update, and delete operations.  
5. Triggers automatically update related tables like status history and audit log.  
6. Updated data is returned to the frontend and displayed in tables.  

### System Architecture Diagram

```text
+----------------------------+
|        User / Browser      |
+-------------+--------------+
              |
              v
+----------------------------+
| HTML / CSS / JavaScript UI |
| Dashboard, Add, View,      |
| Search, Update Status      |
+-------------+--------------+
              |
              v
+----------------------------+
|     Java Backend Server    |
|   API + JDBC Connectivity  |
+-------------+--------------+
              |
              v
+----------------------------+
|        MySQL Database      |
|       lost_found_db        |
+-------------+--------------+
              |
              v
+----------------------------+
| reports                    |
| report_status_history      |
| report_audit_log           |
| Procedures / Function /    |
| Triggers                   |
+----------------------------+
```

### Database Overview

The project mainly uses the following database tables:

#### 1. reports

This is the main table used by the frontend to display and manage report records.

| Column Name | Description |
|---|---|
| report_id | Unique report ID |
| name | Name of person |
| age | Age of person |
| gender | Gender |
| address | Address |
| phone_no | Contact phone number |
| report_date | Date of report |
| location | Last seen location |
| description | Description/details |
| status | Missing / Found / Resolved |
| reporter_name | Name of reporter |
| contact_no | Reporter contact number |
| relationship | Relationship with person |
| status_remarks | Remarks for current status |
| created_at | Record creation time |
| updated_at | Record update time |

#### 2. report_status_history

This table stores every status change automatically.

| Column Name | Description |
|---|---|
| history_id | Unique history ID |
| report_id | Report reference |
| old_status | Previous status |
| new_status | Updated status |
| remarks | Status remarks |
| changed_at | Change timestamp |

#### 3. report_audit_log

This table stores insert, update, status update, and delete actions.

| Column Name | Description |
|---|---|
| audit_id | Unique audit ID |
| report_id | Report reference |
| action_type | Type of action |
| action_message | Action description |
| action_at | Action timestamp |

### Database Table Relationship Diagram

```text
+-----------------------+
|        reports        |
+-----------------------+
| report_id (PK)        |
| name                  |
| age                   |
| gender                |
| address               |
| phone_no              |
| report_date           |
| location              |
| description           |
| status                |
| reporter_name         |
| contact_no            |
| relationship          |
| status_remarks        |
+----------+------------+
           |
           | report_id
           |
  +--------+---------+
  |                  |
  v                  v
+----------------+  +----------------+
|report_status_  |  | report_audit_  |
|history         |  | log            |
+----------------+  +----------------+
| history_id PK  |  | audit_id PK    |
| report_id FK   |  | report_id      |
| old_status     |  | action_type    |
| new_status     |  | action_message |
| remarks        |  | action_at      |
| changed_at     |  +----------------+
+----------------+
```

### Frontend Table Mapping

The frontend shows data in tables using the main `reports` table from MySQL.

| Frontend Table/Page | Database Table | Main Columns Used |
|---|---|---|
| Dashboard Recent Reports | reports | report_id, name, age, gender, location, status, reporter_name |
| View Reports | reports | report_id, name, age, gender, location, report_date, status, reporter_name |
| Search Results | reports | report_id, name, age, gender, location, report_date, status |
| Update Status Page | reports | report_id, status, status_remarks |

---

## 5. Main Code: Database Connectivity and Database Logic

This section contains the main code related to database connectivity and database logic only.

### JDBC Connectivity Code

```java
class DBConnection {
    static Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/lost_found_db?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = "your_password";
        return DriverManager.getConnection(url, user, password);
    }
}
```

This code is used to connect the Java backend with the MySQL database.

### Stored Procedure: Add Report

```sql
CREATE PROCEDURE sp_add_report(
    IN p_name VARCHAR(100),
    IN p_age INT,
    IN p_gender VARCHAR(20),
    IN p_address VARCHAR(255),
    IN p_phone_no BIGINT,
    IN p_report_date DATE,
    IN p_location VARCHAR(150),
    IN p_description VARCHAR(500),
    IN p_reporter_name VARCHAR(100),
    IN p_contact_no VARCHAR(15),
    IN p_relationship VARCHAR(50),
    IN p_status VARCHAR(30),
    OUT p_report_id INT
)
BEGIN
    INSERT INTO reports
    (name, age, gender, address, phone_no, report_date, location,
     description, reporter_name, contact_no, relationship, status,
     status_remarks)
    VALUES
    (p_name, p_age, p_gender, p_address, p_phone_no, p_report_date,
     p_location, p_description, p_reporter_name, p_contact_no,
     p_relationship, p_status, 'Initial report created');

    SET p_report_id = LAST_INSERT_ID();
END;
```

### Stored Procedure: Update Status

```sql
CREATE PROCEDURE sp_update_report_status(
    IN p_report_id INT,
    IN p_status VARCHAR(30),
    IN p_remarks VARCHAR(255)
)
BEGIN
    UPDATE reports
    SET status = p_status,
        status_remarks = NULLIF(TRIM(IFNULL(p_remarks, '')), '')
    WHERE report_id = p_report_id;
END;
```

### Function: Normalize Status

```sql
CREATE FUNCTION fn_normalize_status(input_status VARCHAR(30))
RETURNS VARCHAR(30)
DETERMINISTIC
BEGIN
    DECLARE normalized VARCHAR(30);
    SET normalized = LOWER(TRIM(IFNULL(input_status, 'Missing')));

    IF normalized = 'found' THEN
        RETURN 'Found';
    ELSEIF normalized = 'resolved' THEN
        RETURN 'Resolved';
    ELSE
        RETURN 'Missing';
    END IF;
END;
```

### Trigger: Automatic Status History Update

```sql
CREATE TRIGGER trg_reports_after_update
AFTER UPDATE ON reports
FOR EACH ROW
BEGIN
    IF NEW.status <> OLD.status THEN
        INSERT INTO report_status_history
        (report_id, old_status, new_status, remarks)
        VALUES
        (NEW.report_id, OLD.status, NEW.status, NEW.status_remarks);

        INSERT INTO report_audit_log
        (report_id, action_type, action_message)
        VALUES
        (NEW.report_id, 'STATUS_UPDATE',
         CONCAT('Status changed from ', OLD.status, ' to ', NEW.status));
    END IF;
END;
```

### Java Code for Calling Stored Procedure

```java
CallableStatement cs =
    con.prepareCall("{CALL sp_update_report_status(?, ?, ?)}");
cs.setInt(1, reportId);
cs.setString(2, newStatus);
cs.setString(3, remarks);
cs.execute();
```

This code shows how the backend calls the database procedure when the user updates the report status from the frontend.

---

## 6. Screenshots of Project in Execution

Add screenshots in this section while preparing the final Word or PDF report.

1. **Dashboard showing report summary and recent reports**  
2. **Add New Report form**  
3. **View Reports table**  
4. **Search Reports page**  
5. **Update Status page**  
6. **MySQL reports table after execution**  
7. **MySQL report_status_history table showing automatic updates**  
8. **MySQL report_audit_log table showing audit entries**  

### Suggested Screenshot Captions

- Dashboard showing recent reports  
- Add report form interface  
- All reports fetched from MySQL database  
- Search result table  
- Update status operation  
- Reports table in MySQL  
- Status history table updated by trigger  
- Audit log table updated automatically  

---

## 7. Conclusion

The Lost and Found Management System successfully provides a simple and effective way to manage report information digitally. The system reduces manual work, improves data organization, and makes report handling easier.

This project helped in understanding:

1. Database connectivity using JDBC  
2. CRUD operations through frontend and backend  
3. Frontend and backend integration  
4. Use of stored procedures  
5. Use of database functions  
6. Use of triggers for automatic updates  
7. Real-time data management using MySQL  

The project can be further improved by adding authentication, notifications, image upload, and advanced reporting features.

