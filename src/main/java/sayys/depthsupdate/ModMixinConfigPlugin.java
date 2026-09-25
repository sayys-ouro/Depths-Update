package sayys.depthsupdate;

import java.util.List;
import java.util.Set;

import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class ModMixinConfigPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String s) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    /**
     * The {@link org.spongepowered.asm.mixin.MixinEnvironment.Phase#MOD} allow the
     * mixins being processed after modlist building
     * Which allow calling {@link Loader#isModLoaded(String)}
     *
     * @param targetClassName Not important unless you are writing multi-target
     *                        mixin
     * @param mixinClassName  The full mixin class name. Filtering with group name
     *                        is the easiest solution here.
     * @return If the mixin should apply
     */
    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.endsWith(".MixinRenderGlobalChunkOffset")) {
            return !Launch.classLoader.isClassExist("optifine.OptiFineForgeTweaker");
        }

        if (mixinClassName.contains(".optifine.")) {
            return Launch.classLoader.isClassExist("optifine.OptiFineForgeTweaker");
        }

        if (mixinClassName.contains(".mod.nothirium.")) {
            return Launch.classLoader.isClassExist("meldexun.nothirium.mc.Nothirium");
        }

        if (mixinClassName.contains(".mod.celeritas.")) {
            return Launch.classLoader.isClassExist("org.taumc.celeritas.CeleritasVintage");
        }

        if (mixinClassName.contains(".mod.rltweaker.")) {
            return Launch.classLoader.isClassExist("com.charles445.rltweaker.RLTweaker");
        }

        return true;
    }

    @Override
    public void acceptTargets(Set<String> set, Set<String> set1) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {}

    @Override
    public void postApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {}
}
