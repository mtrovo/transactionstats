package mtrovo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class Transaction {
    @NotNull
    private BigDecimal amount;

    @NotNull
    private Long timestamp;

    public Transaction(BigDecimal amount, Long timestamp) {
        this.amount = amount.setScale(2, RoundingMode.HALF_DOWN);
        this.timestamp = timestamp;
    }

    public Transaction() {
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}


