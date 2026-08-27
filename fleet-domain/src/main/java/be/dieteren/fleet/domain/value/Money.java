package be.dieteren.fleet.domain.value;

import be.dieteren.fleet.domain.utils.Currency;
import java.util.Objects;

/**
 * Represents a monetary value with an amount and a currency.
 */
public record Money(long amount, Currency currency) {
    public Money {
        Objects.requireNonNull(currency, "currency");
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative: " + amount);
        }
    }

    /**
     * Adds the amount of another Money object to this one, provided they have the same currency.
     *
     * @param other the other Money object to add
     * @return a new Money object representing the sum of both amounts
     * @throws IllegalArgumentException if the currencies of the two Money objects are different
     */
    public Money add(Money other) {
        Objects.requireNonNull(other, "other");
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot add Money with different currencies: " + this.currency + " and " + other.currency);
        }
        return new Money(Math.addExact(this.amount, other.amount), this.currency);
    }
}
