package sayys.depthsupdate.compat.optifine;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import sayys.depthsupdate.DepthsUpdateMod;

public final class OptiFineCompatLog {
    private static final Set<String> REPORTED = ConcurrentHashMap.newKeySet();

    private OptiFineCompatLog() {}

    public static void once(String what, Throwable error) {
        if (REPORTED.add(what)) {
            DepthsUpdateMod.LOGGER.error(
                "OptiFine compatibility: {} failed.",
                what,
                error
            );
        }
    }
}
