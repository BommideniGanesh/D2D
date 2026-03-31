# 📦 D2D — Door-to-Door Logistics Platform

A full-stack logistics management platform for end-to-end shipment tracking, driver management, and order fulfillment. Built with **Angular 21** on the frontend and **Spring Boot 3.2** on the backend, backed by a **MySQL** database.

---

## 🏗️ Architecture Overview

```
┌────────────────────────┐       REST / WebSocket       ┌────────────────────────┐
│                        │  ◄──────────────────────────► │                        │
│   Angular 21 SPA       │        Port 4200 → 8080      │   Spring Boot 3.2      │
│   (angular-app/)       │         (proxy.conf.json)     │ (spring-boot-jpa-demo/)│
│                        │                               │                        │
│  • Bootstrap 5         │                               │  • Spring Data JPA     │
│  • RxJS                │                               │  • Spring Security     │
│  • STOMP WebSockets    │                               │  • JWT Authentication  │
│  • jsPDF Reports       │                               │  • Spring Batch        │
│                        │                               │  • WebSocket (STOMP)   │
└────────────────────────┘                               │  • Spring Mail         │
                                                         └───────────┬────────────┘
                                                                     │
                                                                     ▼
                                                         ┌────────────────────────┐
                                                         │     MySQL (D2D)        │
                                                         │     Port 3306          │
                                                         └────────────────────────┘
```

---

## ✨ Features

### 👤 User Management
- User registration & login with **JWT-based authentication**
- Role-based access control (**Customer**, **Driver**, **Admin**)
- Password recovery via email (SMTP / Gmail)
- User profile management

### 📋 Order Management
- Create shipments with full sender, receiver, and package details
- Support for multiple box types, fragile/hazardous flags, and special handling instructions
- Automated tracking number generation
- Order status lifecycle: `PENDING → ASSIGNED → PICKED_UP → IN_TRANSIT → DELIVERED`
- Order history tracking (JSON-based status timeline)

### 🚚 Driver System
- Driver profile registration (license, vehicle info, service areas)
- Driver availability management (`AVAILABLE`, `ON_DELIVERY`, `OFFLINE`)
- Service area assignment via pincode mapping
- Shipment-to-driver assignment (pickup & delivery)
- Dedicated **Driver Dashboard** with pending/active deliveries
- Proof of Delivery (PoD) image upload

### 📊 Admin Dashboard
- Real-time analytics: revenue, order counts by status, damage reports
- Active user / driver / order detail modals
- **Hire Driver** feature — integrates NYC Open Data API for live for-hire vehicle driver data
- Warehouse management

### 💰 Billing & Payments
- Dynamic pricing engine (base cost, tax, insurance, discounts)
- Multiple payment modes
- PDF invoice generation via **jsPDF**
- Wallet system for users
- Driver earnings tracking
- Batch billing via **Spring Batch**

### 📍 Live Tracking
- Real-time shipment tracking via **WebSockets (STOMP)**
- Tracking page with shipment status updates

### 💬 Messaging & Notifications
- In-app support chat (customer ↔ support)
- Operational chat (internal)
- Chat widget embedded across the platform
- Email notifications via **Spring Mail** (Gmail SMTP)
- Webhook integrations for external event notifications

### 🔄 Returns & Ratings
- Order return processing
- Customer rating & review system

---

## 📁 Project Structure

```
D2D/
├── angular-app/                  # Frontend (Angular 21)
│   ├── src/app/
│   │   ├── admin/                # Admin dashboard & hire-driver
│   │   ├── chat-widget/          # Floating chat widget
│   │   ├── create-order/         # Order creation form
│   │   ├── delivered-orders/     # Delivered order listing
│   │   ├── details/              # Order detail view
│   │   ├── driver/               # Driver dashboard
│   │   ├── earnings/             # Driver earnings page
│   │   ├── forgot-password/      # Password recovery
│   │   ├── guards/               # AuthGuard & RoleGuard
│   │   ├── header/ & footer/     # Layout components
│   │   ├── home/                 # Landing page
│   │   ├── interceptors/         # HTTP interceptors (JWT)
│   │   ├── live-tracking/        # Real-time tracking page
│   │   ├── login/ & register/    # Authentication pages
│   │   ├── models/               # TypeScript interfaces
│   │   ├── my-orders/            # Customer order listing
│   │   ├── operation-chat/       # Internal operations chat
│   │   ├── pending-orders/       # Pending order listing
│   │   ├── profile/              # User profile management
│   │   ├── services/             # API & utility services
│   │   ├── support-chat/         # Customer support chat
│   │   ├── tracking/             # Shipment tracking
│   │   ├── wallet/               # User wallet
│   │   ├── warehouse/            # Warehouse management
│   │   ├── webhooks/             # Webhook configuration
│   │   ├── app.routes.ts         # Route definitions
│   │   └── app.config.ts         # App configuration
│   ├── proxy.conf.json           # Dev proxy (→ backend:8080 & NYC API)
│   └── package.json
│
├── spring-boot-jpa-demo/         # Backend (Spring Boot 3.2)
│   ├── src/main/java/com/example/demo/
│   │   ├── analytics/            # Dashboard analytics service
│   │   ├── assignment/           # Shipment-driver assignment
│   │   ├── authorization/        # JWT & Spring Security config
│   │   ├── billing/              # Billing & batch invoicing
│   │   ├── chat/                 # Chat messaging
│   │   ├── configuration/        # CORS, WebSocket, app config
│   │   ├── delivery/             # Delivery processing
│   │   ├── driver/               # Driver profiles & dashboard
│   │   ├── events/               # Domain event handling
│   │   ├── notifications/        # Email & webhook notifications
│   │   ├── orders/               # Order CRUD (sender, receiver, package, shipment)
│   │   ├── payment/              # Payment processing
│   │   ├── pricing/              # Dynamic pricing engine
│   │   ├── rating/               # Customer reviews & ratings
│   │   ├── returns/              # Return order processing
│   │   ├── scheduler/            # Scheduled tasks
│   │   ├── seeder/               # Database seeder controller
│   │   ├── tracking/             # Real-time tracking
│   │   ├── userauthentication/   # Login & JWT token service
│   │   ├── userregistration/     # User registration
│   │   └── warehouse/            # Warehouse management
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   └── db/                   # Database migrations
│   ├── full_schema_reference.sql # Complete DB schema
│   └── pom.xml
│
├── uploads/                      # Uploaded files (PoD images, etc.)
└── README.md
```

---

## 🛠️ Prerequisites

| Tool         | Version    |
|--------------|------------|
| **Java**     | 17+        |
| **Maven**    | 3.8+       |
| **Node.js**  | 18+        |
| **npm**      | 9+         |
| **MySQL**    | 8.0+       |
| **Angular CLI** | 21.x   |

---

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/BommideniGanesh/D2D.git
cd D2D
```

### 2. Database Setup

Create the MySQL database:

```sql
CREATE DATABASE D2D;
```

> The application uses `spring.jpa.hibernate.ddl-auto=update`, so tables will be auto-created on first run. For a manual reference, see [`full_schema_reference.sql`](spring-boot-jpa-demo/full_schema_reference.sql).

### 3. Configure the Backend

Edit `spring-boot-jpa-demo/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/D2D
spring.datasource.username=root
spring.datasource.password=<your-password>
```

For email notifications, update the SMTP credentials:

```properties
spring.mail.username=<your-email>
spring.mail.password=<your-app-password>
```

### 4. Run the Backend

```bash
cd spring-boot-jpa-demo
./mvnw spring-boot:run
```

The API will start on **http://localhost:8080**.

### 5. Run the Frontend

```bash
cd angular-app
npm install
ng serve --proxy-config proxy.conf.json
```

The app will be available at **http://localhost:4200**.

---

## 🔐 Authentication & Authorization

| Role       | Access                                                |
|------------|-------------------------------------------------------|
| `CUSTOMER` | Create orders, track shipments, wallet, chat, profile |
| `DRIVER`   | Driver dashboard, accept/complete deliveries, earnings |
| `ADMIN`    | Admin dashboard, analytics, hire drivers, warehouse    |

JWT tokens are issued at login and must be included in the `Authorization: Bearer <token>` header for all protected endpoints.

---

## 🗄️ Database Schema

The platform uses **6 core tables**:

| Table                         | Purpose                              |
|-------------------------------|--------------------------------------|
| `users` / `roles` / `user_roles` | User accounts & role assignments  |
| `sender_details`             | Shipment sender information          |
| `receiver_details`           | Shipment receiver information        |
| `package_details`            | Package dimensions, weight, handling |
| `shipments`                  | Core order/shipment records          |
| `driver_profiles`            | Driver license, vehicle, availability|
| `driver_service_areas`       | Driver pincode coverage areas        |
| `shipment_driver_assignments`| Driver ↔ shipment assignment mapping |

---

## 🌐 API Proxy Configuration

During development, the Angular dev server proxies API calls:

| Path       | Target                              |
|------------|-------------------------------------|
| `/api/**`  | `http://localhost:8080` (Backend)    |
| `/nyc-api` | `https://data.cityofnewyork.us` (NYC Open Data) |

---

## 📦 Key Dependencies

### Backend
- **Spring Boot 3.2** — Web, Data JPA, Security, Batch, WebSocket, Mail
- **MySQL Connector/J** — Database driver
- **Lombok** — Boilerplate reduction
- **JJWT 0.11.5** — JWT creation & validation

### Frontend
- **Angular 21** — Core framework
- **Bootstrap 5.3** + **Bootstrap Icons** — UI styling
- **RxJS** — Reactive programming
- **STOMP.js** + **SockJS** — WebSocket communication
- **jsPDF** + **jspdf-autotable** — PDF report generation
- **jwt-decode** — Client-side JWT parsing

---

## 🧪 Running Tests

### Backend
```bash
cd spring-boot-jpa-demo
./mvnw test
```

### Frontend
```bash
cd angular-app
npm test
```

---

## 📄 License

This project is developed for educational and demonstration purposes.

---

## 👨‍💻 Author

**Ganesh Bommideni**
- GitHub: [@BommideniGanesh](https://github.com/BommideniGanesh)
