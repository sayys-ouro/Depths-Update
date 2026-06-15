package sayys.depthsupdate.mixin;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import sayys.depthsupdate.core.HeightContext;
import sayys.depthsupdate.core.HeightManager;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {
    @Shadow
    @Final
    private Minecraft mc;

    @Shadow
    private boolean cloudFog;

    @Shadow
    public float fogColorRed;

    @Shadow
    public float fogColorGreen;

    @Shadow
    public float fogColorBlue;

    @Shadow
    private float fogColor2;

    @Shadow
    private float fogColor1;

    @Shadow
    private float bossColorModifier;

    @Shadow
    private float bossColorModifierPrev;

    @Shadow
    private float getNightVisionBrightness(EntityLivingBase entitylivingbaseIn, float partialTicks) {
        return 0.0F;
    }

    @Inject(method = "updateFogColor(F)V", at = @At("HEAD"), cancellable = true)
    private void depthsupdate$updateFogColor(float partialTicks, CallbackInfo ci) {
        World world = this.mc.world;
        Entity entity = this.mc.getRenderViewEntity();
        HeightContext ctx = HeightManager.get(world);

        if (entity == null || !ctx.isExtended() || ctx.minY() >= 0) {
            return;
        }

        float renderDistanceBlend = 0.25F + 0.75F * this.mc.gameSettings.renderDistanceChunks / 32.0F;
        renderDistanceBlend = 1.0F - (float) Math.pow(renderDistanceBlend, 0.25);

        Vec3d skyColor = world.getSkyColor(this.mc.getRenderViewEntity(), partialTicks);
        float skyRed = (float) skyColor.x;
        float skyGreen = (float) skyColor.y;
        float skyBlue = (float) skyColor.z;

        Vec3d fogColor = world.getFogColor(partialTicks);
        this.fogColorRed = (float) fogColor.x;
        this.fogColorGreen = (float) fogColor.y;
        this.fogColorBlue = (float) fogColor.z;

        if (this.mc.gameSettings.renderDistanceChunks >= 4) {
            double sunDirection = MathHelper.sin(world.getCelestialAngleRadians(partialTicks)) > 0.0F ? -1.0 : 1.0;
            Vec3d sunriseDirection = new Vec3d(sunDirection, 0.0, 0.0);
            float sunriseBlend = (float) entity.getLook(partialTicks).dotProduct(sunriseDirection);

            if (sunriseBlend < 0.0F) {
                sunriseBlend = 0.0F;
            }

            if (sunriseBlend > 0.0F) {
                float[] sunriseColors = world.provider.calcSunriseSunsetColors(world.getCelestialAngle(partialTicks), partialTicks);

                if (sunriseColors != null) {
                    sunriseBlend *= sunriseColors[3];
                    this.fogColorRed = this.fogColorRed * (1.0F - sunriseBlend) + sunriseColors[0] * sunriseBlend;
                    this.fogColorGreen = this.fogColorGreen * (1.0F - sunriseBlend) + sunriseColors[1] * sunriseBlend;
                    this.fogColorBlue = this.fogColorBlue * (1.0F - sunriseBlend) + sunriseColors[2] * sunriseBlend;
                }
            }
        }

        this.fogColorRed = this.fogColorRed + (skyRed - this.fogColorRed) * renderDistanceBlend;
        this.fogColorGreen = this.fogColorGreen + (skyGreen - this.fogColorGreen) * renderDistanceBlend;
        this.fogColorBlue = this.fogColorBlue + (skyBlue - this.fogColorBlue) * renderDistanceBlend;

        float rainStrength = world.getRainStrength(partialTicks);

        if (rainStrength > 0.0F) {
            float redGreenRain = 1.0F - rainStrength * 0.5F;
            float blueRain = 1.0F - rainStrength * 0.4F;
            this.fogColorRed *= redGreenRain;
            this.fogColorGreen *= redGreenRain;
            this.fogColorBlue *= blueRain;
        }

        float thunderStrength = world.getThunderStrength(partialTicks);

        if (thunderStrength > 0.0F) {
            float thunderBlend = 1.0F - thunderStrength * 0.5F;
            this.fogColorRed *= thunderBlend;
            this.fogColorGreen *= thunderBlend;
            this.fogColorBlue *= thunderBlend;
        }

        IBlockState iblockstate = ActiveRenderInfo.getBlockStateAtEntityViewpoint(this.mc.world, entity, partialTicks);

        if (this.cloudFog) {
            Vec3d cloudColor = world.getCloudColour(partialTicks);
            this.fogColorRed = (float) cloudColor.x;
            this.fogColorGreen = (float) cloudColor.y;
            this.fogColorBlue = (float) cloudColor.z;
        } else {
            Vec3d viewport = ActiveRenderInfo.projectViewFromEntity(entity, partialTicks);
            BlockPos viewportPos = new BlockPos(viewport);
            IBlockState viewportState = this.mc.world.getBlockState(viewportPos);
            Vec3d inMaterialColor = viewportState.getBlock().getFogColor(this.mc.world, viewportPos, viewportState, entity,
                    new Vec3d(this.fogColorRed, this.fogColorGreen, this.fogColorBlue), partialTicks);
            this.fogColorRed = (float) inMaterialColor.x;
            this.fogColorGreen = (float) inMaterialColor.y;
            this.fogColorBlue = (float) inMaterialColor.z;
        }

        float fogBrightness = this.fogColor2 + (this.fogColor1 - this.fogColor2) * partialTicks;
        this.fogColorRed *= fogBrightness;
        this.fogColorGreen *= fogBrightness;
        this.fogColorBlue *= fogBrightness;

        double voidFogY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
        double voidFogFactor = (voidFogY - ctx.minY()) * world.provider.getVoidFogYFactor() + 1.0D;

        if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).isPotionActive(MobEffects.BLINDNESS)) {
            int duration = ((EntityLivingBase) entity).getActivePotionEffect(MobEffects.BLINDNESS).getDuration();

            if (duration < 20) {
                voidFogFactor *= 1.0F - duration / 20.0F;
            } else {
                voidFogFactor = 0.0;
            }
        }

        if (voidFogFactor < 1.0) {
            if (voidFogFactor < 0.0) {
                voidFogFactor = 0.0;
            }

            voidFogFactor *= voidFogFactor;
            this.fogColorRed = (float) (this.fogColorRed * voidFogFactor);
            this.fogColorGreen = (float) (this.fogColorGreen * voidFogFactor);
            this.fogColorBlue = (float) (this.fogColorBlue * voidFogFactor);
        }

        if (this.bossColorModifier > 0.0F) {
            float bossBlend = this.bossColorModifierPrev + (this.bossColorModifier - this.bossColorModifierPrev) * partialTicks;
            this.fogColorRed = this.fogColorRed * (1.0F - bossBlend) + this.fogColorRed * 0.7F * bossBlend;
            this.fogColorGreen = this.fogColorGreen * (1.0F - bossBlend) + this.fogColorGreen * 0.6F * bossBlend;
            this.fogColorBlue = this.fogColorBlue * (1.0F - bossBlend) + this.fogColorBlue * 0.6F * bossBlend;
        }

        if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).isPotionActive(MobEffects.NIGHT_VISION)) {
            float nightVision = this.getNightVisionBrightness((EntityLivingBase) entity, partialTicks);
            float inverseMax = 1.0F / this.fogColorRed;

            if (inverseMax > 1.0F / this.fogColorGreen) {
                inverseMax = 1.0F / this.fogColorGreen;
            }

            if (inverseMax > 1.0F / this.fogColorBlue) {
                inverseMax = 1.0F / this.fogColorBlue;
            }

            if (Float.isInfinite(inverseMax)) {
                inverseMax = Math.nextAfter(inverseMax, 0.0);
            }

            this.fogColorRed = this.fogColorRed * (1.0F - nightVision) + this.fogColorRed * inverseMax * nightVision;
            this.fogColorGreen = this.fogColorGreen * (1.0F - nightVision) + this.fogColorGreen * inverseMax * nightVision;
            this.fogColorBlue = this.fogColorBlue * (1.0F - nightVision) + this.fogColorBlue * inverseMax * nightVision;
        }

        if (this.mc.gameSettings.anaglyph) {
            float anaglyphRed = (this.fogColorRed * 30.0F + this.fogColorGreen * 59.0F + this.fogColorBlue * 11.0F) / 100.0F;
            float anaglyphGreen = (this.fogColorRed * 30.0F + this.fogColorGreen * 70.0F) / 100.0F;
            float anaglyphBlue = (this.fogColorRed * 30.0F + this.fogColorBlue * 70.0F) / 100.0F;
            this.fogColorRed = anaglyphRed;
            this.fogColorGreen = anaglyphGreen;
            this.fogColorBlue = anaglyphBlue;
        }

        EntityViewRenderEvent.FogColors event = new EntityViewRenderEvent.FogColors((EntityRenderer) (Object) this,
                entity, iblockstate, partialTicks, this.fogColorRed, this.fogColorGreen, this.fogColorBlue);
        MinecraftForge.EVENT_BUS.post(event);

        this.fogColorRed = event.getRed();
        this.fogColorGreen = event.getGreen();
        this.fogColorBlue = event.getBlue();

        GlStateManager.clearColor(this.fogColorRed, this.fogColorGreen, this.fogColorBlue, 0.0F);
        ci.cancel();
    }
}
