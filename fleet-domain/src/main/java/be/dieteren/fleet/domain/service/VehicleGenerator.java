package be.dieteren.fleet.domain.service;

import be.dieteren.fleet.domain.model.Vehicle;
import be.dieteren.fleet.domain.utils.Fuel;
import be.dieteren.fleet.domain.value.Mileage;
import be.dieteren.fleet.domain.value.Vin;

import java.time.Year;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.random.RandomGenerator;

/**
 * Generates realistic sample vehicles for training exercises.
 */
public final class VehicleGenerator {
    private static final int CURRENT_YEAR = Year.now(ZoneOffset.UTC).getValue();
    private static final String VIN_CHARACTERS = "ABCDEFGHJKLMNPRSTUVWXYZ0123456789";
    private static final List<BrandDefinition> BRANDS = List.of(
            new BrandDefinition("Audi", "WAU", List.of("A1", "A3", "A4", "Q3", "Q5"), List.of(Fuel.PETROL, Fuel.DIESEL, Fuel.HYBRID, Fuel.ELECTRIC)),
            new BrandDefinition("BMW", "WBA", List.of("Serie 1", "Serie 3", "X1", "X3", "i4"), List.of(Fuel.PETROL, Fuel.DIESEL, Fuel.HYBRID, Fuel.ELECTRIC)),
            new BrandDefinition("Mercedes", "WDB", List.of("A-Class", "C-Class", "GLA", "GLC", "EQE"), List.of(Fuel.PETROL, Fuel.DIESEL, Fuel.HYBRID, Fuel.ELECTRIC)),
            new BrandDefinition("Tesla", "5YJ", List.of("Model 3", "Model Y", "Model S"), List.of(Fuel.ELECTRIC)),
            new BrandDefinition("Volkswagen", "WVW", List.of("Golf", "Polo", "Passat", "Tiguan", "ID.4"), List.of(Fuel.PETROL, Fuel.DIESEL, Fuel.HYBRID, Fuel.ELECTRIC)),
            new BrandDefinition("Volvo", "YV1", List.of("XC40", "XC60", "V60", "EX30"), List.of(Fuel.PETROL, Fuel.DIESEL, Fuel.HYBRID, Fuel.ELECTRIC))
    );
    private static final List<String> DEALERS = List.of(
            "D'Ieteren Brussels",
            "D'Ieteren Antwerp",
            "D'Ieteren Ghent",
            "D'Ieteren Liège",
            "D'Ieteren Namur",
            "D'Ieteren Leuven",
            "D'Ieteren Charleroi",
            "D'Ieteren Hasselt"
    );
    private static final List<String> OPTIONS_POOL = List.of(
            "Adaptive Cruise Control",
            "Apple CarPlay",
            "Blind Spot Monitor",
            "Heated Seats",
            "Lane Assist",
            "Leather Seats",
            "Navigation",
            "Panoramic Roof",
            "Parking Camera",
            "Parking Sensors"
    );

    private final RandomGenerator random;

    public VehicleGenerator() {
        this(new Random());
    }

    public VehicleGenerator(long seed) {
        this(new Random(seed));
    }

    VehicleGenerator(RandomGenerator random) {
        this.random = Objects.requireNonNull(random, "random");
    }

    /**
     *
     */
    public List<Vehicle> generate(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count cannot be negative: " + count);
        }

        List<Vehicle> vehicles = new ArrayList<>(count);
        Set<String> usedVins = LinkedHashSet.newLinkedHashSet(Math.max(16, count));

        while (vehicles.size() < count) {
            Vehicle candidate = generateVehicle();
            if (usedVins.add(candidate.getVin().value())) {
                vehicles.add(candidate);
            }
        }

        return List.copyOf(vehicles);
    }

    public Vehicle generateVehicle() {
        BrandDefinition brand = pick(BRANDS);
        String model = pick(brand.models());
        Fuel fuel = pick(brand.fuels());
        int registrationYear = randomRegistrationYear();
        Mileage mileage = randomMileage(registrationYear, fuel);
        String dealer = pick(DEALERS);
        Set<String> options = randomOptions();

        return new Vehicle(
                new Vin(randomVin(brand.wmi())),
                brand.name(),
                model,
                fuel,
                mileage,
                registrationYear,
                dealer,
                options
        );
    }

    private int randomRegistrationYear() {
        return random.nextInt(CURRENT_YEAR - 12, CURRENT_YEAR + 1);
    }

    private Mileage randomMileage(int registrationYear, Fuel fuel) {
        int age = Math.max(0, CURRENT_YEAR - registrationYear);
        long annualMileage = switch (fuel) {
            case DIESEL -> random.nextLong(18_000, 36_001);
            case PETROL -> random.nextLong(10_000, 24_001);
            case HYBRID -> random.nextLong(12_000, 26_001);
            case ELECTRIC -> random.nextLong(8_000, 22_001);
        };
        long adjustment = random.nextLong(0, 12_001);
        return new Mileage(age * annualMileage + adjustment);
    }

    private Set<String> randomOptions() {
        int desiredSize = random.nextInt(0, 6);
        Set<String> options = LinkedHashSet.newLinkedHashSet(desiredSize);
        while (options.size() < desiredSize) {
            options.add(pick(OPTIONS_POOL));
        }
        return options;
    }

    private String randomVin(String wmi) {
        StringBuilder builder = new StringBuilder(17).append(wmi);
        while (builder.length() < 17) {
            builder.append(VIN_CHARACTERS.charAt(random.nextInt(VIN_CHARACTERS.length())));
        }
        return builder.toString();
    }

    private <T> T pick(List<T> values) {
        return values.get(random.nextInt(values.size()));
    }

    private record BrandDefinition(String name, String wmi, List<String> models, List<Fuel> fuels) {
    }
}


