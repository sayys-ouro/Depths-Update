package sayys.depthsupdate.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import sayys.depthsupdate.Reference;
import sayys.depthsupdate.item.ItemSpyglass;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = Reference.MOD_ID)
public class SpyglassClientHandler {
    private static final ResourceLocation SCOPE_LOCATION = new ResourceLocation(Reference.MOD_ID, "textures/misc/spyglass_scope.png");
    private static float fovModifier = 1.0F;
    private static float prevFovModifier = 1.0F;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        prevFovModifier = fovModifier;
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;

        float target = 1.0F;

        if (player != null && player.isHandActive() && player.getActiveItemStack().getItem() instanceof ItemSpyglass) {
            target = 0.1F;
        }

        fovModifier += (target - fovModifier) * 0.5F;
    }

    @SubscribeEvent
    public static void onFOVUpdate(EntityViewRenderEvent.FOVModifier event) {
        float lerpedModifier = prevFovModifier + (fovModifier - prevFovModifier) * (float)event.getRenderPartialTicks();
        event.setFOV(event.getFOV() * lerpedModifier);
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;

        if (player != null && player.isHandActive() && player.getActiveItemStack().getItem() instanceof ItemSpyglass) {
            if (mc.gameSettings.thirdPersonView == 0) {
                renderSpyglassScope(event.getResolution().getScaledWidth(), event.getResolution().getScaledHeight());
            }
        }
    }

    private static void renderSpyglassScope(int width, int height) {
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        Minecraft.getMinecraft().getTextureManager().bindTexture(SCOPE_LOCATION);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        int f = Math.min(width, height);
        int h = f;
        int w = f;
        int x = (width - f) / 2;
        int y = (height - f) / 2;

        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos((double)x, (double)(y + h), -90.0D).tex(0.0D, 1.0D).endVertex();
        bufferbuilder.pos((double)(x + w), (double)(y + h), -90.0D).tex(1.0D, 1.0D).endVertex();
        bufferbuilder.pos((double)(x + w), (double)y, -90.0D).tex(1.0D, 0.0D).endVertex();
        bufferbuilder.pos((double)x, (double)y, -90.0D).tex(0.0D, 0.0D).endVertex();
        tessellator.draw();

        GlStateManager.disableTexture2D();
        GlStateManager.color(0, 0, 0, 1);

        if (x > 0) {
            drawRect(0, 0, (int)x, height);
            drawRect((int)(x + w), 0, width, height);
        }

        if (y > 0) {
            drawRect(0, 0, width, (int)y);
            drawRect(0, (int)(y + h), width, height);
        }

        GlStateManager.enableTexture2D();
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void drawRect(int left, int top, int right, int bottom) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos((double)left, (double)bottom, -90.0D).endVertex();
        bufferbuilder.pos((double)right, (double)bottom, -90.0D).endVertex();
        bufferbuilder.pos((double)right, (double)top, -90.0D).endVertex();
        bufferbuilder.pos((double)left, (double)top, -90.0D).endVertex();
        tessellator.draw();
    }
}
