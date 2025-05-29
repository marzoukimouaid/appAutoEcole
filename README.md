# Gestion Auto-École 🚗

A JavaFX desktop application for managing driving school operations, developed as a university project.

## 🚀 Features

- Candidate & instructor management
- Payment tracking with installment support
- Scheduling for code & driving sessions
- Exam registration & status tracking
- Vehicle maintenance & alert system
- Role-based dashboards & AES-encrypted sessions
- BCrypt-secured user authentication
- Real-time notifications (via local logic)

## 🛠 Tech Stack

- Java 8
- JavaFX (FXML UI)
- MySQL (XAMPP)
- PDFBox (PDF generation)
- ImgBB API (image storage)


## 🧪 Testing

- Manually tested key features: session validation, scheduling conflicts, notifications
- Future work: JUnit-based automated testing

## 🔐 Security

- AES-encrypted sessions (24h validity)
- BCrypt password hashing
- Role-based login & session management

## ⚙️ Installation

1. **Prerequisites**
   - Java 8
   - JavaFX SDK
   - MySQL via XAMPP

2. **Setup**
   - Create database `autoecole`
   - Import `autoecole.sql` (provided)

3. **Run**
   - Launch `App.java`
   - Default login: `secretaire / secretaire`

## 🧭 Architecture

Layered design:
- Presentation → Controller → Service → DAO → Entity
- Utility classes handle encryption, alerts, etc.

## 🛤 Roadmap

- Add unit/integration tests
- Real-time notification popups
- Messaging module
- Multilingual & multi-site support
- API integrations (maps, payments)

## 📄 License

Academic use only.

---
 Contributions welcome via pull request.
