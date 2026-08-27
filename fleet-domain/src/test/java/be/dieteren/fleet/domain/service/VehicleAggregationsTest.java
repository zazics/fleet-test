package be.dieteren.fleet.domain.service;

import be.dieteren.fleet.domain.model.Vehicle;
import be.dieteren.fleet.domain.utils.Fuel;
import be.dieteren.fleet.domain.value.Mileage;
import be.dieteren.fleet.domain.value.Vin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for VehicleAggregations exercise 2.2: 15 Streams collectors.
 */
@DisplayName("Exercise 2.2: 15 Aggregations with Streams")
class VehicleAggregationsTest {

    private List<Vehicle> vehicles;

    @BeforeEach
    void setUp() {
        vehicles = List.of(
                // BMW vehicles
                new Vehicle(new Vin("WBA1234567890000"), "BMW", "3 Series", Fuel.PETROL, new Mileage(45000), 2023, "Brussels Dealer", Set.of("leather", "GPS")),
                new Vehicle(new Vin("WBA1234567890001"), "BMW", "5 Series", Fuel.DIESEL, new Mileage(120000), 2020, "Brussels Dealer", Set.of("panoroof", "GPS")),
                new Vehicle(new Vin("WBA1234567890002"), "BMW", "3 Series", Fuel.HYBRID, new Mileage(80000), 2021, "Brussels Dealer", Set.of("leather")),
                new Vehicle(new Vin("WBA1234567890003"), "BMW", "X5", Fuel.ELECTRIC, new Mileage(25000), 2023, "Antwerp Dealer", Set.of("GPS")),

                // Audi vehicles
                new Vehicle(new Vin("WAUZZZ8P1BA100001"), "Audi", "A4", Fuel.PETROL, new Mileage(95000), 2019, "Brussels Dealer", Set.of("cruise control", "GPS")),
                new Vehicle(new Vin("WAUZZZ8P1BA100002"), "Audi", "A6", Fuel.DIESEL, new Mileage(150000), 2018, "Antwerp Dealer", Set.of("panoroof")),
                new Vehicle(new Vin("WAUZZZ8P1BA100003"), "Audi", "Q5", Fuel.HYBRID, new Mileage(110000), 2020, "Liege Dealer", Set.of("leather", "cruise control")),

                // VW vehicles
                new Vehicle(new Vin("WVWZZZ1KZAW000001"), "VW", "Golf", Fuel.PETROL, new Mileage(55000), 2022, "Brussels Dealer", Set.of("cruise control")),
                new Vehicle(new Vin("WVWZZZ1KZAW000002"), "VW", "Passat", Fuel.DIESEL, new Mileage(185000), 2017, "Brussels Dealer", Set.of("panoroof", "GPS")),
                new Vehicle(new Vin("WVWZZZ1KZAW000003"), "VW", "ID.4", Fuel.ELECTRIC, new Mileage(35000), 2022, "Antwerp Dealer", Set.of("leather", "GPS")),
                new Vehicle(new Vin("WVWZZZ1KZAW000004"), "VW", "Tiguan", Fuel.HYBRID, new Mileage(72000), 2021, "Liege Dealer", Set.of("cruise control"))
        );
    }

    @Test
    @DisplayName("1. Count vehicles by brand")
    void test_1_countByBrand() {
        Map<String, Long> result = VehicleAggregations.countByBrand(vehicles);

        assertThat(result)
                .containsEntry("BMW", 4L)
                .containsEntry("Audi", 3L)
                .containsEntry("VW", 4L)
                .hasSize(3);
    }

    @Test
    @DisplayName("2. Total mileage by brand")
    void test_2_totalMileageByBrand() {
        Map<String, Long> result = VehicleAggregations.totalMileageByBrand(vehicles);

        assertThat(result)
                .containsEntry("BMW", 270000L)     // 45k + 120k + 80k + 25k
                .containsEntry("Audi", 355000L)    // 95k + 150k + 110k
                .containsEntry("VW", 347000L);     // 55k + 185k + 35k + 72k
    }

    @Test
    @DisplayName("3. Unique models grouped by brand")
    void test_3_modelsByBrand() {
        Map<String, Set<String>> result = VehicleAggregations.modelsByBrand(vehicles);

        assertThat(result)
                .containsEntry("BMW", Set.of("3 Series", "5 Series", "X5"))
                .containsEntry("Audi", Set.of("A4", "A6", "Q5"))
                .containsEntry("VW", Set.of("Golf", "Passat", "ID.4", "Tiguan"));
    }

    @Test
    @DisplayName("4. Newest vehicle by brand")
    void test_4_newestByBrand() {
        Map<String, Optional<Vehicle>> result = VehicleAggregations.newestByBrand(vehicles);

        assertThat(result)
                .containsKey("BMW")
                .containsKey("Audi")
                .containsKey("VW");

        // BMW newest: 2023 (multiple options)
        assertThat(result.get("BMW")).isPresent()
                .get().satisfies(v -> assertThat(v.getRegistrationYear()).isEqualTo(2023));

        // Audi newest: 2020
        assertThat(result.get("Audi")).isPresent()
                .get().satisfies(v -> assertThat(v.getRegistrationYear()).isEqualTo(2020));

        // VW newest: 2022
        assertThat(result.get("VW")).isPresent()
                .get().satisfies(v -> assertThat(v.getRegistrationYear()).isEqualTo(2022));
    }

    @Test
    @DisplayName("5. Average mileage by dealer")
    void test_5_avgMileageByDealer() {
        Map<String, Double> result = VehicleAggregations.avgMileageByDealer(vehicles);

        assertThat(result)
                .containsEntry("Brussels Dealer", 81000.0)    // (45 + 120 + 80 + 95 + 55 + 185) / 6
                .containsEntry("Antwerp Dealer", 58750.0)     // (25 + 150 + 35) / 3
                .containsEntry("Liege Dealer", 97333.33, within(0.1));
    }

    @Test
    @DisplayName("6. Vehicles grouped by age range")
    void test_6_vehiclesByAgeRange() {
        Map<String, List<Vehicle>> result = VehicleAggregations.vehiclesByAgeRange(vehicles);

        assertThat(result)
                .containsKey("0-2 years")
                .containsKey("3-5 years")
                .containsKey("6+ years");

        // 0-2 years: 2023 & 2022 vehicles
        assertThat(result.get("0-2 years")).hasSize(3)
                .allSatisfy(v -> assertThat(v.getRegistrationYear()).isIn(2023, 2022));

        // 6+ years: 2018 & 2017
        assertThat(result.get("6+ years")).hasSize(2)
                .allSatisfy(v -> assertThat(v.getRegistrationYear()).isIn(2018, 2017));
    }

    @Test
    @DisplayName("7. Partition electric vs thermic with total mileage")
    void test_7_electricVsThermicMileage() {
        Map<Boolean, Long> result = VehicleAggregations.electricVsThermicMileage(vehicles);

        assertThat(result)
                .containsEntry(true, 60000L)    // BMW X5 (25k) + VW ID.4 (35k)
                .containsEntry(false, 952000L); // All others
    }

    @Test
    @DisplayName("8. Formatted brand count string sorted by count descending")
    void test_8_brandCountString() {
        String result = VehicleAggregations.brandCountString(vehicles);

        assertThat(result)
                .contains("BMW (4)")
                .contains("VW (4)")
                .contains("Audi (3)")
                .startsWith("BMW (4)") // Appears first (tied with VW but sorted)
                .endsWith("Audi (3)"); // Appears last
    }

    @Test
    @DisplayName("9. Complete mileage statistics")
    void test_9_mileageStatistics() {
        LongSummaryStatistics stats = VehicleAggregations.mileageStatistics(vehicles);

        assertThat(stats)
                .satisfies(s -> {
                    assertThat(s.getMin()).isEqualTo(25000L);
                    assertThat(s.getMax()).isEqualTo(185000L);
                    assertThat(s.getSum()).isEqualTo(972000L);
                    assertThat(s.getCount()).isEqualTo(11L);
                    assertThat(s.getAverage()).isEqualTo(88363.636, within(1.0));
                });
    }

    @Test
    @DisplayName("10. Oldest vehicle by dealer")
    void test_10_oldestVehicleByDealer() {
        Map<String, Vehicle> result = VehicleAggregations.oldestVehicleByDealer(vehicles);

        assertThat(result)
                .hasSize(3);

        // Brussels Dealer oldest: 2017 Passat
        assertThat(result.get("Brussels Dealer"))
                .satisfies(v -> {
                    assertThat(v.getRegistrationYear()).isEqualTo(2017);
                    assertThat(v.getModel()).isEqualTo("Passat");
                });

        // Antwerp Dealer oldest: 2018 A6
        assertThat(result.get("Antwerp Dealer"))
                .satisfies(v -> {
                    assertThat(v.getRegistrationYear()).isEqualTo(2018);
                    assertThat(v.getModel()).isEqualTo("A6");
                });

        // Liege Dealer oldest: 2020
        assertThat(result.get("Liege Dealer"))
                .satisfies(v -> assertThat(v.getRegistrationYear()).isEqualTo(2020));
    }

    @Test
    @DisplayName("11. All distinct options across vehicles")
    void test_11_allDistinctOptions() {
        Set<String> result = VehicleAggregations.allDistinctOptions(vehicles);

        assertThat(result)
                .containsExactlyInAnyOrder("leather", "GPS", "panoroof", "cruise control")
                .hasSize(4);
    }

    @Test
    @DisplayName("12. Average mileage AND count vehicles > 100k km in single pass")
    void test_12_averageAndHighMileageCount() {
        VehicleAggregations.AverageMileageAndHighMileageCount result =
                VehicleAggregations.averageAndHighMileageCount(vehicles);

        assertThat(result.averageMileage()).isEqualTo(88363.636, within(1.0));
        assertThat(result.highMileageCount()).isEqualTo(4L); // 120k, 150k, 110k, 185k
    }

    @Test
    @DisplayName("13. Custom collector: FleetSummary")
    void test_13_fleetSummary() {
        VehicleAggregations.FleetSummary result = VehicleAggregations.toFleetSummary(vehicles);

        assertThat(result)
                .satisfies(s -> {
                    assertThat(s.totalVehicles()).isEqualTo(11);
                    assertThat(s.totalMileage()).isEqualTo(972000L);
                    assertThat(s.averageMileage()).isEqualTo(88363.636, within(1.0));
                });
    }

    @Test
    @DisplayName("14. First 5 VINs with prefix from infinite stream (demonstrates laziness)")
    void test_14_first5VinsWithPrefix() {
        List<String> result = VehicleAggregations.first5VinsWithPrefix("WVW");

        assertThat(result)
                .hasSize(5)
                .allSatisfy(vin -> assertThat(vin).startsWith("WVW"))
                .containsExactly(
                        "WVW0000000000",
                        "WVW0000000001",
                        "WVW0000000002",
                        "WVW0000000003",
                        "WVW0000000004"
                );
    }

    @Test
    @DisplayName("15. Number the results with IntStream")
    void test_15_numberedVehicles() {
        List<String> result = VehicleAggregations.numberedVehicles(vehicles);

        assertThat(result)
                .hasSize(11)
                .startsWith("1. BMW 3 Series")
                .contains("2. BMW 5 Series")
                .endsWith("11. VW Tiguan");

        // Verify numbering
        assertThat(result).allSatisfy(s -> {
            String number = s.split("\\.")[0];
            assertThat(Integer.parseInt(number)).isPositive();
        });
    }

    @Test
    @DisplayName("Performance comparison: stream vs parallelStream vs for loop")
    void test_performance_comparison() {
        // Create a larger dataset (50,000 vehicles for realistic measurement)
        List<Vehicle> largeDataset = new ArrayList<>();
        for (int i = 0; i < 50_000; i++) {
            largeDataset.add(new Vehicle(
                    new Vin("WVW" + String.format("%013d", i)),
                    "Brand" + (i % 10),
                    "Model" + (i % 20),
                    Fuel.values()[i % 4],
                    new Mileage((long) (Math.random() * 300_000)),
                    2024 - (i % 8),
                    "Dealer" + (i % 5),
                    Set.of("option1", "option2")
            ));
        }

        // Warmup
        VehicleAggregations.countByBrand(largeDataset);

        // Measure stream()
        long start1 = System.nanoTime();
        VehicleAggregations.countByBrand(largeDataset);
        long duration1 = System.nanoTime() - start1;

        // Measure parallelStream()
        long start2 = System.nanoTime();
        largeDataset.parallelStream()
                .collect(Collectors.groupingBy(Vehicle::getBrand, Collectors.counting()));
        long duration2 = System.nanoTime() - start2;

        // Measure for loop
        long start3 = System.nanoTime();
        Map<String, Integer> countByForLoop = new HashMap<>();
        for (Vehicle v : largeDataset) {
            countByForLoop.put(v.getBrand(), countByForLoop.getOrDefault(v.getBrand(), 0) + 1);
        }
        long duration3 = System.nanoTime() - start3;

        System.out.println("\n=== Performance Results (50,000 vehicles) ===");
        System.out.println("stream():         " + (duration1 / 1_000_000.0) + " ms");
        System.out.println("parallelStream(): " + (duration2 / 1_000_000.0) + " ms");
        System.out.println("for loop:         " + (duration3 / 1_000_000.0) + " ms");
        System.out.println("=== Performance Results (11 vehicles) ===");

        // Also test with small dataset
        long start4 = System.nanoTime();
        VehicleAggregations.countByBrand(vehicles);
        long duration4 = System.nanoTime() - start4;

        System.out.println("stream():         " + (duration4 / 1_000.0) + " µs");
    }
}
