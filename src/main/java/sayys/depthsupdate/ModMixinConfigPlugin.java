package sayys.depthsupdate;

import java.util.List;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import net.minecraft.launchwrapper.Launch;

public class ModMixinConfigPlugin implements IMixinConfigPlugin {
    private static final boolean OPTIFINE_LOADED = detectOptiFine();
    private static final boolean NOTHIRIUM_LOADED = detectNothirium();
    private static final boolean CELERITAS_LOADED = detectCeleritas();

    private static boolean detectOptiFine() {
        return Launch.classLoader.isClassExist("optifine.OptiFineForgeTweaker");
    }

    private static boolean detectNothirium() {
        return Launch.classLoader.isClassExist("meldexun.nothirium.mc.Nothirium");
    }

    private static boolean detectCeleritas() {
        return Launch.classLoader.isClassExist("org.taumc.celeritas.CeleritasVintage");
    }

    @Override
    public void onLoad(String s) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    /**
     * An example of mod mixin
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
        if (mixinClassName.contains(".optifine.")) {
            return OPTIFINE_LOADED;
        }

        if (mixinClassName.contains(".mod.nothirium.")) {
            return NOTHIRIUM_LOADED;
        }

        if (mixinClassName.contains(".mod.celeritas.")) {
            return CELERITAS_LOADED;
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
