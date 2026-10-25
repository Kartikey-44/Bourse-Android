# Data and Storage — Bourse Android Client

## 1. Remote Data Layer
- **Retrofit DTOs**: Serialized and deserialized using Gson 2.10.1.
- **Custom Deserialization**: `TimestampDeserializer` handles Java `LocalDateTime` timestamps returned either as ISO-8601 strings or numeric arrays (`[year, month, day, hour, minute, second]`).
- **Repositories**: `UserRepository`, `PortfolioRepository`, `StockRepository`, and `TradingRepository` abstract API responses into Kotlin `Result<T>` structures.

## 2. Local Storage
- **`TokenManager`**: SharedPreferences (`"bourse_secure_auth"`). Persists the JWT authentication token (`"jwt_token"`) and user name across app restarts.
- **`TransactionPinStore`**: Encrypted SharedPreferences (`"bourse_secure_pin"`). Stores the user's 4-digit Transaction PIN encrypted using AES/GCM via AndroidKeyStore (`"bourse_transaction_pin"`).
- **No Local Database**: **VERIFIED FROM ANDROID SOURCE**: The app does not maintain any local Room or SQLite database, relying entirely on remote server state and local secure SharedPreferences for session and PIN persistence.
