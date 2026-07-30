package sayys.depthsupdate;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class ModMixinConfigPlugin implements IMixinConfigPlugin {
    private static boolean isClassPresent(String className) {
        try {
            Class.forName(
                className,
                false,
                ModMixinConfigPlugin.class.getClassLoader()
            );

            return true;
        } catch (ClassNotFoundException | LinkageError e) {
            return false;
        }
    }

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
            return !isClassPresent("optifine.OptiFineForgeTweaker");
        }

        if (mixinClassName.contains(".optifine.")) {
            return isClassPresent("optifine.OptiFineForgeTweaker");
        }

        if (mixinClassName.contains(".mod.nothirium.")) {
            return isClassPresent("meldexun.nothirium.mc.Nothirium");
        }

        if (mixinClassName.contains(".mod.celeritas.")) {
            return isClassPresent("org.taumc.celeritas.CeleritasVintage");
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
