# Architecture — Bourse Android Client

## 1. Architectural Pattern
**VERIFIED FROM ANDROID SOURCE**: The application strictly implements the **Model-View-ViewModel (MVVM)** architectural pattern combined with the **Repository Pattern** and ViewBinding. 

### Layers
- **UI Layer (`Activity` classes + XML layouts)**: Responsible for rendering UI elements, observing ViewModel LiveData state changes, capturing user interaction events, and handling view bindings without containing business logic.
- **Presentation Layer (`ViewModel` classes)**: Subclasses of `AndroidViewModel` (`AuthViewModel`, `HomeViewModel`, `MarketViewModel`, `PortfolioViewModel`, `ProfileViewModel`, `StockDetailsViewModel`, `TradingViewModel`, `TransactionsViewModel`, `TransactionPinViewModel`) that manage UI-related state, execute asynchronous repository calls via `viewModelScope`, and expose immutable `LiveData` streams to the UI.
- **Data Layer (`Repository` classes)**: `UserRepository`, `PortfolioRepository`, `StockRepository`, and `TradingRepository` encapsulate data sources (both remote Retrofit APIs and local storage like `TokenManager` and `TransactionPinStore`), exposing clean `Result<T>` wrappers to ViewModels.

## 2. Dependency Management
- Services and repositories are instantiated manually inside ViewModels and Activities using factory methods or constructors provided by `ApiClient` and `TokenManager`. There is no automated Dependency Injection framework (such as Hilt or Dagger) present in the repository.

## 3. Threading & Concurrency
- Network and disk operations are dispatched onto `Dispatchers.IO` inside repositories using `withContext(Dispatchers.IO)`.
- UI updates occur strictly on the Main thread via LiveData observation in Activities.
