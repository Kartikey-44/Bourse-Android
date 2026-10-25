# Development Guide — Bourse Android Client

## 1. Requirements & Setup
- **IDE**: Android Studio (Compatible with AGP 9.4.1 / Gradle 8.x).
- **JDK**: Java 11 / Java 17.
- **Gradle**: Gradle Wrapper configured in project.
- **SDK**: compileSdk 37, minSdk 26, targetSdk 37 (VERIFIED FROM `build.gradle.kts`).

## 2. Setup Instructions
1. Clone the repository.
2. Open the project folder in Android Studio.
3. Allow Gradle to sync dependencies (`libs.versions.toml`).
4. Ensure an Android emulator (API 26+) or physical device is connected.
5. Click **Run 'app'** to build and deploy.

## 3. Backend Connection
The app connects to the remote Spring Boot backend via `ApiClient`:
- **Base URL**: `http://bourse-alb-546387840.ap-south-1.elb.amazonaws.com/`
- **Cleartext Traffic**: Enabled in `AndroidManifest.xml` (`android:usesCleartextTraffic="true"`).
- **Network Security**: Ensure the device/emulator has active internet access to reach the AWS Elastic Load Balancer endpoint.
