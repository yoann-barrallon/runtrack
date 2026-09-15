package fr.yoannbarrallon.runtrack.stat;

public enum RecordDistanceType {
    ONE_KM(1_000),
    FIVE_KM(5_000),
    TEN_KM(10_000),
    HALF_MARATHON(21_097);

    private final int distanceMeters;

    RecordDistanceType(int distanceMeters) {
        this.distanceMeters = distanceMeters;
    }

    public int distanceMeters() {
        return distanceMeters;
    }
}
