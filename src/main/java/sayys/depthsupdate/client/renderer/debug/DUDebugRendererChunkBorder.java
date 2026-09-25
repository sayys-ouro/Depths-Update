package sayys.depthsupdate.client.renderer.debug;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import sayys.depthsupdate.core.HeightContext;
import sayys.depthsupdate.core.HeightManager;

@SideOnly(Side.CLIENT)
public class DUDebugRendererChunkBorder {
    private DUDebugRendererChunkBorder() {}

    public static void render(Minecraft minecraft, float partialTicks) {
        HeightContext DUHeightContext = HeightManager.get(minecraft.world);

        int minY = DUHeightContext.minY();
        int maxY = DUHeightContext.maxY();

        EntityPlayer entityPlayer = minecraft.player;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        double interpX = entityPlayer.lastTickPosX + (entityPlayer.posX - entityPlayer.lastTickPosX) * partialTicks;
        double interpY = entityPlayer.lastTickPosY + (entityPlayer.posY - entityPlayer.lastTickPosY) * partialTicks;
        double interpZ = entityPlayer.lastTickPosZ + (entityPlayer.posZ - entityPlayer.lastTickPosZ) * partialTicks;

        double bottomY = minY - interpY;
        double topY = maxY - interpY;

        double chunkStartX = (entityPlayer.chunkCoordX << 4) - interpX;
        double chunkStartZ = (entityPlayer.chunkCoordZ << 4) - interpZ;

        GlStateManager.disableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.glLineWidth(1.0F);

        bufferBuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);

        for (int x = -16; x <= 32; x += 16) {
            for (int z = -16; z <= 32; z += 16) {
                bufferBuilder.pos(chunkStartX + x, bottomY, chunkStartZ + z).color(1.0F, 0.0F, 0.0F, 0.0F).endVertex();
                bufferBuilder.pos(chunkStartX + x, bottomY, chunkStartZ + z).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                bufferBuilder.pos(chunkStartX + x, topY, chunkStartZ + z).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
                bufferBuilder.pos(chunkStartX + x, topY, chunkStartZ + z).color(1.0F, 0.0F, 0.0F, 0.0F).endVertex();
            }
        }

        for (int x = 2; x < 16; x += 2) {
            bufferBuilder.pos(chunkStartX + x, bottomY, chunkStartZ).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
            bufferBuilder.pos(chunkStartX + x, bottomY, chunkStartZ).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            bufferBuilder.pos(chunkStartX + x, topY, chunkStartZ).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            bufferBuilder.pos(chunkStartX + x, topY, chunkStartZ).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();

            bufferBuilder.pos(chunkStartX + x, bottomY, chunkStartZ + 16.0).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
            bufferBuilder.pos(chunkStartX + x, bottomY, chunkStartZ + 16.0).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            bufferBuilder.pos(chunkStartX + x, topY, chunkStartZ + 16.0).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            bufferBuilder.pos(chunkStartX + x, topY, chunkStartZ + 16.0).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
        }

        for (int z = 2; z < 16; z += 2) {
            bufferBuilder.pos(chunkStartX, bottomY, chunkStartZ + z).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
            bufferBuilder.pos(chunkStartX, bottomY, chunkStartZ + z).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            bufferBuilder.pos(chunkStartX, topY, chunkStartZ + z).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            bufferBuilder.pos(chunkStartX, topY, chunkStartZ + z).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();

            bufferBuilder.pos(chunkStartX + 16.0, bottomY, chunkStartZ + z).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
            bufferBuilder.pos(chunkStartX + 16.0, bottomY, chunkStartZ + z).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            bufferBuilder.pos(chunkStartX + 16.0, topY, chunkStartZ + z).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            bufferBuilder.pos(chunkStartX + 16.0, topY, chunkStartZ + z).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
        }

        for (int y = minY; y <= maxY; y += 2) {
            double ringY = y - interpY;

            bufferBuilder.pos(chunkStartX, ringY, chunkStartZ).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
            bufferBuilder.pos(chunkStartX, ringY, chunkStartZ).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            bufferBuilder.pos(chunkStartX, ringY, chunkStartZ + 16.0).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            bufferBuilder.pos(chunkStartX + 16.0, ringY, chunkStartZ + 16.0).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            bufferBuilder.pos(chunkStartX + 16.0, ringY, chunkStartZ).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            bufferBuilder.pos(chunkStartX, ringY, chunkStartZ).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
            bufferBuilder.pos(chunkStartX, ringY, chunkStartZ).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
        }

        tessellator.draw();
        GlStateManager.glLineWidth(2.0F);
        bufferBuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);

        for (int x = 0; x <= 16; x += 16) {
            for (int l1 = 0; l1 <= 16; l1 += 16) {
                bufferBuilder.pos(chunkStartX + x, bottomY, chunkStartZ + l1).color(0.25F, 0.25F, 1.0F, 0.0F).endVertex();
                bufferBuilder.pos(chunkStartX + x, bottomY, chunkStartZ + l1).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
                bufferBuilder.pos(chunkStartX + x, topY, chunkStartZ + l1).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
                bufferBuilder.pos(chunkStartX + x, topY, chunkStartZ + l1).color(0.25F, 0.25F, 1.0F, 0.0F).endVertex();
            }
        }

        for (int y = minY; y <= maxY; y += 16) {
            double ringY = y - interpY;

            bufferBuilder.pos(chunkStartX, ringY, chunkStartZ).color(0.25F, 0.25F, 1.0F, 0.0F).endVertex();
            bufferBuilder.pos(chunkStartX, ringY, chunkStartZ).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
            bufferBuilder.pos(chunkStartX, ringY, chunkStartZ + 16.0).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
            bufferBuilder.pos(chunkStartX + 16.0, ringY, chunkStartZ + 16.0).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
            bufferBuilder.pos(chunkStartX + 16.0, ringY, chunkStartZ).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
            bufferBuilder.pos(chunkStartX, ringY, chunkStartZ).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
            bufferBuilder.pos(chunkStartX, ringY, chunkStartZ).color(0.25F, 0.25F, 1.0F, 0.0F).endVertex();
        }

        tessellator.draw();
        GlStateManager.glLineWidth(1.0F);
        GlStateManager.enableBlend();
        GlStateManager.enableTexture2D();
    }
}
