<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'

const apiBase = ref('http://localhost:8080')
const auth = reactive({
  token: localStorage.getItem('token') || '',
  username: localStorage.getItem('username') || '',
  admin: localStorage.getItem('admin') === 'true',
  userId: Number(localStorage.getItem('userId') || 0)
})
const activeView = ref(auth.admin ? 'admin' : 'market')
const message = ref('')

const loginForm = reactive({ username: '', password: '' })
const registerForm = reactive({ username: '', password: '' })
const transferForm = reactive({
  fromAccount: 'FUNDING',
  toAccount: 'SPOT',
  currency: 'USDT',
  amount: ''
})
const spotOrderForm = reactive({
  symbol: '',
  side: 'BUY',
  price: '',
  quantity: '',
  executeImmediately: true
})
const contractOrderForm = reactive({
  symbol: '',
  side: 'LONG',
  price: '',
  quantity: '',
  margin: ''
})
const adminSymbolForm = reactive({
  symbol: '',
  baseCurrency: '',
  quoteCurrency: '',
  spotEnabled: true,
  contractEnabled: true
})

const symbols = ref([])
const spotSymbols = computed(() => symbols.value.filter((symbol) => symbol.spotEnabled))
const contractSymbols = computed(() => symbols.value.filter((symbol) => symbol.contractEnabled))
const selectedSymbol = ref('')
const orderBook = reactive({ bids: [], asks: [] })
const balances = ref({})
const orders = ref([])
const positions = ref([])
const trades = ref([])
const adminSymbols = ref([])

const chartUrl = computed(() => {
  if (!selectedSymbol.value) return ''
  const cleanSymbol = selectedSymbol.value.replace('/', '')
  return `https://s.tradingview.com/widgetembed/?symbol=BINANCE:${cleanSymbol}&interval=1&hidesidetoolbar=1&theme=dark`
})

const formatAmount = (value) => {
  if (value === null || value === undefined) return '-'
  return Number(value).toFixed(4)
}

const request = async (path, options = {}) => {
  const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) }
  if (auth.token) {
    headers.Authorization = `Bearer ${auth.token}`
  }
  const response = await fetch(`${apiBase.value}${path}`, { ...options, headers })
  if (!response.ok) {
    const data = await response.json().catch(() => ({}))
    throw new Error(data.message || '请求失败')
  }
  return response.json()
}

const storeAuth = (data) => {
  auth.token = data.token
  auth.username = data.username
  auth.admin = data.admin
  auth.userId = data.userId
  localStorage.setItem('token', data.token)
  localStorage.setItem('username', data.username)
  localStorage.setItem('admin', String(data.admin))
  localStorage.setItem('userId', String(data.userId))
}

const clearAuth = () => {
  auth.token = ''
  auth.username = ''
  auth.admin = false
  auth.userId = 0
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('admin')
  localStorage.removeItem('userId')
}

const handleError = (error) => {
  message.value = error.message || String(error)
}

const login = async () => {
  try {
    const data = await request('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify(loginForm)
    })
    storeAuth(data)
    activeView.value = data.admin ? 'admin' : 'market'
    message.value = `欢迎回来，${data.username}`
    await loadAllData()
  } catch (error) {
    handleError(error)
  }
}

const register = async () => {
  try {
    const data = await request('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify(registerForm)
    })
    storeAuth(data)
    activeView.value = data.admin ? 'admin' : 'market'
    message.value = `注册成功，欢迎 ${data.username}`
    await loadAllData()
  } catch (error) {
    handleError(error)
  }
}

const logout = () => {
  clearAuth()
  activeView.value = 'market'
  message.value = '已退出登录'
}

const selectSymbol = (symbol) => {
  selectedSymbol.value = symbol
}

const syncSymbolSelections = () => {
  const allSymbols = symbols.value.map((symbol) => symbol.symbol)
  if (!allSymbols.includes(selectedSymbol.value)) {
    selectedSymbol.value = allSymbols[0] || ''
  }
  if (!spotSymbols.value.some((symbol) => symbol.symbol === spotOrderForm.symbol)) {
    spotOrderForm.symbol = spotSymbols.value[0]?.symbol || ''
  }
  if (!contractSymbols.value.some((symbol) => symbol.symbol === contractOrderForm.symbol)) {
    contractOrderForm.symbol = contractSymbols.value[0]?.symbol || ''
  }
}

const loadSymbols = async () => {
  symbols.value = await request('/api/trading/symbols')
  syncSymbolSelections()
}

const loadOrderBook = async () => {
  if (!selectedSymbol.value) return
  orderBook.bids = []
  orderBook.asks = []
  const data = await request(`/api/trading/orderbook?symbol=${encodeURIComponent(selectedSymbol.value)}`)
  orderBook.bids = data.bids || []
  orderBook.asks = data.asks || []
}

const loadBalances = async () => {
  balances.value = await request('/api/accounts/overview')
}

const loadOrders = async () => {
  orders.value = await request('/api/trading/orders')
}

const loadTrades = async () => {
  trades.value = await request('/api/trading/trades')
}

const loadPositions = async () => {
  positions.value = await request('/api/trading/positions')
}

const loadAdminData = async () => {
  if (!auth.admin) return
  adminSymbols.value = await request('/api/admin/symbols')
}

const loadAllData = async () => {
  if (!auth.token) return
  await loadSymbols()
  await Promise.all([loadBalances(), loadOrders(), loadTrades(), loadPositions(), loadAdminData(), loadOrderBook()])
}

const submitTransfer = async () => {
  try {
    await request('/api/accounts/transfer', {
      method: 'POST',
      body: JSON.stringify({
        ...transferForm,
        amount: Number(transferForm.amount)
      })
    })
    message.value = '划转成功'
    transferForm.amount = ''
    await loadBalances()
  } catch (error) {
    handleError(error)
  }
}

const placeSpotOrder = async () => {
  try {
    const payload = {
      symbol: spotOrderForm.symbol,
      side: spotOrderForm.side,
      price: Number(spotOrderForm.price),
      quantity: Number(spotOrderForm.quantity),
      executeImmediately: spotOrderForm.executeImmediately
    }
    await request('/api/trading/spot/orders', {
      method: 'POST',
      body: JSON.stringify(payload)
    })
    message.value = '现货订单已提交'
    spotOrderForm.price = ''
    spotOrderForm.quantity = ''
    await Promise.all([loadOrders(), loadTrades(), loadBalances(), loadOrderBook()])
  } catch (error) {
    handleError(error)
  }
}

const cancelOrder = async (orderId) => {
  try {
    await request(`/api/trading/spot/orders/${orderId}/cancel`, { method: 'POST' })
    message.value = '订单已撤销'
    await Promise.all([loadOrders(), loadBalances(), loadOrderBook()])
  } catch (error) {
    handleError(error)
  }
}

const openContract = async () => {
  try {
    const payload = {
      symbol: contractOrderForm.symbol,
      side: contractOrderForm.side,
      price: Number(contractOrderForm.price),
      quantity: Number(contractOrderForm.quantity),
      margin: Number(contractOrderForm.margin)
    }
    await request('/api/trading/contract/orders', {
      method: 'POST',
      body: JSON.stringify(payload)
    })
    message.value = '合约开仓成功'
    contractOrderForm.price = ''
    contractOrderForm.quantity = ''
    contractOrderForm.margin = ''
    await Promise.all([loadPositions(), loadTrades(), loadBalances()])
  } catch (error) {
    handleError(error)
  }
}

const closePosition = async (positionId) => {
  try {
    await request(`/api/trading/contract/positions/${positionId}/close`, { method: 'POST' })
    message.value = '合约已平仓'
    await Promise.all([loadPositions(), loadBalances()])
  } catch (error) {
    handleError(error)
  }
}

const saveSymbol = async () => {
  try {
    await request('/api/admin/symbols', {
      method: 'POST',
      body: JSON.stringify(adminSymbolForm)
    })
    message.value = '币对配置已保存'
    adminSymbolForm.symbol = ''
    adminSymbolForm.baseCurrency = ''
    adminSymbolForm.quoteCurrency = ''
    await Promise.all([loadSymbols(), loadAdminData()])
  } catch (error) {
    handleError(error)
  }
}

watch(selectedSymbol, async () => {
  await loadOrderBook()
})

onMounted(async () => {
  if (auth.token) {
    await loadAllData()
  }
})
</script>

<template>
  <div class="app">
    <header class="app-header">
      <div class="brand">
        <div>
          <h1>Rocky Coin Exchange</h1>
          <p>行情与资产中心分离，支持现货与合约配置</p>
        </div>
      </div>
      <div class="header-actions">
        <div class="api-config">
          <label>API 地址</label>
          <input v-model="apiBase" />
        </div>
        <nav v-if="auth.token" class="nav-links">
          <button
            :class="{ active: activeView === 'market' }"
            :aria-current="activeView === 'market' ? 'page' : undefined"
            @click="activeView = 'market'"
          >
            行情
          </button>
          <button
            :class="{ active: activeView === 'center' }"
            :aria-current="activeView === 'center' ? 'page' : undefined"
            @click="activeView = 'center'"
          >
            个人中心
          </button>
          <button
            v-if="auth.admin"
            :class="{ active: activeView === 'admin' }"
            :aria-current="activeView === 'admin' ? 'page' : undefined"
            @click="activeView = 'admin'"
          >
            管理后台
          </button>
        </nav>
        <div class="auth-status">
          <div v-if="auth.token" class="user-info">
            <span>当前用户：{{ auth.username }}</span>
            <span v-if="auth.admin" class="badge">管理员</span>
            <button class="ghost" @click="logout">退出</button>
          </div>
        </div>
      </div>
    </header>

    <section class="card" v-if="!auth.token">
      <h2>登录 / 注册</h2>
      <div class="grid two">
        <div>
          <h3>登录</h3>
          <div class="form-row">
            <label>用户名</label>
            <input v-model="loginForm.username" placeholder="输入用户名" />
          </div>
          <div class="form-row">
            <label>密码</label>
            <input v-model="loginForm.password" type="password" placeholder="输入密码" />
          </div>
          <button @click="login">登录</button>
        </div>
        <div>
          <h3>注册</h3>
          <div class="form-row">
            <label>用户名</label>
            <input v-model="registerForm.username" placeholder="设置用户名" />
          </div>
          <div class="form-row">
            <label>密码</label>
            <input v-model="registerForm.password" type="password" placeholder="设置密码" />
          </div>
          <button @click="register">注册</button>
        </div>
      </div>
    </section>

    <section class="card message" v-if="message">
      {{ message }}
    </section>

    <section v-if="auth.token && activeView === 'market'" class="market-layout">
      <div class="card market-list">
        <div class="market-list-header">
          <h2>行情列表</h2>
          <span class="muted">{{ symbols.length }} 个币对</span>
        </div>
        <ul class="symbol-list">
          <li
            v-for="symbol in symbols"
            :key="symbol.symbol"
            :class="{ active: selectedSymbol === symbol.symbol }"
            role="button"
            tabindex="0"
            @click="selectSymbol(symbol.symbol)"
            @keydown.enter="selectSymbol(symbol.symbol)"
            @keydown.space.prevent="selectSymbol(symbol.symbol)"
          >
            <div class="symbol-name">{{ symbol.symbol }}</div>
            <div class="symbol-tags">
              <span v-if="symbol.spotEnabled" class="tag spot">现货</span>
              <span v-if="symbol.contractEnabled" class="tag contract">合约</span>
            </div>
          </li>
          <li v-if="symbols.length === 0" class="empty">暂无币对</li>
        </ul>
      </div>

      <div class="market-main">
        <div class="card market-chart">
          <div class="market-chart-header">
            <div>
              <h2>行情图表</h2>
              <p class="muted">TradingView 实时行情</p>
            </div>
            <div class="form-row compact">
              <label>交易对</label>
              <select v-model="selectedSymbol">
                <option v-for="symbol in symbols" :key="symbol.symbol" :value="symbol.symbol">
                  {{ symbol.symbol }}
                </option>
              </select>
            </div>
          </div>
          <iframe v-if="chartUrl" :src="chartUrl" class="chart-frame" allowfullscreen></iframe>
        </div>

        <div class="grid two market-trade">
          <div class="card">
            <h2>现货交易</h2>
            <div class="form-row">
              <label>币对</label>
              <select v-model="spotOrderForm.symbol">
                <option v-for="symbol in spotSymbols" :key="symbol.symbol" :value="symbol.symbol">
                  {{ symbol.symbol }}
                </option>
              </select>
              <label>方向</label>
              <select v-model="spotOrderForm.side">
                <option>BUY</option>
                <option>SELL</option>
              </select>
            </div>
            <div class="form-row">
              <label>价格</label>
              <input v-model="spotOrderForm.price" placeholder="0.00" />
              <label>数量</label>
              <input v-model="spotOrderForm.quantity" placeholder="0.00" />
            </div>
            <div class="form-row checkbox">
              <input id="spotExecute" type="checkbox" v-model="spotOrderForm.executeImmediately" />
              <label for="spotExecute">立即成交（取消勾选可用于撤单演示）</label>
            </div>
            <button @click="placeSpotOrder">下单</button>
          </div>

          <div class="card">
            <h2>合约交易</h2>
            <div class="form-row">
              <label>币对</label>
              <select v-model="contractOrderForm.symbol">
                <option v-for="symbol in contractSymbols" :key="symbol.symbol" :value="symbol.symbol">
                  {{ symbol.symbol }}
                </option>
              </select>
              <label>方向</label>
              <select v-model="contractOrderForm.side">
                <option>LONG</option>
                <option>SHORT</option>
              </select>
            </div>
            <div class="form-row">
              <label>开仓价</label>
              <input v-model="contractOrderForm.price" placeholder="0.00" />
              <label>数量</label>
              <input v-model="contractOrderForm.quantity" placeholder="0.00" />
            </div>
            <div class="form-row">
              <label>保证金</label>
              <input v-model="contractOrderForm.margin" placeholder="0.00" />
            </div>
            <button @click="openContract">开仓</button>
          </div>
        </div>
      </div>

      <div class="card market-orderbook">
        <h2>盘口（Order Book）</h2>
        <div class="orderbook">
          <div>
            <h3>买单</h3>
            <table>
              <thead>
                <tr><th>价格</th><th>数量</th></tr>
              </thead>
              <tbody>
                <tr v-for="bid in orderBook.bids" :key="bid.id">
                  <td>{{ bid.price }}</td>
                  <td>{{ bid.quantity }}</td>
                </tr>
              </tbody>
            </table>
          </div>
          <div>
            <h3>卖单</h3>
            <table>
              <thead>
                <tr><th>价格</th><th>数量</th></tr>
              </thead>
              <tbody>
                <tr v-for="ask in orderBook.asks" :key="ask.id">
                  <td>{{ ask.price }}</td>
                  <td>{{ ask.quantity }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </section>

    <section v-if="auth.token && activeView === 'center'" class="grid two">
      <div class="card">
        <h2>账户资产</h2>
        <div class="balances">
          <div v-for="(account, type) in balances" :key="type">
            <h3>{{ type }}</h3>
            <ul>
              <li v-for="(amount, currency) in account" :key="currency">
                {{ currency }}: {{ formatAmount(amount) }}
              </li>
              <li v-if="Object.keys(account).length === 0">暂无资产</li>
            </ul>
          </div>
        </div>
        <h3>账户划转</h3>
        <div class="form-row">
          <label>从</label>
          <select v-model="transferForm.fromAccount">
            <option>SPOT</option>
            <option>CONTRACT</option>
            <option>FUNDING</option>
          </select>
          <label>到</label>
          <select v-model="transferForm.toAccount">
            <option>SPOT</option>
            <option>CONTRACT</option>
            <option>FUNDING</option>
          </select>
        </div>
        <div class="form-row">
          <label>币种</label>
          <input v-model="transferForm.currency" placeholder="USDT" />
          <label>金额</label>
          <input v-model="transferForm.amount" placeholder="0.00" />
        </div>
        <button @click="submitTransfer">划转</button>
      </div>

      <div class="card">
        <h2>现货订单</h2>
        <table>
          <thead>
            <tr><th>ID</th><th>币对</th><th>方向</th><th>价格</th><th>数量</th><th>状态</th><th>操作</th></tr>
          </thead>
          <tbody>
            <tr v-for="order in orders" :key="order.id">
              <td>{{ order.id }}</td>
              <td>{{ order.symbol }}</td>
              <td>{{ order.side }}</td>
              <td>{{ order.price }}</td>
              <td>{{ order.quantity }}</td>
              <td>{{ order.status }}</td>
              <td>
                <button v-if="order.status === 'OPEN'" class="ghost" @click="cancelOrder(order.id)">
                  撤单
                </button>
                <span v-else>-</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="card">
        <h2>当前持仓</h2>
        <table>
          <thead>
            <tr><th>ID</th><th>币对</th><th>方向</th><th>数量</th><th>保证金</th><th>状态</th><th>操作</th></tr>
          </thead>
          <tbody>
            <tr v-for="position in positions" :key="position.id">
              <td>{{ position.id }}</td>
              <td>{{ position.symbol }}</td>
              <td>{{ position.side }}</td>
              <td>{{ position.quantity }}</td>
              <td>{{ position.margin }}</td>
              <td>{{ position.closedAt ? '已平仓' : '持仓中' }}</td>
              <td>
                <button v-if="!position.closedAt" class="ghost" @click="closePosition(position.id)">
                  平仓
                </button>
                <span v-else>-</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="card">
        <h2>交易记录</h2>
        <table>
          <thead>
            <tr><th>ID</th><th>类型</th><th>币对</th><th>方向</th><th>价格</th><th>数量</th><th>成交额</th></tr>
          </thead>
          <tbody>
            <tr v-for="trade in trades" :key="trade.id">
              <td>{{ trade.id }}</td>
              <td>{{ trade.category }}</td>
              <td>{{ trade.symbol }}</td>
              <td>{{ trade.side }}</td>
              <td>{{ trade.price }}</td>
              <td>{{ trade.quantity }}</td>
              <td>{{ trade.notional }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <section v-if="auth.token && auth.admin && activeView === 'admin'" class="admin-layout">
      <div class="card">
        <h2>币种币对配置</h2>
        <div class="form-row">
          <label>币对</label>
          <input v-model="adminSymbolForm.symbol" placeholder="BTC/USDT" />
        </div>
        <div class="form-row">
          <label>基础币</label>
          <input v-model="adminSymbolForm.baseCurrency" placeholder="BTC" />
          <label>计价币</label>
          <input v-model="adminSymbolForm.quoteCurrency" placeholder="USDT" />
        </div>
        <div class="form-row checkbox">
          <input id="spotEnabled" type="checkbox" v-model="adminSymbolForm.spotEnabled" />
          <label for="spotEnabled">现货启用</label>
        </div>
        <div class="form-row checkbox">
          <input id="contractEnabled" type="checkbox" v-model="adminSymbolForm.contractEnabled" />
          <label for="contractEnabled">合约启用</label>
        </div>
        <button @click="saveSymbol">保存币对</button>
        <h3>当前配置</h3>
        <ul class="symbol-config-list">
          <li v-for="symbol in adminSymbols" :key="symbol.symbol">
            <span>{{ symbol.symbol }}</span>
            <span class="muted">
              现货 {{ symbol.spotEnabled ? '开启' : '关闭' }} / 合约
              {{ symbol.contractEnabled ? '开启' : '关闭' }}
            </span>
          </li>
        </ul>
      </div>
    </section>
  </div>
</template>
