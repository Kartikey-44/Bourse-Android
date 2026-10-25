# UI Documentation — Bourse

## 1. Screen Inventory (10 Activities)

| Screen | Technology | Entry Point | Purpose | ViewModel |
|---|---|---|---|---|
| `SplashActivity` | XML + ViewBinding | App Launcher | Initial session check & routing | None |
| `LoginActivity` | XML + ViewBinding | `SplashActivity` / Logout | User authentication | `AuthViewModel` |
| `SignupActivity` | XML + ViewBinding | `LoginActivity` | New account registration | `AuthViewModel` |
| `MainActivity` | XML + ViewBinding | Authentication / Splash | Home dashboard & portfolio summary | `HomeViewModel` |
| `MarketActivity` | XML + ViewBinding | Bottom Nav / Home | Stock market exploration & search | `MarketViewModel` |
| `StockDetailsActivity` | XML + ViewBinding | Market / Portfolio list | Stock details, history chart, & trading | `StockDetailsViewModel`, `TradingViewModel` |
| `PortfolioActivity` | XML + ViewBinding | Bottom Nav | Holdings & portfolio overview | `PortfolioViewModel` |
| `AllHoldingsActivity` | XML + programmatic UI | Profile / Portfolio | List all active stock holdings | `PortfolioViewModel` |
| `TransactionsActivity` | XML + ViewBinding | Home / Portfolio | Trade history & filtering | `TransactionsViewModel` |
| `ProfileActivity` | XML + ViewBinding | Bottom Nav | Account info, PIN setup, & theme toggle | `ProfileViewModel`, `TransactionPinViewModel` |

## 2. Reusable UI Components & Dialogs
- **Bottom Navigation Bar**: Fixed 4-tab bottom navigation (`Home`, `Market`, `Portfolio`, `Profile`) present across primary screens.
- **Quantity Dialog (`dialog_quantity.xml`)**: Modal dialog for specifying share quantity with plus/minus steppers and estimated total calculation.
- **PIN Dialog (`dialog_pin.xml`)**: Secure 4-digit PIN entry dialog for trade confirmation.
- **PIN Setup Dialog (`dialog_change_pin.xml`)**: Dialog for creating or modifying the 4-digit transaction PIN.
- **Loading & Result Dialogs (`dialog_loading.xml`, `dialog_success.xml`, `dialog_failure.xml`)**: Themed dark dialogs indicating async progress, successful trade execution, or failure messages.

## 3. UI State Management
- **Loading State**: Displays progress indicators (`ProgressBar`, loading dialogs) while keeping layouts stable.
- **Success State**: Binds DTO data into views, cards, RecyclerView adapters, and MPAndroidChart line charts.
- **Error State**: Displays retry-enabled error states or alert dialogs.
- **Unauthorized State (`401`)**: Automatically clears local JWT tokens via `TokenManager` and redirects the user to `LoginActivity`.
