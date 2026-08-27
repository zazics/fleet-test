package be.dieteren.fleet.domain.event;

import be.dieteren.fleet.domain.value.Vin;

import java.time.Instant;

public sealed interface FleetEvent permits VehicleRegistered,VehicleSold,MileageUpdated,VehicleUpdated {
    Vin vin();
    Instant occurredAt();
}
