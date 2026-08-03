package sayys.depthsupdate.util;

/**
 * A block reference from config, optionally pinning one metadata variant.
 * Accepts {@code modid:name}, {@code modid:name@meta} and {@code modid:name:meta}.
 *
 * Pure syntax: whether the block exists and whether it accepts the metadata is
 * {@link BlockUtils#parseBlockState} to decide.
 */
public record BlockSpec(String name, int meta) {
    public static final int NO_META = -1;

    public boolean hasMeta() {
        return this.meta != NO_META;
    }

    public static BlockSpec parse(String spec) {
        String trimmed = spec == null ? "" : spec.trim();
        int separator = trimmed.lastIndexOf('@');

        if (separator < 0 && trimmed.indexOf(':') != trimmed.lastIndexOf(':')) {
            separator = trimmed.lastIndexOf(':');
        }

        if (separator <= 0 || separator == trimmed.length() - 1) {
            return new BlockSpec(trimmed, NO_META);
        }

        int meta;

        try {
            meta = Integer.parseInt(trimmed.substring(separator + 1));
        } catch (NumberFormatException notMetadata) {
            return new BlockSpec(trimmed, NO_META);
        }

        if (meta < 0) {
            return new BlockSpec(trimmed, NO_META);
        }

        return new BlockSpec(trimmed.substring(0, separator), meta);
    }
}
