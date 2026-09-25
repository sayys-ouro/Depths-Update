package sayys.depthsupdate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import sayys.depthsupdate.util.BlockSpec;

class BlockSpecTest {
    private static void assertSpec(String input, String name, int meta) {
        BlockSpec parsed = BlockSpec.parse(input);

        Assertions.assertEquals(name, parsed.name(), () -> "name of \"%s\"".formatted(input));
        Assertions.assertEquals(meta, parsed.meta(), () -> "meta of \"%s\"".formatted(input));
    }

    @Test
    void plainNamesKeepTheirDefaultState() {
        assertSpec("depthsupdate:deepslate", "depthsupdate:deepslate", BlockSpec.NO_META);
        assertSpec("minecraft:stone", "minecraft:stone", BlockSpec.NO_META);
        assertSpec("stone", "stone", BlockSpec.NO_META);
    }

    @Test
    void bothMetadataSeparatorsParse() {
        assertSpec("minecraft:stone@1", "minecraft:stone", 1);
        assertSpec("minecraft:stone:1", "minecraft:stone", 1);
        assertSpec("stone@3", "stone", 3);
        assertSpec("  minecraft:stone@2  ", "minecraft:stone", 2);
        assertSpec("minecraft:stone@0", "minecraft:stone", 0);
    }

    @Test
    void malformedSuffixesAreNotTreatedAsMetadata() {
        assertSpec("minecraft:stone@abc", "minecraft:stone@abc", BlockSpec.NO_META);
        assertSpec("minecraft:stone@", "minecraft:stone@", BlockSpec.NO_META);
        assertSpec("minecraft:stone@-1", "minecraft:stone@-1", BlockSpec.NO_META);
        assertSpec("@5", "@5", BlockSpec.NO_META);
        assertSpec("", "", BlockSpec.NO_META);
        assertSpec(null, "", BlockSpec.NO_META);
    }

    @Test
    void outOfRangeMetadataReachesTheBlock() {
        assertSpec("minecraft:stone@99", "minecraft:stone", 99);
    }
}
