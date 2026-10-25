# Bourse — The Trading Simulation Platform (Android Client)

## 1. Project Introduction
**Bourse — The Trading Simulation Platform** is the Android client application repository for Bourse. It enables users to experience simulated equity trading, track portfolio performance, view stock market data and interactive stock price history charts, and execute simulated `BUY` and `SELL` orders secured by a local 4-digit Transaction PIN.

The Android application consumes market, trading, portfolio, and user data from the Bourse backend via REST APIs.

---

## 2. Important Repository Boundary
This repository contains the **Android client application only**. The backend is maintained separately and is not available in this repository. 

Backend implementation details (such as database schemas, business logic, authorization rules, and infrastructure state) are not part of this codebase and cannot be determined solely from the Android source.

---

## 3. Technology Stack

| Technology | Usage |
|---|---|
| Kotlin | Android application (version determined by Gradle configuration) |
| XML | UI layouts |
| ViewBinding | Type-safe view access |
| MVVM | Application architecture |
| Repository Pattern | Data access abstraction |
| Retrofit (2.9.0) | REST API communication |
| OkHttp (4.11.0) | HTTP transport & interceptors |
| Gson (2.10.1) | JSON serialization / deserialization |
| Coroutines (1.8.1) | Asynchronous operations |
| LiveData (2.8.7) | Observable UI state |
| MPAndroidChart (3.1.0) | Stock price history charts |
| SharedPreferences | Local session & preference data |
| AndroidKeyStore | Secure Transaction PIN key storage |
| AES-GCM | Transaction PIN encryption at rest |

---

## 4. Architecture
The application uses an **MVVM + Repository architecture** with multiple Activities and ViewBinding. Dependencies are instantiated manually in ViewModels and Activities; there is no automated Dependency Injection framework (such as Hilt or Dagger).

```text
Activity / UI
     ↓
ViewModel
     ↓
Repository
     ↓
Retrofit API
     ↓
Bourse Backend
```

Local storage handling is structured separately:
```text
Activity / ViewModel
       ↓
Local storage
       ├── TokenManager (SharedPreferences)
       └── TransactionPinStore (AndroidKeyStore + AES-GCM)
```

---

## 5. Screens & Application Structure
The verified Android source implements **10 Activities**:

- **`SplashActivity`**: App launch entry point; performs initial JWT session check and routes accordingly.
- **`LoginActivity`**: User authentication screen; validates credentials, handles JWT persistence, and checks local Transaction PIN status.
- **`SignupActivity`**: New user registration screen; registers account, authenticates, and initializes PIN setup.
- **`MainActivity`**: Home dashboard screen; displays portfolio valuation summary, cash/invested balances, and recent activity.
- **`MarketActivity`**: Market screen; lists available stocks with search filtering and price change percentages.
- **`StockDetailsActivity`**: Stock details screen; shows company info, MPAndroidChart historical price trend chart, and modal quantity/PIN trading dialogs.
- **`PortfolioActivity`**: Portfolio screen; displays active holdings, returns, recommendations, and transaction history entry.
- **`AllHoldingsActivity`**: Dedicated view listing all active stock holdings.
- **`TransactionsActivity`**: Transaction history screen; lists trades with filter pills (`ALL`, `BUY`, `SELL`) and detail modals.
- **`ProfileActivity`**: Profile screen; manages personal info, transaction PIN updates, theme selection (System, Light, Dark), and explicit logout.

---

## 6. Networking & API Integration
- **Client & Transport**: Retrofit 2.9.0 with Gson converter and OkHttp 4.11.0.
- **Interceptors**: `AuthInterceptor` automatically attaches `Authorization: Bearer <JWT_TOKEN>` headers to outgoing requests. `HttpLoggingInterceptor` logs network traffic during debug builds.
- **Configured Base URL**: `http://bourse-alb-546387840.ap-south-1.elb.amazonaws.com/` (configured with cleartext traffic enabled).
- **API Inventory**: The Android client contains **19 Retrofit API method definitions representing 14 logical API routes**, including five alternative/fallback route definitions used for stock-history retrieval.

For detailed request/response models and endpoint specifications, refer to [docs/API_INTEGRATION.md](docs/API_INTEGRATION.md).

---

## 7. Authentication & Security
- **JWT Session**: User login and registration return a JWT bearer token stored locally via `TokenManager` (`SharedPreferences`). `AuthInterceptor` injects the token into requests. A `401 Unauthorized` response clears the session and redirects to `LoginActivity`.
- **Transaction PIN**: A local client-side transaction confirmation mechanism separate from backend authentication. Stored securely using AndroidKeyStore (AES-GCM encryption) via `TransactionPinStore`. It is never sent to the backend and is verified locally in `StockDetailsActivity` before trade requests are submitted.
- **Security Consideration**: The currently configured backend URL uses HTTP cleartext (`android:usesCleartextTraffic="true"`). Production deployments should migrate to HTTPS/TLS to protect credentials, JWT tokens, and API traffic in transit.

For details, see [docs/AUTHENTICATION.md](docs/AUTHENTICATION.md).

---

## 8. Local Data & Storage
- **SharedPreferences**: Used exclusively for local session and key-value persistence.
- **`TokenManager`**: Manages the persistent JWT session token and user name.
- **`TransactionPinStore`**: Manages encrypted 4-digit Transaction PIN storage via AndroidKeyStore.
- **No Database**: The application does **not** use Room, SQLite, or DataStore.

For details, see [docs/DATA_AND_STORAGE.md](docs/DATA_AND_STORAGE.md).

---

## 9. Navigation
The application implements a **Multi-Activity architecture**. Navigation between screens is performed via explicit Android Intents with backstack-clearing flags (`FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK`) where appropriate. A persistent 4-tab Bottom Navigation bar connects primary sections (`Home`, `Market`, `Portfolio`, `Profile`).

For details, see [docs/NAVIGATION.md](docs/NAVIGATION.md).

---

## 10. Project Structure
```text
.
├── app/
│   └── src/
│       └── main/
│           ├── java/ind/finance/bourse/
│           │   ├── data/       # local, model, remote
│           │   ├── repository/ # UserRepository, PortfolioRepository, StockRepository, TradingRepository
│           │   ├── ui/         # ViewModels
│           │   ├── *Activity   # Activities (10 total)
│           │   └── *Adapter    # RecyclerView Adapters
│           ├── res/            # Layouts, drawables, values, themes
│           └── AndroidManifest.xml
├── gradle/
├── docs/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## 11. Build & Development Setup

### Requirements
- **IDE**: Android Studio (Compatible with AGP 9.4.1 / Gradle 8.x)
- **JDK**: Java 11 / Java 17 source & target compatibility configured in Gradle
- **SDK**: `minSdk 26`, `compileSdk 37`, `targetSdk 37`

### Build Commands
To build a debug APK from the root project directory:
```bash
./gradlew assembleDebug
```
*(On Windows command prompt, use `gradlew.bat assembleDebug`)*

For detailed setup guides, see [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md).

---

## 12. Documentation Map

| Document | Purpose |
|---|---|
| [`docs/ANDROID_DOCUMENTATION.md`](docs/ANDROID_DOCUMENTATION.md) | Complete Android project documentation & user flows |
| [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) | MVVM architecture & layering |
| [`docs/UI_DOCUMENTATION.md`](docs/UI_DOCUMENTATION.md) | Screen inventory & UI components |
| [`docs/API_INTEGRATION.md`](docs/API_INTEGRATION.md) | Retrofit services & API endpoints inventory |
| [`docs/NAVIGATION.md`](docs/NAVIGATION.md) | Activity navigation flows |
| [`docs/DATA_AND_STORAGE.md`](docs/DATA_AND_STORAGE.md) | Local storage (`TokenManager`, `TransactionPinStore`) |
| [`docs/AUTHENTICATION.md`](docs/AUTHENTICATION.md) | JWT authentication & Transaction PIN security |
| [`docs/DEVELOPMENT.md`](docs/DEVELOPMENT.md) | Requirements, build instructions & Gradle configuration |
| [`docs/TROUBLESHOOTING.md`](docs/TROUBLESHOOTING.md) | Troubleshooting common Gradle & networking issues |

---

## 13. Known Limitations
- **Backend Dependency**: The backend implementation is maintained separately and is not available in this repository.
- **Backend Availability**: Live backend availability and infrastructure state cannot be determined from the Android repository alone.
- **Network Security**: Configured with HTTP cleartext traffic; production environments require HTTPS/TLS.
- **Transaction PIN Scope**: The Transaction PIN is a client-side confirmation mechanism, not server-side authorization.
