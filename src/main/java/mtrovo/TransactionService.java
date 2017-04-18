package mtrovo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class TransactionService {

    private final StatsManager stats;
    private final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    public TransactionService(TimestampProvider timestampProvider, @Value("${stats.window_ts}") Long windowTs) {
        logger.info("starting transaction service stats with window (ms): {}", windowTs);
        this.stats = new StatsManager(windowTs, timestampProvider);
    }

    public void addTransaction(Transaction t) {
        // add to real storage here
        notifyAddingTransaction(t);
    }

    private void notifyAddingTransaction(Transaction t) {
        stats.addTransaction(t);
    }

    public Stats getStats() {
        return stats.getStats();
    }
}

class Stats {
    private final BigDecimal sum;
    private final long count;
    private final BigDecimal max;
    private final BigDecimal min;
    private final BigDecimal avg;

    public Stats(BigDecimal sum, long count, BigDecimal max, BigDecimal min, BigDecimal avg) {
        this.sum = sum;
        this.count = count;
        this.max = max;
        this.min = min;
        this.avg = avg;
    }

    public BigDecimal getSum() {
        return sum;
    }

    public long getCount() {
        return count;
    }

    public BigDecimal getMax() {
        return max;
    }

    public BigDecimal getMin() {
        return min;
    }

    public BigDecimal getAvg() {
        return avg;
    }
}

class StatsManager {
    private final long windowTs;
    private final TimestampProvider timestampProvider;
    private final PriorityQueue<Transaction> store = new PriorityQueue<>(Comparator.comparing(Transaction::getTimestamp));
    private final ScheduledExecutorService cleaner = Executors.newScheduledThreadPool(1);

    private final Lock transactionLock = new ReentrantLock();
    private BigDecimal sum = new BigDecimal(0.0);
    private AtomicLong count = new AtomicLong();
    private PriorityQueue<BigDecimal> max = new PriorityQueue<>(Comparator.<BigDecimal>reverseOrder());
    private PriorityQueue<BigDecimal> min = new PriorityQueue<>(Comparator.naturalOrder());

    private Stats currentStats = new Stats(BigDecimal.ZERO, 0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

    public StatsManager(long windowTs, TimestampProvider timestampProvider) {
        this.windowTs = windowTs;
        this.timestampProvider = timestampProvider;
    }

    public BigDecimal getSum() {
        return sum;
    }

    public Long getCount() {
        return count.get();
    }

    public BigDecimal getAvg() {
        if(sum.equals(BigDecimal.ZERO) || count.get() == 0L) {
            return BigDecimal.ZERO;
        } else {
            return sum.divide(BigDecimal.valueOf(count.get()), BigDecimal.ROUND_HALF_DOWN);
        }
    }

    public BigDecimal getMax() {
        if(max.isEmpty()) {
            return BigDecimal.ZERO;
        } else {
            return max.peek();
        }
    }

    public BigDecimal getMin() {
        if(min.isEmpty()) {
            return BigDecimal.ZERO;
        } else {
            return min.peek();
        }
    }

    public void addTransaction(final Transaction t) {
        long now = timestampProvider.now();
        if(t.getTimestamp() < now - windowTs) {
            // item is too old
            return;
        }

        transactionLock.lock();
        try {
            this.store.add(t);
            updateStatsInsertingTransaction(t);
            currentStats = new Stats(getSum(), getCount(), getMax(), getMin(), getAvg());
            long delay = t.getTimestamp() - now + windowTs;
            cleaner.schedule(() -> removeTransaction(t), delay, TimeUnit.MILLISECONDS);
        } finally {
            transactionLock.unlock();
        }
    }

    public void removeTransaction(final Transaction t) {
        transactionLock.lock();
        try {
            this.store.remove(t);
            this.updateStatsRemovingTransaction(t);
            this.currentStats = new Stats(getSum(), getCount(), getMax(), getMin(), getAvg());
        } finally {
            transactionLock.unlock();
        }
    }

    private void updateStatsInsertingTransaction(Transaction t) {
        BigDecimal cur = t.getAmount();
        sum = sum.add(cur);
        count.incrementAndGet();
        max.offer(cur);
        min.offer(cur);
    }

    private void updateStatsRemovingTransaction(Transaction t) {
        BigDecimal cur = t.getAmount();
        sum = sum.subtract(cur);
        count.decrementAndGet();
        max.remove(cur);
        min.remove(cur);
    }

    public Stats getStats() {
        return currentStats;
    }
}

