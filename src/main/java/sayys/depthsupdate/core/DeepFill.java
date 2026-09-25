package sayys.depthsupdate.core;

import java.util.Random;

import net.minecraft.block.state.IBlockState;

import sayys.depthsupdate.DepthsUpdateConfig;

public final class DeepFill {
    private DeepFill() {}

    public static IBlockState bandAt(int y, int minY, Random rand,
            IBlockState bedrock, IBlockState deepslate, IBlockState stone) {
        int deepslateMaxY = DepthsUpdateConfig.deepslateMaxY;
        int transitionRange = DepthsUpdateConfig.deepslateTransitionRange;

        if (y <= minY + rand.nextInt(5)) {
            return bedrock;
        }

        if (y <= deepslateMaxY - transitionRange) {
            return deepslate;
        }

        if (y < deepslateMaxY) {
            if (rand.nextDouble() < (double) (deepslateMaxY - y) / (double) transitionRange) {
                return deepslate;
            }

            return y < 0 ? stone : null;
        }

        return y < 0 ? stone : null;
    }
}
