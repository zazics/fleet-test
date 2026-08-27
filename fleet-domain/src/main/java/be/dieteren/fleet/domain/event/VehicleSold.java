package be.dieteren.fleet.domain.event;

import be.dieteren.fleet.domain.value.Vin;

import java.time.Instant;

public record VehicleSold(Vin vin, Instant occurredAt, String buyer) implements FleetEvent {

}
