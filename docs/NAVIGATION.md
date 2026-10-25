# Navigation — Bourse Android Client

## 1. Navigation Architecture
**VERIFIED FROM ANDROID SOURCE**: The application uses a **Multi-Activity architecture** where each major screen or feature set is represented by a dedicated `AppCompatActivity`, connected via explicit Intents with appropriate flag configurations (e.g., `FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK` for clearing auth backstacks).

## 2. Navigation Flow Diagram
```mermaid
flowchart TD
    Splash[SplashActivity] -->|Token Exists| Main[MainActivity / Home]
    Splash -->|No Token| Login[LoginActivity]
    Login -->|Sign Up| Signup[SignupActivity]
    Signup -->|Register & Set PIN| Main
    Login -->|Login & Set PIN (if needed)| Main
    
    Main --> Market[MarketActivity]
    Main --> Portfolio[PortfolioActivity]
    Main --> Profile[ProfileActivity]
    Main --> Transactions[TransactionsActivity]
    
    Market --> StockDetails[StockDetailsActivity]
    Portfolio --> StockDetails
    Portfolio --> Transactions
    Profile --> AllHoldings[AllHoldingsActivity]
    AllHoldings --> StockDetails
    
    StockDetails -->|Buy/Sell Success| StockDetails
    Profile -->|Log Out| Login
```
