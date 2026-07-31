package sayys.depthsupdate.client;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import sayys.depthsupdate.Reference;
import sayys.depthsupdate.core.HeightContext;
import sayys.depthsupdate.core.HeightManager;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = Reference.MOD_ID)
public final class DebugOverlayHandler {
    private static final String OUTSIDE = "Outside of world...";

    private DebugOverlayHandler() {}

    @SubscribeEvent
    public static void onDebugText(RenderGameOverlayEvent.Text event) {
        List<String> left = event.getLeft();
        int index = left.indexOf(OUTSIDE);

        if (index < 0) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        World world = mc.world;
        Entity view = mc.getRenderViewEntity();

        if (world == null || view == null || !HeightManager.isExtended(world)) {
            return;
        }

        BlockPos pos = new BlockPos(view.posX, view.getEntityBoundingBox().minY, view.posZ);
        HeightContext ctx = HeightManager.get(world);

        if (pos.getY() < ctx.minY() || pos.getY() >= ctx.maxY() || !world.isBlockLoaded(pos)) {
            return;
        }

        Chunk chunk = world.getChunk(pos);

        if (chunk.isEmpty()) {
            return;
        }

        left.set(index, "Biome: " + chunk.getBiome(pos, world.getBiomeProvider()).getBiomeName());
        left.add(index + 1, "Light: " + chunk.getLightSubtracted(pos, 0)
                + " (" + chunk.getLightFor(EnumSkyBlock.SKY, pos)
                + " sky, " + chunk.getLightFor(EnumSkyBlock.BLOCK, pos) + " block)");

        DifficultyInstance difficulty = world.getDifficultyForLocation(pos);

        if (mc.isIntegratedServerRunning() && mc.getIntegratedServer() != null && mc.player != null) {
            EntityPlayerMP serverPlayer = mc.getIntegratedServer().getPlayerList().getPlayerByUUID(mc.player.getUniqueID());

            if (serverPlayer != null) {
                difficulty = serverPlayer.world.getDifficultyForLocation(new BlockPos(serverPlayer));
            }
        }

        left.add(index + 2, String.format("Local Difficulty: %.2f // %.2f (Day %d)",
                difficulty.getAdditionalDifficulty(),
                difficulty.getClampedAdditionalDifficulty(),
                world.getWorldTime() / 24000L));
    }
}
