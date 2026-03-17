# rocky-coin-ex

Simple cryptocurrency exchange system example based on **Java + Spring Boot + Vue 3**, including user-facing and admin-system features.

## Features Overview

### User Side
- User registration / login
- TradingView charts + order book display
- Spot and contract trading (place/cancel orders, open/close positions)
- View trade history, orders, and positions
- Spot account, contract account, funding account
- Transfers between accounts

### Admin System
- Configure spot and contract trading pairs
- View user trade history
- Airdrop to user funding accounts
- User management and balance viewing

## Local Development

### Backend (Spring Boot)
```bash
cd backend
mvn spring-boot:run
```

Default admin account:
- Username: `admin`
- Password: `admin123`

Newly registered users receive 10,000 USDT in the funding account by default, making it easy to try transfers and trading flows.

### Frontend (Vue 3 + Vite)
```bash
cd frontend
npm install
npm run dev
```

The frontend points to `http://localhost:8080` by default, and you can change the API endpoint at the top of the page.
