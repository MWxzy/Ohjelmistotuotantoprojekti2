package database;

public record Unit(
        int id,
        String key,
        String category,
        String symbol,
        double factorToBase,
        double offsetToBase,
        String name,
        String description
) {

    @Override
    public String toString() {
        return name;
    }
}
