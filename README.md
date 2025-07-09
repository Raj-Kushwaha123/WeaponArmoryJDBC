# WeaponArmoryJDBC
# Weapon Armory JDBC Management System

This is a Java-based console application that uses JDBC and MySQL to manage a fictional Weapon Armory database.

---

# Features

- Add, view, and update **Weapons** and **Attachments**
- Maintain **Customer** and **Employee** records
- Manage **License** information for customers
- Place **Orders** and manage **Transactions**
- Supports many-to-many relationships:
  - Weapons ↔ Attachments
  - Orders ↔ Weapons
  - Orders ↔ Attachments

---

# Project Structure

WeaponArmoryJDBC/
├── src/
│ └── WeaponArmoryJDBC.java # Java code
├── schema/
│ └── weapon_armory_schema.sql # MySQL schema file
└── README.md
