package mtrovo;

import j8spec.Var;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;

import java.math.BigDecimal;

import static j8spec.J8Spec.*;
import static j8spec.Var.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
@RunWith(J8SpecRunner.class) public class StatsTest {{
    describe("stats", () -> {
        Var<StatsManager> statsManager = var();
        beforeEach(() -> {
                var(statsManager, new StatsManager(60 * 1000, new FixedTimestampProvider()));
        });

        it("update stats when adding values", () -> {
            var(statsManager).addTransaction(new Transaction(BigDecimal.valueOf(100.0), 100L));
            Stats stats = var(statsManager).getStats();
            assertThat(stats.getCount(), is(equalTo(1L)));
            assertThat(stats.getSum(), is(equalTo(new BigDecimal("100.00"))));
            assertThat(stats.getAvg(), is(equalTo(new BigDecimal("100.00"))));
            assertThat(stats.getMax(), is(equalTo(new BigDecimal("100.00"))));
            assertThat(stats.getMin(), is(equalTo(new BigDecimal("100.00"))));

            var(statsManager).addTransaction(new Transaction(BigDecimal.valueOf(201.5), 100L));
            stats = var(statsManager).getStats();
            assertThat(stats.getCount(), is(equalTo(2L)));
            assertThat(stats.getSum(), is(equalTo(new BigDecimal("301.50"))));
            assertThat(stats.getAvg(), is(equalTo(new BigDecimal("150.75"))));
            assertThat(stats.getMax(), is(equalTo(new BigDecimal("201.50"))));
            assertThat(stats.getMin(), is(equalTo(new BigDecimal("100.00"))));
        });

        it("update stats when adding repeated values", () -> {
            var(statsManager).addTransaction(new Transaction(BigDecimal.valueOf(99.33), 100L));
            var(statsManager).addTransaction(new Transaction(BigDecimal.valueOf(99.33), 100L));
            var(statsManager).addTransaction(new Transaction(BigDecimal.valueOf(99.33), 100L));
            var(statsManager).addTransaction(new Transaction(BigDecimal.valueOf(99.33), 100L));
            var(statsManager).addTransaction(new Transaction(BigDecimal.valueOf(99.33), 100L));
            Stats stats = var(statsManager).getStats();
            assertThat(stats.getCount(), is(equalTo(5L)));
            assertThat(stats.getSum(), is(equalTo(new BigDecimal("496.65"))));
            assertThat(stats.getAvg(), is(equalTo(new BigDecimal("99.33"))));
            assertThat(stats.getMax(), is(equalTo(new BigDecimal("99.33"))));
            assertThat(stats.getMin(), is(equalTo(new BigDecimal("99.33"))));
        });

        it("update stats when deleting values", () -> {
            var(statsManager).addTransaction(new Transaction(BigDecimal.valueOf(100.0), 100L));
            var(statsManager).addTransaction(new Transaction(BigDecimal.valueOf(201.5), 100L));
            var(statsManager).removeTransaction(new Transaction(BigDecimal.valueOf(100.0), 100L));
            Stats stats = var(statsManager).getStats();
            assertThat(stats.getCount(), is(equalTo(1L)));
            assertThat(stats.getSum(), is(equalTo(new BigDecimal("201.50"))));
            assertThat(stats.getAvg(), is(equalTo(new BigDecimal("201.50"))));
            assertThat(stats.getMax(), is(equalTo(new BigDecimal("201.50"))));
            assertThat(stats.getMin(), is(equalTo(new BigDecimal("201.50"))));
        });

        it("has a valid state while deleting all stats", () -> {
            var(statsManager).addTransaction(new Transaction(BigDecimal.valueOf(100.0), 100L));
            var(statsManager).addTransaction(new Transaction(BigDecimal.valueOf(201.5), 100L));
            var(statsManager).removeTransaction(new Transaction(BigDecimal.valueOf(100.0), 100L));
            var(statsManager).removeTransaction(new Transaction(BigDecimal.valueOf(201.5), 100L));
            Stats stats = var(statsManager).getStats();
            assertThat(stats.getCount(), is(equalTo(0L)));
            assertThat(stats.getSum(), is(equalTo(new BigDecimal("0.00"))));
            assertThat(stats.getAvg(), is(equalTo(new BigDecimal("0"))));
            assertThat(stats.getMax(), is(equalTo(new BigDecimal("0"))));
            assertThat(stats.getMin(), is(equalTo(new BigDecimal("0"))));
        });

        it("update max when old value is removed", () -> {
            var(statsManager).addTransaction(new Transaction(BigDecimal.valueOf(100.0), 100L));
            var(statsManager).addTransaction(new Transaction(BigDecimal.valueOf(90.0), 100L));
            var(statsManager).addTransaction(new Transaction(BigDecimal.valueOf(90.0), 100L));
            var(statsManager).addTransaction(new Transaction(BigDecimal.valueOf(80.0), 100L));
            var(statsManager).addTransaction(new Transaction(BigDecimal.valueOf(70.0), 100L));
            Stats stats = var(statsManager).getStats();
            assertThat(stats.getMax(), is(equalTo(new BigDecimal("100.00"))));

            var(statsManager).removeTransaction(new Transaction(BigDecimal.valueOf(100.0), 100L));
            stats = var(statsManager).getStats();
            assertThat(stats.getMax(), is(equalTo(new BigDecimal("90.00"))));

            var(statsManager).removeTransaction(new Transaction(BigDecimal.valueOf(90.0), 100L));
            stats = var(statsManager).getStats();
            assertThat(stats.getMax(), is(equalTo(new BigDecimal("90.00"))));

            var(statsManager).removeTransaction(new Transaction(BigDecimal.valueOf(90.0), 100L));
            stats = var(statsManager).getStats();
            assertThat(stats.getMax(), is(equalTo(new BigDecimal("80.00"))));

            var(statsManager).addTransaction(new Transaction(BigDecimal.valueOf(110.0), 100L));
            stats = var(statsManager).getStats();
            assertThat(stats.getMax(), is(equalTo(new BigDecimal("110.00"))));
        });

        it("update min when old value is removed", () -> {
            var(statsManager).addTransaction(new Transaction(BigDecimal.valueOf(100.0), 100L));
            var(statsManager).addTransaction(new Transaction(BigDecimal.valueOf(90.0), 100L));
            var(statsManager).addTransaction(new Transaction(BigDecimal.valueOf(80.0), 100L));
            var(statsManager).addTransaction(new Transaction(BigDecimal.valueOf(80.0), 100L));
            var(statsManager).addTransaction(new Transaction(BigDecimal.valueOf(70.0), 100L));
            Stats stats = var(statsManager).getStats();
            assertThat(stats.getMin(), is(equalTo(new BigDecimal("70.00"))));

            var(statsManager).removeTransaction(new Transaction(BigDecimal.valueOf(70.0), 100L));
            stats = var(statsManager).getStats();
            assertThat(stats.getMin(), is(equalTo(new BigDecimal("80.00"))));

            var(statsManager).removeTransaction(new Transaction(BigDecimal.valueOf(80.0), 100L));
            stats = var(statsManager).getStats();
            assertThat(stats.getMin(), is(equalTo(new BigDecimal("80.00"))));

            var(statsManager).removeTransaction(new Transaction(BigDecimal.valueOf(80.0), 100L));
            stats = var(statsManager).getStats();
            assertThat(stats.getMin(), is(equalTo(new BigDecimal("90.00"))));

            var(statsManager).addTransaction(new Transaction(BigDecimal.valueOf(10.0), 100L));
            stats = var(statsManager).getStats();
            assertThat(stats.getMin(), is(equalTo(new BigDecimal("10.00"))));
        });
    });
}}