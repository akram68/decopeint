# Project Structure

```
advertising-management/
│
├── pom.xml                              # Maven configuration with JavaFX dependencies
├── README.md                            # Complete documentation and setup guide
├── PROJECT_STRUCTURE.md                 # This file - project structure overview
├── .gitignore                           # Git ignore rules
├── run.sh                               # Linux/Mac run script
├── run.bat                              # Windows run script
│
├── database/
│   └── schema.sql                       # MySQL database schema with sample data
│
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── advertising/
        │           ├── MainApp.java                          # Main application entry point
        │           │
        │           ├── controller/
        │           │   ├── LoginController.java              # Login page controller
        │           │   └── DashboardController.java          # Dashboard controller
        │           │
        │           └── util/
        │               └── DatabaseConnection.java.template  # Database connection template
        │
        └── resources/
            ├── fxml/
            │   ├── Login.fxml           # Login page layout
            │   └── Dashboard.fxml       # Dashboard layout
            │
            └── css/
                └── style.css            # Application styling
```

## File Descriptions

### Root Files

- **pom.xml**: Maven project configuration with JavaFX and MySQL dependencies
- **README.md**: Comprehensive documentation including setup, usage, and database integration guide
- **run.sh**: Bash script to compile and run the application (Linux/Mac)
- **run.bat**: Batch script to compile and run the application (Windows)
- **.gitignore**: Git ignore patterns for Java/Maven projects

### Database

- **schema.sql**: Complete MySQL database schema including:
  - Tables: users, clients, services, suppliers, payments, projects
  - Sample data for demonstration
  - Useful views for dashboard statistics
  - Indexes for performance

### Java Source Files

#### Main Application

- **MainApp.java**: Application entry point, manages scenes and navigation between pages

#### Controllers

- **LoginController.java**: Handles login authentication with static credentials and detailed comments for database integration
- **DashboardController.java**: Manages dashboard data display and navigation with static demo data

#### Utilities (Template)

- **DatabaseConnection.java.template**: Ready-to-use MySQL connection manager template

### Resources

#### FXML Layouts

- **Login.fxml**: Professional login page with username, password fields, and validation
- **Dashboard.fxml**: Feature-rich dashboard with:
  - Navigation sidebar (Dashboard, Clients, Services, Suppliers, Payments)
  - Statistics cards (Total Services, Total Sales, Active Clients)
  - Performance indicators with progress bars

#### Stylesheets

- **style.css**: Modern, professional styling with:
  - Custom color scheme
  - Hover effects and transitions
  - Responsive layout components

## Key Features

### Current Implementation

1. **Login System**
   - Static authentication (admin/admin123)
   - Input validation
   - Error messaging
   - Ready for database integration

2. **Dashboard**
   - Statistics overview (5 services, $15,000 sales, 3 clients)
   - Performance metrics with progress bars
   - Navigation sidebar
   - Logout functionality

3. **Navigation Framework**
   - Scene management
   - Easy navigation between pages
   - Prepared for future modules

### Ready for Implementation

1. **Client Management Module**
2. **Services Management Module**
3. **Suppliers Management Module**
4. **Payments Management Module**
5. **Database Integration** (MySQL/WAMP)

## Database Integration Points

All controllers contain detailed comments marking where static data should be replaced with database queries. Look for these sections:

```java
// ========== STATIC DATA (FOR DEMONSTRATION) ==========
// ... current static implementation ...

// ========== DATABASE INTEGRATION (FUTURE IMPLEMENTATION) ==========
// ... commented code showing exact database queries to use ...
```

## Quick Start

### For Linux/Mac:
```bash
./run.sh
```

### For Windows:
```
run.bat
```

### Manual:
```bash
mvn clean compile
mvn javafx:run
```

## Technologies Used

- Java 17+
- JavaFX 21
- Maven
- FXML
- CSS3
- MySQL Connector (ready for integration)

## Next Steps

1. Install Java JDK 17+
2. Install Maven
3. Run the application using the provided scripts
4. When ready for database:
   - Install MySQL/WAMP
   - Execute `database/schema.sql`
   - Rename `DatabaseConnection.java.template` to `DatabaseConnection.java`
   - Update database credentials
   - Uncomment database code in controllers
   - Remove static data sections

## Support

Refer to README.md for detailed instructions and troubleshooting.
