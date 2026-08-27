package be.dieteren.fleet.domain.service;

import be.dieteren.fleet.domain.model.Vehicle;
import org.junit.jupiter.api.Test;

import java.time.Year;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VehicleGeneratorTest {
    private static final int CURRENT_YEAR = Year.now(ZoneOffset.UTC).getValue();

    @Test
    void generatesRequestedNumberOfVehiclesWithUniqueVins() {
        VehicleGenerator generator = new VehicleGenerator(42L);

        List<Vehicle> vehicles = generator.generate(500);

        assertEquals(500, vehicles.size());
        assertEquals(500, vehicles.stream().map(vehicle -> vehicle.getVin().value()).distinct().count());
        assertTrue(vehicles.stream().allMatch(vehicle -> vehicle.getOptions().stream().noneMatch(java.util.Objects::isNull)));
        assertTrue(vehicles.stream().allMatch(vehicle -> vehicle.getRegistrationYear() <= CURRENT_YEAR));
    }

    @Test
    void generatesDeterministicSequenceForSameSeed() {
        VehicleGenerator first = new VehicleGenerator(123L);
        VehicleGenerator second = new VehicleGenerator(123L);

        List<Vehicle> firstBatch = first.generate(25);
        List<Vehicle> secondBatch = second.generate(25);

        for (int index = 0; index < firstBatch.size(); index++) {
            Vehicle left = firstBatch.get(index);
            Vehicle right = secondBatch.get(index);

            assertEquals(left.getVin(), right.getVin());
            assertEquals(left.getBrand(), right.getBrand());
            assertEquals(left.getModel(), right.getModel());
            assertEquals(left.getFuel(), right.getFuel());
            assertEquals(left.getMileage(), right.getMileage());
            assertEquals(left.getRegistrationYear(), right.getRegistrationYear());
            assertEquals(left.getDealer(), right.getDealer());
            assertEquals(left.getOptions(), right.getOptions());
        }
    }

    @Test
    void rejectsNegativeCount() {
        VehicleGenerator generator = new VehicleGenerator();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> generator.generate(-1));

        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("negative"));
    }
}



