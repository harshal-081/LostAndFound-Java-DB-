CREATE DATABASE IF NOT EXISTS lost_found_db;

USE lost_found_db;

DROP TRIGGER IF EXISTS trg_reports_before_insert;
DROP TRIGGER IF EXISTS trg_reports_before_update;
DROP TRIGGER IF EXISTS trg_reports_after_insert;
DROP TRIGGER IF EXISTS trg_reports_after_update;
DROP TRIGGER IF EXISTS trg_reports_after_delete;
DROP PROCEDURE IF EXISTS sp_add_report;
DROP PROCEDURE IF EXISTS sp_update_report_status;
DROP PROCEDURE IF EXISTS sp_delete_report;
DROP FUNCTION IF EXISTS fn_normalize_status;

DROP TABLE IF EXISTS report_audit_log;
DROP TABLE IF EXISTS report_status_history;
DROP TABLE IF EXISTS reports;

CREATE TABLE reports (
    report_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    age INT NOT NULL CHECK (age BETWEEN 1 AND 120),
    gender VARCHAR(20) NOT NULL,
    address VARCHAR(255) NOT NULL,
    phone_no BIGINT NOT NULL,
    report_date DATE NOT NULL,
    location VARCHAR(150) NOT NULL,
    description VARCHAR(500) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'Missing',
    reporter_name VARCHAR(100) NOT NULL,
    contact_no VARCHAR(15) NOT NULL,
    relationship VARCHAR(50),
    status_remarks VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

ALTER TABLE reports AUTO_INCREMENT = 101;

CREATE TABLE report_status_history (
    history_id INT PRIMARY KEY AUTO_INCREMENT,
    report_id INT NOT NULL,
    old_status VARCHAR(30),
    new_status VARCHAR(30) NOT NULL,
    remarks VARCHAR(255),
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_status_history_report
        FOREIGN KEY (report_id) REFERENCES reports(report_id)
        ON DELETE CASCADE
);

CREATE TABLE report_audit_log (
    audit_id INT PRIMARY KEY AUTO_INCREMENT,
    report_id INT,
    action_type VARCHAR(20) NOT NULL,
    action_message VARCHAR(255) NOT NULL,
    action_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

DELIMITER $$

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
END$$

CREATE TRIGGER trg_reports_before_insert
BEFORE INSERT ON reports
FOR EACH ROW
BEGIN
    SET NEW.status = fn_normalize_status(NEW.status);
    SET NEW.contact_no = TRIM(NEW.contact_no);
    IF NEW.status_remarks IS NULL THEN
        SET NEW.status_remarks = 'Initial report created';
    END IF;
END$$

CREATE TRIGGER trg_reports_before_update
BEFORE UPDATE ON reports
FOR EACH ROW
BEGIN
    SET NEW.status = fn_normalize_status(NEW.status);
    IF NEW.status <> OLD.status AND (NEW.status_remarks IS NULL OR TRIM(NEW.status_remarks) = '') THEN
        SET NEW.status_remarks = CONCAT('Status changed from ', OLD.status, ' to ', NEW.status);
    END IF;
END$$

CREATE TRIGGER trg_reports_after_insert
AFTER INSERT ON reports
FOR EACH ROW
BEGIN
    INSERT INTO report_status_history (report_id, old_status, new_status, remarks)
    VALUES (NEW.report_id, NULL, NEW.status, NEW.status_remarks);

    INSERT INTO report_audit_log (report_id, action_type, action_message)
    VALUES (NEW.report_id, 'INSERT', CONCAT('Report created with status ', NEW.status));
END$$

CREATE TRIGGER trg_reports_after_update
AFTER UPDATE ON reports
FOR EACH ROW
BEGIN
    IF NEW.status <> OLD.status THEN
        INSERT INTO report_status_history (report_id, old_status, new_status, remarks)
        VALUES (NEW.report_id, OLD.status, NEW.status, NEW.status_remarks);

        INSERT INTO report_audit_log (report_id, action_type, action_message)
        VALUES (NEW.report_id, 'STATUS_UPDATE', CONCAT('Status changed from ', OLD.status, ' to ', NEW.status));
    ELSE
        INSERT INTO report_audit_log (report_id, action_type, action_message)
        VALUES (NEW.report_id, 'UPDATE', 'Report details updated');
    END IF;
END$$

CREATE TRIGGER trg_reports_after_delete
AFTER DELETE ON reports
FOR EACH ROW
BEGIN
    INSERT INTO report_audit_log (report_id, action_type, action_message)
    VALUES (OLD.report_id, 'DELETE', CONCAT('Report deleted for ', OLD.name));
END$$

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
        (name, age, gender, address, phone_no, report_date, location, description,
         reporter_name, contact_no, relationship, status, status_remarks)
    VALUES
        (p_name, p_age, p_gender, p_address, p_phone_no, p_report_date, p_location, p_description,
         p_reporter_name, p_contact_no, p_relationship, p_status, 'Initial report created');

    SET p_report_id = LAST_INSERT_ID();
END$$

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
END$$

CREATE PROCEDURE sp_delete_report(IN p_report_id INT)
BEGIN
    DELETE FROM reports
    WHERE report_id = p_report_id;
END$$

DELIMITER ;

CALL sp_add_report('Aarav Sharma', 16, 'Male', 'Andheri West, Mumbai', 9876543210,
    '2026-04-10', 'Andheri Railway Station',
    'Wearing blue jeans and white shirt. Carrying a black school bag.',
    'Neha Sharma', '9876543210', 'Parent', 'Missing', @new_id);

CALL sp_add_report('Priya Patil', 22, 'Female', 'Kothrud, Pune', 9123456780,
    '2026-04-12', 'Pune Bus Stand',
    'Last seen near platform number 3. Wearing a red kurti.',
    'Rohan Patil', '9123456780', 'Sibling', 'Found', @new_id);

CALL sp_add_report('Rahul Verma', 35, 'Male', 'Civil Lines, Nagpur', 9988776655,
    '2026-04-14', 'Sitabuldi Market',
    'Medium height, wearing spectacles and a grey jacket.',
    'Kavita Verma', '9988776655', 'Spouse', 'Missing', @new_id);

CALL sp_add_report('Sneha Kulkarni', 28, 'Female', 'Nashik Road, Nashik', 9090909090,
    '2026-04-15', 'Nashik Railway Station',
    'Carrying a brown handbag and wearing a green saree.',
    'Amit Kulkarni', '9090909090', 'Spouse', 'Resolved', @new_id);

CALL sp_add_report('Mohit Jain', 10, 'Male', 'Dharampeth, Nagpur', 8765432109,
    '2026-04-16', 'School Gate',
    'Wearing school uniform and blue shoes.',
    'Pooja Jain', '8765432109', 'Parent', 'Missing', @new_id);

CALL sp_add_report('Ananya Deshmukh', 19, 'Female', 'Viman Nagar, Pune', 9654321870,
    '2026-04-18', 'Phoenix Mall',
    'Wearing black top and denim jacket. Has a small wrist tattoo.',
    'Madhav Deshmukh', '9654321870', 'Relative', 'Found', @new_id);

SELECT * FROM reports;
SELECT * FROM report_status_history;
SELECT * FROM report_audit_log;
