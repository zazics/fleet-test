package be.dieteren.fleet.domain.model;

import be.dieteren.fleet.domain.utils.Fuel;
import be.dieteren.fleet.domain.value.Mileage;
import be.dieteren.fleet.domain.value.Vin;

import java.util.Objects;
import java.util.Set;

/**
 * Represents a vehicle in the fleet management system.
 */
public class Vehicle {
    private final Vin vin;
    private final String brand;
    private final String model;
    private final Fuel fuel;
    private final Mileage mileage;
    private final int registrationYear;
    private final String dealer;
    private final Set<String> options;

    public Vehicle(Vin vin,
                   String brand,
                   String model,
                   Fuel fuel,
                   Mileage mileage,
                   int registrationYear,
                   String dealer,
                   Set<String> options) {
        this.vin = Objects.requireNonNull(vin, "vin");
        this.brand = Objects.requireNonNull(brand, "brand");
        this.model = Objects.requireNonNull(model, "model");
        this.fuel = Objects.requireNonNull(fuel, "fuel");
        this.mileage = Objects.requireNonNull(mileage, "mileage");
        this.dealer = Objects.requireNonNull(dealer, "dealer");
        this.options = Set.copyOf(Objects.requireNonNull(options, "options"));

        if (registrationYear < 1886) {
            throw new IllegalArgumentException("registrationYear is invalid: " + registrationYear);
        }
        this.registrationYear = registrationYear;
    }

    public Vin getVin() {
        return vin;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public Fuel getFuel() {
        return fuel;
    }

    public Mileage getMileage() {
        return mileage;
    }

    public int getRegistrationYear() {
        return registrationYear;
    }

    public String getDealer() {
        return dealer;
    }

    public Set<String> getOptions() {
        return options;
    }

    @Override
    public String toString() {
        return "Vehicle %s %s %s".formatted(brand, model, dealer);
    }
}