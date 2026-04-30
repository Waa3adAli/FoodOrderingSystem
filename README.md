# 🍔 Food Ordering System (Java)

A university project developed using Java that simulates a real-world online food ordering system with multiple user roles and database integration.

---

## 📋 Project Overview
The Online Food Ordering System allows customers to browse restaurants, select dishes, add items to a cart, place orders, and track order status.  
The system also supports restaurant owners in managing menus and orders, while administrators oversee users, restaurants, and system activities.

---

## 👥 User Roles
- Customer: Register, login, browse restaurants, place orders, make payments, track orders, and leave reviews.
- Restaurant: Manage dishes and view customer orders.
- Admin: Manage users, restaurants, and track all system orders.

---

## ⭐ Features
- User authentication (Login, Register, Forgot Password)
- Role-based dashboards (Customer, Restaurant, Admin)
- Restaurant and dish listing
- Shopping cart functionality
- Order placement and order tracking
- Payment simulation
- Customer reviews and ratings
- Admin management system

---

## 🛠 Technologies Used
- Java (Swing)
- MySQL
- JDBC
- NetBeans IDE

---

## 🗂 Project Structure

FoodOrderingSystem/
├── src/
│   └── foodorderingsystem/
│       ├── LoginForm.java
│       ├── RegisterForm.java
│       ├── AdminDashboard.java
│       ├── CustomerDashboard.java
│       ├── RestaurantDashboard.java
│       ├── CartForm.java
│       ├── PaymentForm.java
│       ├── OrderTrackingForm.java
│       └── DatabaseConnection.java
├── Food-Ordering-System-Report.pdf
└── README.md

## 🗄 Database
The system uses a MySQL database named FoodOrderingSystem with tables for:
- Users
- Restaurants
- Dishes
- Cart
- Orders
- Payments
- Reviews
- Notifications

---

## ⚠️ Exception Handling
- Handles database connection errors using try-catch blocks
- Validates user inputs (empty fields, email format, password strength)
- Displays user-friendly error messages using dialog boxes

---

## ✅ Testing and Validation
- Manual UI testing of all forms and system features
- Database validation to ensure correct data insertion and updates
- Error simulation for invalid login and registration attempts
- Automatic order tracking refresh using timers

---

## 🎯 Purpose
This project was developed as part of a university coursework to demonstrate:
- Object-Oriented Programming (OOP)
- GUI development using Java Swing
- Database connectivity using JDBC
- System design with multiple user roles

---

## 📄 Project Report
A detailed project report is available in PDF format: [📄 Food-Ordering-System-Report.pdf](Food-Ordering-System-Report.pdf)

---

## 👨‍💻 Team Members
- Lamis Alorini
- Waad Ali
- Shwaa Alrashidi
- Amsha Al-rashidi
- Joud Al-hoshani
- Nada Al-khiari
