package sayys.depthsupdate.world.surface;

import sayys.depthsupdate.core.HeightContext;
import sayys.depthsupdate.core.HeightManager;

/**
 * Resolves a Y coordinate relative to world height bounds.
 * Simplified backport of vanilla VerticalAnchor (no codecs).
 */
public interface VerticalAnchor {

    int resolveY(int minY, int maxY);

    // ===== Factory methods =====

    static VerticalAnchor absolute(int y) {
        return (minY, maxY) -> y;
    }

    static VerticalAnchor aboveBottom(int offset) {
        return (minY, maxY) -> minY + offset;
    }

    static VerticalAnchor belowTop(int offset) {
        return (minY, maxY) -> maxY - offset;
    }

    static VerticalAnchor bottom() {
        return aboveBottom(0);
    }

    static VerticalAnchor top() {
        return belowTop(0);
    }

    /**
     * Resolves using the standard extended overworld bounds.
     */
    default int resolveY() {
        HeightContext ctx = HeightManager.getMaxContext();
        return resolveY(ctx.minY(), ctx.maxY() - 1);
    }
}
