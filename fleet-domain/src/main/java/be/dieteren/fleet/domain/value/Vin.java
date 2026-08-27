package be.dieteren.fleet.domain.value;

import java.util.Objects;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Represents the Vehicle Identification Number (VIN) of a vehicle.
 */
public record Vin(String value) {
    private static final Pattern PATTERN = Pattern.compile("[A-HJ-NPR-Z0-9]{17}");

    public Vin {
        Objects.requireNonNull(value, "vin");
        value = value.trim().toUpperCase(Locale.ROOT);
        if (!PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid VIN: " + value);
        }
    }

    /**
     * Returns the World Manufacturer Identifier (WMI) part of the VIN.
     *
     * @return the WMI part of the VIN
     */
    public String worldManufacturerIdentifier() { return value.substring(0, 3); }
}
