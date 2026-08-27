package be.dieteren.fleet.domain.event;

import be.dieteren.fleet.domain.value.Mileage;
import be.dieteren.fleet.domain.value.Vin;

import java.time.Instant;
import java.util.Objects;

public record VehicleUpdated(Vin vin, Instant occurredAt, Mileage km) implements FleetEvent {
    public VehicleUpdated {
        Objects.requireNonNull(vin);
        Objects.requireNonNull(occurredAt);
        Objects.requireNonNull(km);
    }
}
