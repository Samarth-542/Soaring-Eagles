package in.soaringeagles.api.registrations;

public enum StudyYear {
    FIRST(1),
    SECOND(2),
    THIRD(3),
    FOURTH(4);

    private final int value;

    StudyYear(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
