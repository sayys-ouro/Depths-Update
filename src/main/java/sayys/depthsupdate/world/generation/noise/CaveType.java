package sayys.depthsupdate.world.generation.noise;

/**
 * Which generator owns a carve. The min composition keeps only the lowest
 * density, so without this the winner is unrecoverable afterwards.
 */
public enum CaveType {
    CHEESE("cheese"),
    SPAGHETTI("spaghetti"),
    NOODLE("noodle"),
    ENTRANCE("entrance");

    private final String label;

    CaveType(String label) {
        this.label = label;
    }

    public String label() {
        return this.label;
    }
}
