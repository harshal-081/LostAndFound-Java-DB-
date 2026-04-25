# 🔍 Lost & Found Management System

## 📌 Project Description

The **Lost & Found Management System** is a Java-based application designed to help manage and track missing persons in an organized and efficient way.
The system allows users to report missing individuals and record found persons, storing all information securely in a MySQL database.

This project focuses on solving real-world problems faced during **large public gatherings**, where managing lost individuals becomes difficult.

---

## 🌍 Applications / Use Cases

* Large gatherings like Kumbh Mela 🕉️
* Festivals and public events 🎉
* Railway stations and bus stands 🚉
* Disaster management situations 🚨
* Police and administrative tracking systems 👮

---

## ⚙️ Features

* Add missing person reports
* Add found person reports
* Store and manage records in database
* Retrieve and display reports
* Simple frontend interface for interaction

---

## 🛠️ Technologies Used

* **Java** – Backend logic
* **MySQL** – Database management
* **HTML/CSS** – Frontend interface

---

## 🗄️ Database

* MySQL database used for storing reports
* Includes:

  * `database_setup.sql`
  * `database_queries_for_print.sql`

---

## ▶️ How to Run

1. Install MySQL and create database using `database_setup.sql`
2. Update database credentials in Java files
3. Compile Java files:

   ```bash
   javac *.java
   ```
4. Run the backend:

   ```bash
   java LostFoundWebServer
   ```

   OR use:

   ```powershell
   ./run-backend.ps1
   ```
5. Open `lost_found_frontend.html` in your browser

---

## 🔮 Future Scope

* Face recognition using AI/ML 🤖
* Mobile application integration 📱
* Real-time location tracking 📍
* Cloud database integration ☁️
* Integration with government systems

---

## 📁 Project Structure

* Java backend files
* SQL database scripts
* Frontend HTML file
* Project report

---

## 👨‍💻 Author

**Harshal Pingale**
Second Year B.Tech IT Student

---

## 📢 Note

Currently, the system is designed to manage **missing and found persons**.
It can be extended in the future to include lost items and advanced tracking features.
