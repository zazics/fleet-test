package be.dieteren.fleet.domain.service;

import be.dieteren.fleet.domain.model.Vehicle;
import be.dieteren.fleet.domain.utils.Fuel;
import be.dieteren.fleet.domain.value.Mileage;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Solutions for exercise 2.2: 15 aggregations using Streams and Collectors.
 */
public class VehicleAggregations {

    /**
     * 1. Count vehicles by brand
     * Result: {BMW=12, Audi=8, VW=30}
     */
    public static Map<String, Long> countByBrand(List<Vehicle> vehicles) {
        return vehicles.stream()
                .collect(Collectors.groupingBy(Vehicle::getBrand, Collectors.counting()));
    }

    /**
     * 2. Total mileage by brand
     */
    public static Map<String, Long> totalMileageByBrand(List<Vehicle> vehicles) {
        return vehicles.stream()
                .collect(Collectors.groupingBy(
                        Vehicle::getBrand,
                        Collectors.summingLong(v -> v.getMileage().value())
                ));
    }

    /**
     * 3. Unique models grouped by brand
     */
    public static Map<String, Set<String>> modelsByBrand(List<Vehicle> vehicles) {
        return vehicles.stream()
                .collect(Collectors.groupingBy(
                        Vehicle::getBrand,
                        Collectors.mapping(Vehicle::getModel, Collectors.toSet())
                ));
    }

    /**
     * 4. Newest vehicle class by brand (max registration year)
     */
    public static Map<String, Optional<Vehicle>> newestByBrand(List<Vehicle> vehicles) {
        return vehicles.stream()
                .collect(Collectors.groupingBy(
                        Vehicle::getBrand,
                        Collectors.maxBy(Comparator.comparingInt(Vehicle::getRegistrationYear))
                ));
    }

    /**
     * 5. Average mileage by dealer
     */
    public static Map<String, Double> avgMileageByDealer(List<Vehicle> vehicles) {
        return vehicles.stream()
                .collect(Collectors.groupingBy(
                        Vehicle::getDealer,
                        Collectors.averagingLong(v -> v.getMileage().value())
                ));
    }

    /**
     * 6. Vehicles grouped by age range (0-2 years, 3-5 years, 6+ years)
     * Uses current year 2024
     */
    public static Map<String, List<Vehicle>> vehiclesByAgeRange(List<Vehicle> vehicles) {
        return vehicles.stream()
                .collect(Collectors.groupingBy(v -> {
                    int age = 2024 - v.getRegistrationYear();
                    if (age <= 2) return "0-2 years";
                    if (age <= 5) return "3-5 years";
                    return "6+ years";
                }));
    }

    /**
     * 7. Partition: electric vs thermal with total mileage per group
     * Uses fuel type: ELECTRIC partition is true, others are false
     */
    public static Map<Boolean, Long> electricVsThermicMileage(List<Vehicle> vehicles) {
        return vehicles.stream()
                .collect(Collectors.partitioningBy(
                        v -> v.getFuel() == Fuel.ELECTRIC,
                        Collectors.summingLong(v -> v.getMileage().value())
                ));
    }

    /**
     * 8. Formatted string "BMW (12), Audi (8), VW (30)" sorted by count descending
     */
    public static String brandCountString(List<Vehicle> vehicles) {
        return vehicles.stream()
                .collect(Collectors.groupingBy(Vehicle::getBrand, Collectors.counting()))
                .entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .map(e -> e.getKey() + " (" + e.getValue() + ")")
                .collect(Collectors.joining(", "));
    }

    /**
     * 9. Complete statistics on mileage (min, max, average, sum, count)
     */
    public static LongSummaryStatistics mileageStatistics(List<Vehicle> vehicles) {
        return vehicles.stream()
                .collect(Collectors.summarizingLong(v -> v.getMileage().value()));
    }

    /**
     * 10. Oldest vehicle (min registration year) by dealer
     */
    public static Map<String, Vehicle> oldestVehicleByDealer(List<Vehicle> vehicles) {
        return vehicles.stream()
                .collect(Collectors.groupingBy(
                        Vehicle::getDealer,
                        Collectors.collectingAndThen(
                                Collectors.minBy(Comparator.comparingInt(Vehicle::getRegistrationYear)),
                                opt -> opt.orElseThrow(() -> new IllegalStateException("No vehicle found"))
                        )
                ));
    }

    /**
     * 11. All distinct options across all vehicles (flattened)
     */
    public static Set<String> allDistinctOptions(List<Vehicle> vehicles) {
        return vehicles.stream()
                .flatMap(v -> v.getOptions().stream())
                .collect(Collectors.toSet());
    }

    /**
     * 12. In a single pass: average mileage AND count of vehicles > 100,000 km
     * Uses Collectors.teeing to combine two collectors
     */
    public static AverageMileageAndHighMileageCount averageAndHighMileageCount(List<Vehicle> vehicles) {
        return vehicles.stream()
                .collect(Collectors.teeing(
                        Collectors.averagingLong(v -> v.getMileage().value()),
                        Collectors.filtering(v -> v.getMileage().value() > 100_000, Collectors.counting()),
                        AverageMileageAndHighMileageCount::new
                ));
    }

    /**
     * 13. Custom collector: accumulate into FleetSummary
     */
    public static FleetSummary toFleetSummary(List<Vehicle> vehicles) {
        return vehicles.stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> new FleetSummary(
                                list.size(),
                                list.stream().mapToLong(v -> v.getMileage().value()).sum(),
                                list.stream().mapToLong(v -> v.getMileage().value()).average().orElse(0.0)
                        )
                ));
    }

    /**
     * 14. First 5 VINs starting with "WVW" from an infinite stream
     * Demonstrates laziness with peek
     */
    public static List<String> first5VinsWithPrefix(String prefix) {
        return Stream.iterate(0, i -> i + 1)
                .map(i -> prefix + String.format("%010d", i))
                .peek(vin -> System.out.println("Generated: " + vin))  // Demonstrates laziness
                .limit(5)
                .collect(Collectors.toList());
    }

    /**
     * 15. Number the results using IntStream.range
     */
    public static List<String> numberedVehicles(List<Vehicle> vehicles) {
        return IntStream.range(0, vehicles.size())
                .mapToObj(i -> (i + 1) + ". " + vehicles.get(i).getBrand() + " " + vehicles.get(i).getModel())
                .collect(Collectors.toList());
    }

    /**
     * Record for result of aggregation 12
     */
    public record AverageMileageAndHighMileageCount(double averageMileage, long highMileageCount) {}

    /**
     * Record for custom collector result (aggregation 13)
     */
    public record FleetSummary(int totalVehicles, long totalMileage, double averageMileage) {}
}
