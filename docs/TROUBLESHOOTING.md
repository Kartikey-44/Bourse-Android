# Troubleshooting — Bourse Android Client

## 1. Common Issues & Solutions

### Gradle Sync Failure
- **Cause**: Network issues or incompatible Android Studio / AGP version.
- **Solution**: Check internet connection, invalidate caches / restart Android Studio (`File > Invalidate Caches`), and re-sync Gradle.

### API Connection Failure / Network Timeout
- **Cause**: Device offline or AWS backend unreachable.
- **Solution**: Verify device internet connection and check if `android:usesCleartextTraffic="true"` is present in `AndroidManifest.xml`.

### Transaction PIN Keystore Exception / Crash
- **Cause**: Corrupted Keystore state or invalid IV parameters.
- **Solution**: `TransactionPinStore` includes fallback recovery handling (`clearPin()`) to safely reset corrupted encrypted preferences. Clear app data if necessary during local testing.

### 401 Unauthorized Loop
- **Cause**: Expired or invalid JWT token.
- **Solution**: `AuthInterceptor` and repositories automatically clear expired tokens and redirect to `LoginActivity`. If stuck, clear app data and sign in again.
