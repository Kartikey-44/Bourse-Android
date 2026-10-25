# Authentication — Bourse Android Client

## 1. Authentication Flow
- **Login / Registration**: Users enter credentials. Successful responses return a JWT bearer token.
- **Token Persistence**: The JWT token is securely saved via `TokenManager`.
- **Request Authorization**: `AuthInterceptor` intercepts all outgoing OkHttp requests and attaches the `Authorization: Bearer <token>` header.
- **Session Expiration (`401 Unauthorized`)**: If any API response returns status code `401`, `TokenManager.clearToken()` is invoked, and the user is redirected to `LoginActivity` with the activity stack cleared.
- **Explicit Logout**: Tapping "Log Out" in `ProfileActivity` invokes `TokenManager.clearToken()` and redirects to `LoginActivity`.

## 2. Transaction PIN Security
- **Independent 4-Digit PIN**: Separate from the login password.
- **Encryption at Rest**: Encrypted using AndroidKeyStore (AES/GCM) and stored locally via `TransactionPinStore`.
- **Client-Side Verification**: **VERIFIED FROM ANDROID SOURCE**: Transaction PIN is a local client-side confirmation mechanism and is separate from backend authentication. It is never sent to the backend. Verified locally in `StockDetailsActivity` before invoking trade repository/API calls.
- **First-Time Onboarding**: Required immediately after successful login or registration if no PIN has been set up yet.
