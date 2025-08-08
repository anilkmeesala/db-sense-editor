# DB Editor

A simple Java Swing-based database editor with SQL syntax highlighting and auto-completion.

## Features

- SQL syntax highlighting
- Auto-completion for SQL keywords, functions, and database objects
- Execute SQL queries and view results in a table
- Support for multiple database types (MySQL, PostgreSQL, etc.)

## Auto-Completion

The editor includes intelligent auto-completion that provides suggestions for:

- SQL keywords (SELECT, FROM, WHERE, etc.)
- SQL functions (COUNT, SUM, AVG, etc.)
- Database tables and columns (if connected to a database)

### How to use auto-completion:

1. **Automatic trigger**: Type 2-3 letters of a SQL keyword (e.g., "SL" for "SELECT", "FR" for "FROM")
2. **Manual trigger**: Press `Ctrl+Space` to manually trigger auto-completion
3. **Test button**: Use the "Test Auto-Complete" button to focus the editor and then type "SL" to see SELECT suggestions

### Examples:
- Type "SL" → shows "SELECT" suggestion
- Type "FR" → shows "FROM" suggestion  
- Type "WH" → shows "WHERE" suggestion
- Type "CO" → shows "COUNT" suggestion

### Auto-completion includes:
- Basic SQL keywords: SELECT, FROM, WHERE, INSERT, UPDATE, DELETE, etc.
- SQL functions: COUNT(*), SUM(), AVG(), MAX(), MIN(), etc.
- Database tables and columns (when connected to a database)

## Configuration

Create a `config.properties` file in the project root with your database connection details:

```properties
db.url=jdbc:mysql://localhost:3306/your_database
db.user=your_username
db.password=your_password
db.driver=com.mysql.cj.jdbc.Driver
```

## Building and Running

```bash
# Compile the project
mvn clean compile

# Run the application
java -cp target/classes;target/dependency/* app.Main

# Or build a JAR with dependencies
mvn clean package
java -jar target/db-editor-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## Dependencies

- RSyntaxTextArea 3.3.2 - For syntax highlighting and auto-completion
- MySQL Connector/J 9.4.0 - For MySQL database connectivity
- SLF4J Simple 2.0.9 - For logging

## Usage

1. Start the application
2. Configure your database connection in `config.properties`
3. Type SQL queries in the editor
4. Use `Ctrl+Enter` or click "Run" to execute queries
5. View results in the table below the editor
6. Type 2-3 letters of SQL keywords to see auto-completion suggestions

<img width="1080" height="734" alt="phase-2-1" src="https://github.com/user-attachments/assets/d89d0d43-7981-40a0-b6ee-8b551c722c79" />
<img width="1084" height="744" alt="phase-2-2" src="https://github.com/user-attachments/assets/01238cf5-27f8-4b54-b9f9-0c3309f924aa" />

