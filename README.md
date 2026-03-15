# my-coin-ex

基于 **Java + Spring Boot + Vue 3** 的简易加密货币交易所系统示例，包含用户端与管理系统端功能。

## 功能概览

### 用户端
- 用户注册 / 登录
- TradingView 行情 + 盘口展示
- 现货与合约交易（下单、撤单、开仓、平仓）
- 查看交易记录、订单、持仓
- 现货账户、合约账户、资金账户
- 账户间资金划转

### 管理系统端
- 现货与合约币对配置
- 查看用户交易记录
- 空投至用户资金账户
- 用户管理与账户余额查看

## 本地运行

### 后端（Spring Boot）
```bash
cd backend
mvn spring-boot:run
```

默认管理员账号：
- 用户名：`admin`
- 密码：`admin123`

新注册用户默认在资金账户获得 10,000 USDT，方便体验划转与交易流程。

### 前端（Vue 3 + Vite）
```bash
cd frontend
npm install
npm run dev
```

前端默认访问后端 `http://localhost:8080`，可以在页面顶部修改 API 地址。
