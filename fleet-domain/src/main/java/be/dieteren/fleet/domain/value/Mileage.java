package be.dieteren.fleet.domain.value;

import java.util.Objects;

/**
 * Represents the mileage of a vehicle.
 */
public record Mileage(long value) {
    public Mileage() {
        this(0L);
    }

    public Mileage {
        if (value < 0) {
            throw new IllegalArgumentException("Mileage cannot be negative: " + value);
        }
    }

    /**
     * Adds the mileage of another Mileage object to this one.
     *
     * @param other the other Mileage object to add
     * @return a new Mileage object representing the sum of both mileages
     */
    public Mileage add(Mileage other) {
        Objects.requireNonNull(other, "other cannot be null");
        return new Mileage(Math.addExact(this.value, other.value));
    }
}
