package be.dieteren.fleet;

import be.dieteren.fleet.domain.model.Vehicle;
import be.dieteren.fleet.domain.service.VehicleGenerator;
import be.dieteren.fleet.domain.utils.Fuel;

import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    static void main() {


        VehicleGenerator generator = new VehicleGenerator(42L);

        List<Vehicle> vehicles = generator.generate(50_000);

        // 2.2

        // 1. Liste des marques distinctes, triées.
        var brands = vehicles.stream().collect(
                Collectors.groupingBy(Vehicle::getBrand, Collectors.counting()));
        System.out.println("Brands:" + brands);

        // 2. Kilométrage moyen par marque → Map<String, Double>.
        var kms = vehicles.stream().collect(
                Collectors.groupingBy(Vehicle::getBrand,
                        Collectors.averagingLong((v) -> v.getMileage().value())));

        System.out.println("Kms:" + kms);

        // 3. Nombre de véhicules par (marque, motorisation) → Map<String, Map<Fuel, Long>>.
        var vehicles3 = vehicles.stream().collect(
                Collectors.groupingBy(Vehicle::getBrand,
                        Collectors.groupingBy(Vehicle::getFuel, Collectors.counting())));

        System.out.println("Vehicles by brand and fuel:" + vehicles3);

        // 4. Top 3 des concessions par nombre de véhicules.
        var dealersTop3 = vehicles.stream().collect(
                        Collectors.groupingBy(Vehicle::getDealer, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .toList();

        System.out.println("Top 3 dealers:" + dealersTop3);

        // 5. Map<Vin, Vehicle> — gérez explicitement le cas de VIN dupliqué avec le merge function.
        var mapVehicles = vehicles.stream().collect(
                Collectors.toMap(Vehicle::getVin, vehicle -> vehicle,
                        (existing, replacement) -> {
                            System.out.println("VIN duplicity detected: " + existing.getVin());
                            return existing;
                        })
        );

        System.out.println("Map VIN:" + mapVehicles);

        // 6. Véhicules groupés par tranche d'âge (0-2 ans, 3-5, 6+) — groupingBy avec fonction de classification calculée.

        var agedVehicles = vehicles.stream().collect(
                Collectors.groupingBy((v) -> {
                    int age = Year.now().getValue() - v.getRegistrationYear();
                    if (age <= 2) return "0-2 ans";
                    if (age <= 5) return "3-5 ans";
                    else return "6+ ans";
                }));

        System.out.println("Vehicles by age range:" + agedVehicles);

        // 7. Séparer électriques / thermiques (partitioningBy) avec, pour chaque groupe, le kilométrage total (downstream summingLong).

        var elecVehicles = vehicles.stream().collect(
                Collectors.partitioningBy((v) -> v.getFuel() == Fuel.ELECTRIC,
                        Collectors.summingLong((v) -> v.getMileage().value())
                ));

        System.out.println("Vehicles splitted:" + elecVehicles);

        // 8. Chaîne « BMW (12), Audi (8), VW (30) » triée par count décroissant → joining.
        var brandCounts = vehicles.stream().collect(
                        Collectors.groupingBy(Vehicle::getBrand, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(e -> "%s (%d)".formatted(e.getKey(), e.getValue()))
                .collect(Collectors.joining(","));

    }
}
