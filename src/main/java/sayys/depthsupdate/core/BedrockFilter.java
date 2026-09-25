package sayys.depthsupdate.core;

public final class BedrockFilter {
    private static final ThreadLocal<Boolean> ACTIVE = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private BedrockFilter() {}

    public static void begin() {
        ACTIVE.set(Boolean.TRUE);
    }

    public static void end() {
        ACTIVE.set(Boolean.FALSE);
    }

    public static boolean active() {
        return ACTIVE.get();
    }
}
