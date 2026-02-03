# Quick Start Guide

## Prerequisites Checklist

Before running the application, ensure you have:

- [ ] Java JDK 17 or higher installed
- [ ] Maven installed
- [ ] JAVA_HOME environment variable set

## Installation & Running

### Step 1: Verify Prerequisites

**Check Java:**
```bash
java -version
```
Expected output: Java version 17 or higher

**Check Maven:**
```bash
mvn -version
```
Expected output: Maven version info

### Step 2: Run the Application

**On Windows:**
- Double-click `run.bat`
- Or open Command Prompt in project folder and type: `run.bat`

**On Linux/Mac:**
```bash
./run.sh
```

**Alternative (any OS):**
```bash
mvn clean compile
mvn javafx:run
```

### Step 3: Login

When the application starts:
- **Username:** admin
- **Password:** admin123

### Step 4: Explore the Dashboard

After login, you'll see:
- Total Services: 5
- Total Sales: $15,000
- Active Clients: 3
- Performance metrics with progress bars

Click the sidebar buttons to see placeholders for future modules.

## Application Flow

```
Start Application
    ↓
Login Page (admin/admin123)
    ↓
[Validate Credentials]
    ↓
Dashboard
    ├─ View Statistics
    ├─ Monitor Performance
    └─ Navigate to:
        ├─ Clients Module (placeholder)
        ├─ Services Module (placeholder)
        ├─ Suppliers Module (placeholder)
        └─ Payments Module (placeholder)
```

## Current Features

### Login Page
- Username and password fields
- Input validation
- Error messaging
- Professional styling

### Dashboard
- Real-time statistics display
- Performance monitoring
- Navigation sidebar
- Logout functionality

## Static Demo Data

The application currently uses these static values for demonstration:

| Metric | Value |
|--------|-------|
| Total Services | 5 |
| Total Sales | $15,000 |
| Active Clients | 3 |
| Services Completed | 80% |
| Client Satisfaction | 95% |
| Revenue Goal | 65% |

## Troubleshooting

### Problem: "mvn: command not found"
**Solution:** Install Maven from https://maven.apache.org/download.cgi

### Problem: "java: command not found"
**Solution:** Install Java JDK 17+ from https://adoptium.net/

### Problem: Application doesn't start
**Solution:**
1. Make sure Java 17+ is installed
2. Set JAVA_HOME environment variable
3. Run `mvn clean compile` first

### Problem: "Error: JavaFX runtime components are missing"
**Solution:** Use `mvn javafx:run` instead of running JAR directly

## Adding Database Support

When you're ready to connect to MySQL/WAMP:

1. **Set up database:**
   ```bash
   mysql -u root -p < database/schema.sql
   ```

2. **Configure connection:**
   - Copy `DatabaseConnection.java.template` to `DatabaseConnection.java`
   - Update credentials in the new file

3. **Update controllers:**
   - Open `LoginController.java` and `DashboardController.java`
   - Find sections marked "DATABASE INTEGRATION"
   - Uncomment database code
   - Remove static data sections

## Project Structure

```
advertising-management/
├── src/main/java/com/advertising/
│   ├── MainApp.java              # Entry point
│   └── controller/
│       ├── LoginController.java   # Login logic
│       └── DashboardController.java  # Dashboard logic
├── src/main/resources/
│   ├── fxml/                     # UI layouts
│   └── css/                      # Styling
├── database/
│   └── schema.sql                # Database setup
└── pom.xml                       # Maven config
```

## Next Steps

1. ✅ Run the application
2. ✅ Test login functionality
3. ✅ Explore the dashboard
4. ⏹ Set up MySQL database (optional)
5. ⏹ Integrate database queries (optional)
6. ⏹ Implement additional modules (clients, services, etc.)

## Support

For detailed information, see:
- **README.md** - Complete documentation
- **PROJECT_STRUCTURE.md** - Detailed file descriptions
- **database/schema.sql** - Database structure

## Default Credentials

Remember:
- Username: `admin`
- Password: `admin123`

Enjoy using the Advertising Company Management System!
