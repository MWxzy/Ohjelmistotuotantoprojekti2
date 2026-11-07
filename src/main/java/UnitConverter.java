import database.Unit;

public class UnitConverter {

    public double convertTemperature(double value, Unit from, Unit to) {
        requireFinite(value, "Temperature");
        requireCategory(from, "TEMPERATURE");
        requireCategory(to, "TEMPERATURE");

        double base = value * from.factorToBase() + from.offsetToBase();
        return (base - to.offsetToBase()) / to.factorToBase();
    }

    public double convertLinear(double value, Unit from, Unit to) {
        requireFinite(value, "Value");
        requireUnit(from);
        requireUnit(to);

        if (!from.category().equals(to.category())) {
            throw new IllegalArgumentException("Units must belong to the same category.");
        }

        if (from.factorToBase() <= 0 || to.factorToBase() <= 0) {
            throw new IllegalArgumentException("Conversion factors must be positive.");
        }

        return value * from.factorToBase() / to.factorToBase();
    }

    public double calculateSpeed(
            double distance,
            Unit distanceUnit,
            double time,
            Unit timeUnit,
            Unit speedUnit
    ) {
        requireFinite(distance, "Distance");
        requireFinite(time, "Time");
        requireCategory(distanceUnit, "DISTANCE");
        requireCategory(timeUnit, "TIME");
        requireCategory(speedUnit, "SPEED");

        if (distance < 0) {
            throw new IllegalArgumentException("Distance cannot be negative.");
        }

        if (time <= 0) {
            throw new IllegalArgumentException("Time must be greater than zero.");
        }

        if (distanceUnit.factorToBase() <= 0
                || timeUnit.factorToBase() <= 0
                || speedUnit.factorToBase() <= 0) {
            throw new IllegalArgumentException("Conversion factors must be positive.");
        }

        double metres = distance * distanceUnit.factorToBase();
        double seconds = time * timeUnit.factorToBase();
        return metres / seconds / speedUnit.factorToBase();
    }

    private void requireFinite(double value, String label) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(label + " must be a finite number.");
        }
    }

    private void requireUnit(Unit unit) {
        if (unit == null) {
            throw new IllegalArgumentException("Unit is required.");
        }
    }

    private void requireCategory(Unit unit, String category) {
        requireUnit(unit);
        if (!category.equals(unit.category())) {
            throw new IllegalArgumentException("Expected a " + category + " unit.");
        }
    }
}
