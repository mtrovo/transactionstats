package mtrovo;

import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

import static j8spec.J8Spec.*;
import static org.hamcrest.CoreMatchers.*;

interface TestHelper {
    long WINDOW_TS = 60_000;
    default BigDecimal $(String num) {
        return new BigDecimal(num);
    }
    default void assertStats(Stats stats, BigDecimal avg, long count, BigDecimal max, BigDecimal min, BigDecimal sum) {
        assertThat(stats.getAvg(), is(equalTo(avg)));
        assertThat(stats.getCount(), is(equalTo(count)));
        assertThat(stats.getMax(), is(equalTo(max)));
        assertThat(stats.getMin(), is(equalTo(min)));
        assertThat(stats.getSum(), is(equalTo(sum)));
    }
}

@RunWith(J8SpecRunner.class) public class TransactionServiceTest implements TestHelper {{
    describe("TransactionService", () -> {
        it("addTransaction a new transaction to the store", () -> {
            final FixedTimestampProvider timestampProvider = new FixedTimestampProvider();
            TransactionService service = new TransactionService(timestampProvider, WINDOW_TS);
            service.addTransaction(new Transaction(new BigDecimal("100"), 0L));
            assertStats(service.getStats(), $("100.00"), 1, $("100.00"), $("100.00"), $("100.00"));
        });
        it("calculate stats correctly", () -> {
            final FixedTimestampProvider timestampProvider = new FixedTimestampProvider();
            TransactionService service = new TransactionService(timestampProvider, WINDOW_TS);
            service.addTransaction(new Transaction(new BigDecimal("100"), 0L));
            service.addTransaction(new Transaction(new BigDecimal("100"), 0L));
            service.addTransaction(new Transaction(new BigDecimal("100"), 0L));
            assertStats(service.getStats(), $("100.00"), 3, $("100.00"), $("100.00"), $("300.00"));
        });
        it("calculate stats correctly on thread-safe manner", () -> {
            final FixedTimestampProvider timestampProvider = new FixedTimestampProvider();
            TransactionService service = new TransactionService(timestampProvider, 500L);
            final List<Transaction> transactions = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                transactions.add(new Transaction(new BigDecimal("100"), 0L));
            }
            transactions.parallelStream().forEach(service::addTransaction);
            assertStats(service.getStats(), $("100.00"), 1000, $("100.00"), $("100.00"), $("100000.00"));

            Thread.sleep(250L);
            service.addTransaction(new Transaction(new BigDecimal("300"), 0L));

            Thread.sleep(251L);
            assertStats(service.getStats(), $("300.00"), 1, $("300.00"), $("300.00"), $("300.00"));

            Thread.sleep(501L);
            assertStats(service.getStats(), $("0"), 0, $("0"), $("0"), $("0.00"));
        });
        it("old transactions should be removed on the right moment", () -> {
            final FixedTimestampProvider timestampProvider = new FixedTimestampProvider();
            timestampProvider.now = 120_000L;
            TransactionService service = new TransactionService(timestampProvider, timestampProvider.now);

            service.addTransaction(new Transaction(new BigDecimal("100"), 250L));
            assertStats(service.getStats(), $("100.00"), 1, $("100.00"), $("100.00"), $("100.00"));

            Thread.sleep(300L);
            assertStats(service.getStats(), $("0"), 0, $("0"), $("0"), $("0.00"));

        });
        it("ignore old transactions", () -> {
            final FixedTimestampProvider timestampProvider = new FixedTimestampProvider();
            timestampProvider.now = System.currentTimeMillis() + WINDOW_TS;
            TransactionService service = new TransactionService(timestampProvider, WINDOW_TS);
            service.addTransaction(new Transaction(new BigDecimal("100"), 0L));
            assertStats(service.getStats(), $("0"), 0, $("0"), $("0"), $("0"));

            service.addTransaction(new Transaction(new BigDecimal("100"), 0L));
            service.addTransaction(new Transaction(new BigDecimal("100"), timestampProvider.now));
            assertStats(service.getStats(), $("100.00"), 1, $("100.00"), $("100.00"), $("100.00"));
        });
        it("remove old transactions from stats", () -> {
            TransactionService service = new TransactionService(new SystemTimestampProvider(), 500L);
            service.addTransaction(new Transaction(new BigDecimal("100"), 0L));
            service.addTransaction(new Transaction(new BigDecimal("200"), System.currentTimeMillis()));
            assertStats(service.getStats(), $("200.00"), 1, $("200.00"), $("200.00"), $("200.00"));

            Thread.sleep(1000L);
            assertStats(service.getStats(), $("0"), 0, $("0"), $("0"), $("0.00"));
        });
    });
}}