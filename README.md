<![CDATA[<div align="center">

# 🏨 Hotel Management System

### A Modern, Feature-Rich Desktop Application Built with Java & JavaFX

[![Java](https://img.shields.io/badge/Java-23-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21.0.2-0095D5?style=for-the-badge&logo=java&logoColor=white)](https://openjfx.io/)
[![Maven](https://img.shields.io/badge/Maven-Build-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)](LICENSE)

> *Streamline your hotel operations with a sleek dark-themed interface, real-time analytics, and automated billing — all in a single desktop application.*

---

</div>

## ✨ Features at a Glance

| Feature | Description |
|:---:|---|
| 📊 **Live Dashboard** | Real-time stats — total rooms, availability, occupancy, and revenue — updated instantly |
| 🛏️ **Room Management** | Add, search, filter, and delete rooms across 4 categories (Single, Double, Suite, Deluxe) |
| 📝 **Smart Booking** | Threaded booking engine with async processing, progress indicators, and form validation |
| 💰 **Automated Billing** | GST-inclusive invoices auto-generated at checkout with detailed cost breakdowns |
| 🔍 **Powerful Search** | Real-time filtering across rooms and bills by name, ID, type, or room number |
| 🌙 **Dark Theme** | Professional Material Design-inspired dark UI — easy on the eyes, premium in feel |
| 💾 **Persistent Storage** | File-based serialization ensures data survives application restarts |
| 🧵 **Concurrency** | Thread-safe operations with background booking threads and synchronized service layer |

---

## 🖥️ Application Overview

### 📊 Dashboard
The landing page provides an at-a-glance summary of your hotel's status:
- **Stat Cards** — Total Rooms, Available, Booked, and Total Revenue
- **Recent Guests** — The last 5 check-ins with guest name, room number, and contact
- **Quick Actions** — One-click access to Add Room, Book Room, Checkout, and Refresh

### 🛏️ Rooms View
Full room inventory management with:
- **Table View** with Room No., Type, Price (₹), Status, Guest, and Actions
- **Status Badges** — Green for Available, Red for Booked
- **Combo Filter** — Toggle between All / Available / Booked rooms
- **Per-Row Actions** — Cancel Booking (booked rooms) or Delete (available rooms only)
- **Safety Guards** — Prevents deletion of occupied rooms

### 📝 Bookings
Manage all active reservations:
- View all current guests with room assignments
- Book new rooms with full customer detail entry
- Cancel existing bookings with confirmation dialogs
- **10-digit phone number validation** — input is restricted at the field level

### 💰 Billing & Invoicing
Comprehensive billing system:
- **Revenue Analytics** — Total Revenue, Bills Generated, Average Bill Value, Tax Collected
- **Bill Search** — Filter by customer name, room number, bill ID, or room type
- **Invoice Viewer** — Detailed modal with Guest Details, Room Details, Stay Duration, Charges, and GST breakdown
- **Tax Calculation** — 18% GST automatically applied on all bills

---

## 🏗️ Architecture

The project follows a **hybrid MVC (Model-View-Controller)** architecture with a clean separation of concerns:

```
com.raahul.hms
├── 📦 model/                  # Data Models (Serializable POJOs)
│   ├── Customer.java          # Guest entity — id, name, contact, roomNo
│   ├── Room.java              # Room entity — roomNo, type, price, availability
│   └── Bill.java              # Invoice entity — auto-calculates charges + GST
│
├── 🎨 ui/                     # Programmatic JavaFX Views
│   ├── MainView.java          # Primary layout — sidebar nav + content area
│   ├── ViewRoomsView.java     # Room table with search, filter, actions
│   ├── AddRoomDialog.java     # Modal form to add new rooms
│   ├── BookRoomDialog.java    # Modal form to book rooms (async)
│   └── CheckoutDialog.java    # Modal form for guest checkout + billing
│
├── 🎛️ controller/             # FXML Controllers
│   ├── DashboardController.java  # Dashboard stats, recent guests, quick actions
│   └── BillingController.java    # Revenue analytics, bill search, invoices
│
├── ⚙️ service/                # Business Logic Layer
│   ├── HotelService.java      # Central facade — all operations (synchronized)
│   └── FileStorageManager.java # Generic<T> file persistence (Repository pattern)
│
├── 🧵 thread/                 # Concurrency
│   └── BookingThread.java     # Background thread for async booking
│
└── 🚀 App.java                # JavaFX Application entry point
```

```
resources/com/raahul/hms/
├── styles.css                 # Dark theme stylesheet (272 lines)
└── fxml/
    ├── DashboardView.fxml     # Dashboard layout (Scene Builder compatible)
    └── BillingView.fxml       # Billing layout (Scene Builder compatible)
```

---

## 🔧 Tech Stack

| Layer | Technology |
|---|---|
| **Language** | Java 23 |
| **GUI Framework** | JavaFX 21.0.2 (Controls + FXML) |
| **Build Tool** | Apache Maven (with Maven Wrapper) |
| **Persistence** | Java Object Serialization (`ObjectOutputStream` / `ObjectInputStream`) |
| **Module System** | Java Platform Module System (JPMS) |
| **Styling** | Custom CSS with dark theme |

---

## 🎓 Java Concepts Demonstrated

This project serves as a comprehensive demonstration of core and advanced Java concepts:

| Concept | Implementation |
|---|---|
| **OOP — Inheritance** | `App extends Application`, `BookingThread extends Thread` |
| **OOP — Polymorphism** | Method overriding (`start()`, `run()`, `toString()`, `equals()`, `hashCode()`) |
| **OOP — Encapsulation** | Private fields with public getters/setters across all models |
| **OOP — Abstraction** | Service facade hides data access complexity from UI layer |
| **Generics** | `FileStorageManager<T extends Serializable>` — bounded type parameters |
| **Collections Framework** | `ArrayList`, `List` — extensive use throughout |
| **Streams API** | `filter()`, `collect()`, `mapToDouble()`, `mapToInt()`, `findFirst()`, `sum()`, `max()` |
| **Lambda Expressions** | Event handlers, cell factories, stream operations, `TextFormatter` |
| **Functional Interfaces** | `Consumer<Boolean>` (callbacks), `Runnable` (navigation hooks) |
| **Multithreading** | `Thread` subclassing, `synchronized` methods, daemon threads, `Platform.runLater()` |
| **File I/O** | `ObjectOutputStream`, `ObjectInputStream`, `Files.createDirectories()` |
| **Serialization** | All models implement `Serializable` with `serialVersionUID` |
| **Exception Handling** | `try-catch` for `IOException`, `ClassNotFoundException`, `NumberFormatException` |
| **Date/Time API** | `LocalDate`, `ChronoUnit.DAYS.between()` for stay duration calculation |
| **JPMS (Modules)** | `module-info.java` with `requires`, `opens`, `exports` |
| **Design Patterns** | Repository, Service Facade, Observer, Callback |

---

## 🚀 Getting Started

### Prerequisites

- **Java 23** (or later) — [Download OpenJDK](https://openjdk.org/)
- **Git** — [Download Git](https://git-scm.com/)

> **Note:** Maven is **not** required — the project includes a Maven Wrapper (`mvnw` / `mvnw.cmd`).

### Installation

```bash
# Clone the repository
git clone https://github.com/shenzy-125/Hotel-Management-System.git

# Navigate to the project directory
cd Hotel-Management-System
```

### Running the Application

**Windows:**
```powershell
.\mvnw.cmd javafx:run
```

**macOS / Linux:**
```bash
./mvnw javafx:run
```

The application will compile, download dependencies (first run only), and launch the Hotel Management System window.

### Clean Build + Run

```bash
.\mvnw.cmd clean javafx:run
```

---

## 📂 Data Storage

All data is persisted in the `data/` directory using Java serialization:

| File | Contents |
|---|---|
| `data/rooms.dat` | Room inventory (room numbers, types, prices, availability) |
| `data/customers.dat` | Active guest records (IDs, names, contacts, room assignments) |
| `data/bills.dat` | Generated invoices (charges, taxes, dates, totals) |

> Data files are created automatically on first use and persist across application restarts.

---

## 💡 Usage Guide

### Adding a Room
1. Navigate to **Rooms** or click **Add Room** from the Dashboard
2. Enter the Room Number, select a Type (Single / Double / Suite / Deluxe), and set the Price
3. Click **Add Room** — the room appears in the inventory as Available

### Booking a Room
1. Navigate to **Bookings** or click **Book Room** from the Dashboard
2. Select an available room from the list
3. Enter Customer ID, Full Name, and a valid **10-digit Contact Number**
4. Click **Book Room** — booking is processed on a background thread with a progress spinner

### Checking Out a Guest
1. Navigate to **Bookings** or click **Checkout** from the Dashboard
2. Select the booked room
3. Pick the Check-in and Check-out dates
4. Click **Checkout** — an invoice is auto-generated with room charges + 18% GST

### Viewing an Invoice
1. Navigate to **Billing**
2. Select a bill from the table
3. Click **View Invoice** — a detailed breakdown appears in a modal dialog

---

## 🤝 Contributing

Contributions are welcome! Feel free to:

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/your-feature`)
3. **Commit** your changes (`git commit -m "Add your feature"`)
4. **Push** to the branch (`git push origin feature/your-feature`)
5. **Open** a Pull Request

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).

---

<div align="center">

**Built with ❤️ using Java & JavaFX**

*If you found this project useful, consider giving it a ⭐!*

</div>
]]>