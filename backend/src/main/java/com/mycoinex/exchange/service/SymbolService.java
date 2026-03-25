package com.mycoinex.exchange.service;

import com.mycoinex.exchange.model.SymbolPair;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class SymbolService {
    private final Map<String, SymbolPair> symbols = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(2000);

    @PostConstruct
    public void initializeDefaults() {
        createOrUpdate("BTC/USDT", "BTC", "USDT", true, false);
        createOrUpdate("BTCUSDT", "BTC", "USDT", false, true);
        createOrUpdate("ETH/USDT", "ETH", "USDT", true, true);
    }

    public List<SymbolPair> listAll() {
        return new ArrayList<>(symbols.values());
    }

    public SymbolPair getBySymbol(String symbol) {
        return symbols.get(symbol);
    }

    public SymbolPair createOrUpdate(String symbol,
                                     String baseCurrency,
                                     String quoteCurrency,
                                     boolean spotEnabled,
                                     boolean contractEnabled) {
        SymbolPair existing = symbols.get(symbol);
        if (existing != null) {
            existing.setSpotEnabled(spotEnabled);
            existing.setContractEnabled(contractEnabled);
            return existing;
        }
        SymbolPair pair = new SymbolPair(idGenerator.incrementAndGet(),
                symbol,
                baseCurrency,
                quoteCurrency,
                spotEnabled,
                contractEnabled,
                Instant.now());
        symbols.put(symbol, pair);
        return pair;
    }
}
