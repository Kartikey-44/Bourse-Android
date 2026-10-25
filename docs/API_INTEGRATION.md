# API Integration — Bourse Android Client

> **NOT DETERMINABLE WITHOUT BACKEND SOURCE**: This API documentation represents only the endpoints currently referenced by the Android application. The backend implementation is maintained separately and is not available in this repository.

## 1. Network Architecture
- **Client**: Retrofit 2.9.0 with Gson 2.10.1 converter.
- **Transport**: OkHttp 4.11.0 with `HttpLoggingInterceptor` and `AuthInterceptor`.
- **Base URL**: `http://bourse-alb-546387840.ap-south-1.elb.amazonaws.com/` (configured with cleartext traffic permitted).
- **Authentication**: Automatically attaches `Authorization: Bearer <JWT_TOKEN>` header to requests via `AuthInterceptor`.

## 2. API Endpoints Inventory
**19 Retrofit API method definitions, representing 14 logical API routes. Five methods are alternative fallback routes for stock-history retrieval.**

| Method | Endpoint | Request | Response | Authentication | Used By / Service |
|---|---|---|---|---|---|
| `POST` | `/api/users/login` | `LoginRequest` | `LoginResponse` | None | `UserServiceApi` (`UserRepository`) |
| `POST` | `/api/users/register` | `RegisterRequest` | `RegisterResponse` | None | `UserServiceApi` (`UserRepository`) |
| `GET` | `/api/users/profile` | None | `UserDto` | Bearer Token | `UserServiceApi` (`UserRepository`) |
| `GET` | `/api/stocks` | None | `List<StockResponse>` | Bearer Token | `StockServiceApi` (`StockRepository`) |
| `GET` | `/api/stocks/{symbol}` | None | `StockResponse` | Bearer Token | `StockServiceApi` (`StockRepository`) |
| `GET` | `/market/trend/{symbol}` | None | `StockTrendResponse` | Bearer Token | `StockServiceApi` (`StockRepository`) |
| `GET` | `/api/stocks/{symbol}/history` | Query `limit` | `List<PriceHistoryPoint>` | Bearer Token | `StockServiceApi` (`StockRepository`) |
| `GET` | `/market/history/{symbol}` | Query `limit` | `List<PriceHistoryPoint>` | Bearer Token | `StockServiceApi` (History Fallback) |
| `GET` | `/api/market/history/{symbol}` | Query `limit` | `List<PriceHistoryPoint>` | Bearer Token | `StockServiceApi` (History Fallback) |
| `GET` | `/market/stocks/{symbol}/history` | Query `limit` | `List<PriceHistoryPoint>` | Bearer Token | `StockServiceApi` (History Fallback) |
| `GET` | `/api/stocks/history/{symbol}` | Query `limit` | `List<PriceHistoryPoint>` | Bearer Token | `StockServiceApi` (History Fallback) |
| `GET` | `/api/portfolio` | None | `PortfolioResponse` | Bearer Token | `PortfolioServiceApi` (`PortfolioRepository`) |
| `GET` | `/api/portfolio/holdings` | None | `List<HoldingResponse>` | Bearer Token | `PortfolioServiceApi` (`PortfolioRepository`) |
| `GET` | `/api/portfolio/recommendations` | None | `List<RecommendationResponse>` | Bearer Token | `PortfolioServiceApi` (`PortfolioRepository`) |
| `GET` | `/api/portfolio/summary` | None | `PortfolioSummaryResponse` | Bearer Token | `PortfolioServiceApi` (`PortfolioRepository`) |
| `GET` | `/api/portfolio/transactions` | None | `List<PortfolioTransactionDto>` | Bearer Token | `PortfolioServiceApi` (`PortfolioRepository`) |
| `POST` | `/api/trading/buy` | `TradeRequest` | `TransactionDto` | Bearer Token | `TradingServiceApi` (`TradingRepository`) |
| `POST` | `/api/trading/sell` | `TradeRequest` | `TransactionDto` | Bearer Token | `TradingServiceApi` (`TradingRepository`) |
| `GET` | `/api/trading/history` | None | `List<TransactionDto>` | Bearer Token | `TradingServiceApi` (`TradingRepository`) |
