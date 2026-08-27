package be.dieteren.fleet.domain.event;

import be.dieteren.fleet.domain.value.Mileage;
import be.dieteren.fleet.domain.value.Vin;

import java.time.Instant;

public class FleetEventDescriber {

    public String describe(FleetEvent event) {
        switch (event){
            case VehicleRegistered(Vin vin, Instant occurredAt, String dealer) -> {
                return "Vehicle with VIN %s was registered at %s by dealer %s".formatted(vin, occurredAt, dealer);
            }

            case MileageUpdated(Vin vin, Instant at, Mileage mil) when mil.value() > 200_000 -> {
                return "Vehicle with VIN %s had its mileage updated to %d at %s. Reached the usage limit".formatted(vin, mil.value(), at);
            }

            case MileageUpdated(Vin vin, Instant at, Mileage mil) -> {
                return "Vehicle with VIN %s had its mileage updated to %d at %s".formatted(vin, mil.value(), at);
            }

            case VehicleUpdated(Vin vin, Instant occurredAt, Mileage km) -> {
                return "Vehicle with VIN %s was updated at %s with mileage %d".formatted(vin, occurredAt, km.value());
            }

            case VehicleSold s-> {
                return "Vehicle with VIN %s was sold at %s to buyer %s".formatted(s.vin(), s.occurredAt(), s.buyer());
            }

        }
    }
}
