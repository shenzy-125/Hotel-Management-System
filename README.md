# Hotel Management System

A desktop application for managing hotel operations — rooms, bookings, billing, and guest records — built with **Java** and **JavaFX**.

---

## What It Does

- **Dashboard** — See room availability, occupancy, revenue, and recent guests at a glance.
- **Room Management** — Add rooms (Single, Double, Suite, Deluxe), set custom pricing, search and filter by status.
- **Bookings** — Book rooms for guests with validated contact info, cancel reservations, view all active bookings.
- **Checkout & Billing** — Generate invoices with automatic stay duration calculation and 18% GST. View detailed bill breakdowns anytime.
- **Data Persistence** — All data is saved to disk and survives app restarts.

---

## How to Run

**Prerequisites:** Java 23 or later ([Download](https://openjdk.org/))

```bash
# Clone the repo
git clone https://github.com/shenzy-125/Hotel-Management-System.git
cd Hotel-Management-System

# Run (Maven Wrapper is included — no Maven install needed)
.\mvnw.cmd javafx:run        # Windows
./mvnw javafx:run             # macOS / Linux
```

---

## Project Structure

```
src/main/java/com/raahul/hms/
│
├── App.java                        # Entry point
│
├── model/
│   ├── Customer.java               # Guest data (id, name, contact, room)
│   ├── Room.java                   # Room data (number, type, price, status)
│   └── Bill.java                   # Invoice with auto-calculated GST
│
├── service/
│   ├── HotelService.java           # Core business logic (thread-safe)
│   └── FileStorageManager.java     # Generic file-based persistence
│
├── ui/
│   ├── MainView.java               # Main layout with sidebar navigation
│   ├── ViewRoomsView.java          # Room table with search and filters
│   ├── AddRoomDialog.java          # Dialog to add new rooms
│   ├── BookRoomDialog.java         # Dialog to book a room
│   └── CheckoutDialog.java         # Dialog for checkout and billing
│
├── controller/
│   ├── DashboardController.java    # Dashboard stats and quick actions
│   └── BillingController.java      # Revenue analytics and invoice viewer
│
└── thread/
    └── BookingThread.java          # Background thread for async bookings
```

---

## Key Concepts Used

**Object-Oriented Programming**
- Inheritance (`App extends Application`, `BookingThread extends Thread`)
- Encapsulation (private fields, public getters/setters)
- Polymorphism (method overriding — `toString`, `equals`, `hashCode`)

**Java Features**
- Generics — `FileStorageManager<T extends Serializable>`
- Streams API — filtering, mapping, aggregation across rooms, customers, and bills
- Lambda expressions — event handlers, cell factories, formatters
- Multithreading — `synchronized` service methods, daemon threads, `Platform.runLater()`
- Serialization — persistent storage via `ObjectOutputStream` / `ObjectInputStream`
- Java Date/Time API — `LocalDate`, `ChronoUnit` for stay duration
- JPMS — `module-info.java` with proper module declarations

**Design Patterns**
- MVC (Model-View-Controller)
- Repository pattern (`FileStorageManager`)
- Service Facade (`HotelService`)
- Callback pattern (`Consumer<Boolean>`)

**JavaFX**
- Programmatic UI (MainView, dialogs) + declarative FXML (Dashboard, Billing)
- Custom CSS dark theme (272 lines)
- TableView with custom cell factories and status badges
- Modal dialogs, progress indicators, real-time search filtering

---

## Data Storage

Data is stored in the `data/` directory (created automatically):

| File | Contents |
|---|---|
| `rooms.dat` | Room inventory |
| `customers.dat` | Active guest records |
| `bills.dat` | Generated invoices |

---

## Built With

- Java 23
- JavaFX 21.0.2
- Apache Maven (with included wrapper)

---

## License

Open source under the [MIT License](LICENSE).