package sayys.depthsupdate.mixin.mod.optifine;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import sayys.depthsupdate.DepthsUpdateMod;

/**
 * These mixins reach into OptiFine internals by reflection, so a build that
 * renamed a field leaves the extended-height renderer half-wired. Reporting it
 * once beats the previous silent catch, where the symptom was an invisible
 * world with nothing in the log.
 */
final class OptiFineCompatLog {
    private static final Set<String> REPORTED = ConcurrentHashMap.newKeySet();

    private OptiFineCompatLog() {
    }

    static void once(String what, Throwable error) {
        if (REPORTED.add(what)) {
            DepthsUpdateMod.LOGGER.error(
                    "OptiFine compatibility: {} failed. Extended height rendering may be wrong on this OptiFine build.",
                    what, error);
        }
    }
}
