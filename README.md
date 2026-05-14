# NovaBank Banking System

A Java CLI Banking System with MySQL database.

## Group Members
- David (Database & Security Lead)
- Benjamina (CustomerDAO & Customer Menu)
- George (AccountDAO & Account Menu)
- Emmanuel (TransactionDAO & Transaction Menu)

## Tech Stack
- Java 17+
- MySQL 8.x
- JDBC (mysql-connector-j-9.7.0)
- NetBeans IDE

## Project Structure
src/
banking/
config/     - DatabaseConnection (Singleton)
model/      - Customer, Account, AccountType, Transaction
dao/        - CustomerDAO, AccountDAO, TransactionDAO
util/       - InputValidator, Printer
ui/         - BankingCLI (Main menu)

## Database Setup
1. Install MySQL 8.x
2. Open MySQL Workbench
3. Run banking_system.sql
4. Database and sample data will be created automatically

## How to Run
1. Open project in NetBeans
2. Add mysql-connector-j-9.7.0.jar to project Libraries
3. Run BankingCLI.java as main class

## Database Credentials
- Host: localhost:3306
- Database: banking_system
- Username: root
- Password: contact David
