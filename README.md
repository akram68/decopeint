# Advertising Company Management System

A JavaFX-based desktop application for managing advertising company operations, including clients, services, suppliers, and payments.

## Features

### Current Implementation
- **Login System**: Secure login page with credential validation
- **Dashboard**: Overview with key statistics and performance metrics
- **Navigation Menu**: Easy access to different modules (Clients, Services, Suppliers, Payments)
- **Static Demo Data**: Sample data for demonstration purposes

### Future Features (Ready for Implementation)
- Client management (CRUD operations)
- Service catalog management
- Supplier management
- Payment tracking and reporting
- Database integration with MySQL/WAMP

## Technology Stack

- **JavaFX 21**: Modern UI framework
- **Maven**: Dependency management and build tool
- **FXML**: Declarative UI layout
- **CSS**: Custom styling
- **MySQL Connector**: Ready for database integration (included in dependencies)

## Prerequisites

Before running this application, ensure you have:

1. **Java JDK 17 or higher** installed
   - Download from: https://adoptium.net/
   - Verify installation: `java -version`

2. **Maven** installed
   - Download from: https://maven.apache.org/download.cgi
   - Verify installation: `mvn -version`

3. **JavaFX SDK** (handled automatically by Maven)

## Project Structure

```
advertising-management/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── advertising/
│   │   │           ├── MainApp.java
│   │   │           └── controller/
│   │   │               ├── LoginController.java
│   │   │               └── DashboardController.java
│   │   └── resources/
│   │       ├── fxml/
│   │       │   ├── Login.fxml
│   │       │   └── Dashboard.fxml
│   │       └── css/
│   │           └── style.css
├── pom.xml
└── README.md
```

## How to Run

### Option 1: Using Maven (Recommended)

1. **Navigate to the project directory**:
   ```bash
   cd advertising-management
   ```

2. **Clean and compile the project**:
   ```bash
   mvn clean compile
   ```

3. **Run the application**:
   ```bash
   mvn javafx:run
   ```

### Option 2: Using IDE (IntelliJ IDEA / Eclipse)

1. **Import the project**:
   - Open your IDE
   - Select "Import Maven Project"
   - Choose the `pom.xml` file

2. **Run the MainApp class**:
   - Navigate to `src/main/java/com/advertising/MainApp.java`
   - Right-click and select "Run MainApp"

## Login Credentials

Use these credentials to log in:

- **Username**: `admin`
- **Password**: `admin123`

## Application Usage

### Login Page
1. Enter username and password
2. Click "Login" button
3. On successful authentication, you'll be redirected to the dashboard

### Dashboard
- View key statistics (Services, Sales, Active Clients)
- Monitor performance metrics with progress bars
- Navigate to different modules using the sidebar menu
- Logout using the button in the top-right corner

### Navigation Menu
The sidebar provides access to:
- **Dashboard**: Overview and statistics
- **Clients**: Client management (to be implemented)
- **Services**: Service catalog (to be implemented)
- **Suppliers**: Supplier management (to be implemented)
- **Payments**: Payment tracking (to be implemented)

## Database Integration Guide

The application is ready for MySQL/WAMP database integration. Follow these steps:

### 1. Set Up MySQL Database

Create the database and tables:

```sql
CREATE DATABASE advertising_db;
USE advertising_db;

-- Users table for authentication
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Clients table
CREATE TABLE clients (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    company VARCHAR(100),
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Services table
CREATE TABLE services (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2),
    status VARCHAR(20) DEFAULT 'available',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Suppliers table
CREATE TABLE suppliers (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Payments table
CREATE TABLE payments (
    id INT PRIMARY KEY AUTO_INCREMENT,
    client_id INT,
    amount DECIMAL(10, 2),
    status VARCHAR(20) DEFAULT 'pending',
    payment_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (client_id) REFERENCES clients(id)
);

-- Insert demo user
INSERT INTO users (username, password, full_name, email)
VALUES ('admin', 'admin123', 'Administrator', 'admin@example.com');
```

### 2. Create Database Connection Class

Create a new file: `src/main/java/com/advertising/util/DatabaseConnection.java`

```java
package com.advertising.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/advertising_db";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // Update with your MySQL password

    private static Connection connection = null;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver not found", e);
            }
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
```

### 3. Update Controllers

The controller files (`LoginController.java` and `DashboardController.java`) contain detailed comments showing exactly where to replace static data with database queries. Look for sections marked:

```java
// ========== STATIC DATA (FOR DEMONSTRATION) ==========
// ... static code ...

// ========== DATABASE INTEGRATION (FUTURE IMPLEMENTATION) ==========
// ... commented database code ready to be uncommented and customized ...
```

### 4. Configure WAMP

If using WAMP:
1. Install WAMP Server
2. Start all services (Apache, MySQL)
3. Create the database using phpMyAdmin
4. Update the database connection URL in `DatabaseConnection.java` if needed

## Security Notes

**Important**: The current implementation uses plain text passwords for demonstration only. When integrating with a database:

1. **Hash passwords** using BCrypt or similar algorithms
2. **Use PreparedStatements** to prevent SQL injection
3. **Validate all inputs** before processing
4. **Implement proper session management**
5. **Use environment variables** for sensitive configuration

Example password hashing:
```java
// For hashing (when creating user):
String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());

// For verification (when logging in):
if (BCrypt.checkpw(enteredPassword, storedHashedPassword)) {
    // Login successful
}
```

## Troubleshooting

### Issue: JavaFX runtime components are missing
**Solution**: Make sure you're using the `javafx-maven-plugin` to run the application:
```bash
mvn javafx:run
```

### Issue: Maven dependencies not downloading
**Solution**: Clear Maven cache and update:
```bash
mvn clean install -U
```

### Issue: Application doesn't start
**Solution**: Ensure Java 17+ is installed and JAVA_HOME is set correctly

### Issue: CSS styles not applying
**Solution**: Check that the CSS file path in `MainApp.java` matches the actual file location

## Future Development

This skeleton is designed to be easily extended. To add new features:

1. Create new FXML files in `src/main/resources/fxml/`
2. Create corresponding controller classes in `src/main/java/com/advertising/controller/`
3. Add navigation methods in `MainApp.java`
4. Update the sidebar menu in `Dashboard.fxml`
5. Create database tables and queries
6. Replace static data with database calls

## License

This project is created for internal use by the advertising company.

## Support

For questions or issues, contact the development team.
