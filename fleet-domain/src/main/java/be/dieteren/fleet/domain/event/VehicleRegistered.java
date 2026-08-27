package be.dieteren.fleet.domain.event;

import be.dieteren.fleet.domain.value.Vin;

import java.time.Instant;

public record VehicleRegistered(Vin vin, Instant occurredAt, String dealer) implements FleetEvent {

}
