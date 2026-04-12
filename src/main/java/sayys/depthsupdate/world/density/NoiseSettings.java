package sayys.depthsupdate.world.density;

/**
 * Defines the noise generation parameters for a dimension: Y range and cell sizes.
 * Faithful backport of modern Minecraft's NoiseSettings record.
 */
public final class NoiseSettings {
    // Vanilla dimension presets (OVERWORLD is dynamic, see overworld() method)
    public static final NoiseSettings NETHER = create(0, 128, 1, 2);
    public static final NoiseSettings END = create(0, 128, 2, 1);
    public static final NoiseSettings FLOATING_ISLANDS = create(0, 256, 2, 1);

    /**
     * Returns overworld noise settings derived from HeightManager config.
     * This replaces the old static OVERWORLD constant.
     */
    public static NoiseSettings overworld() {
        sayys.depthsupdate.core.HeightContext ctx = sayys.depthsupdate.core.HeightManager.getMaxContext();
        return create(ctx.minY(), ctx.totalHeight(), 1, 2);
    }

    /**
     * Returns cave noise settings derived from HeightManager config.
     */
    public static NoiseSettings caves() {
        sayys.depthsupdate.core.HeightContext ctx = sayys.depthsupdate.core.HeightManager.getMaxContext();
        // Caves use a shorter height: 192 out of vanilla's 384, so scale proportionally
        int caveHeight = Math.min(ctx.totalHeight(), (ctx.totalHeight() * 192) / 384);
        caveHeight = (caveHeight >> 4) << 4; // round down to multiple of 16
        if (caveHeight < 16) caveHeight = 16;
        return create(ctx.minY(), caveHeight, 1, 2);
    }

    /** @deprecated Use {@link #overworld()} instead for dynamic height config support. */
    @Deprecated
    public static final NoiseSettings OVERWORLD = create(-64, 384, 1, 2);
    /** @deprecated Use {@link #caves()} instead for dynamic height config support. */
    @Deprecated
    public static final NoiseSettings CAVES = create(-64, 192, 1, 2);

    private final int minY;
    private final int height;
    private final int noiseSizeHorizontal;
    private final int noiseSizeVertical;

    public NoiseSettings(int minY, int height, int noiseSizeHorizontal, int noiseSizeVertical) {
        this.minY = minY;
        this.height = height;
        this.noiseSizeHorizontal = noiseSizeHorizontal;
        this.noiseSizeVertical = noiseSizeVertical;
    }

    public static NoiseSettings create(int minY, int height, int noiseSizeHorizontal, int noiseSizeVertical) {
        if (minY + height > 2048) {
            throw new IllegalStateException("min_y + height cannot be higher than 2048");
        }
        if (height % 16 != 0) {
            throw new IllegalStateException("height has to be a multiple of 16");
        }
        if (minY % 16 != 0) {
            throw new IllegalStateException("min_y has to be a multiple of 16");
        }
        return new NoiseSettings(minY, height, noiseSizeHorizontal, noiseSizeVertical);
    }

    /** Cell height in blocks: noiseSizeVertical * 4 */
    public int getCellHeight() {
        return this.noiseSizeVertical * 4;
    }

    /** Cell width in blocks: noiseSizeHorizontal * 4 */
    public int getCellWidth() {
        return this.noiseSizeHorizontal * 4;
    }

    public int minY() { return minY; }
    public int height() { return height; }
    public int noiseSizeHorizontal() { return noiseSizeHorizontal; }
    public int noiseSizeVertical() { return noiseSizeVertical; }
}
