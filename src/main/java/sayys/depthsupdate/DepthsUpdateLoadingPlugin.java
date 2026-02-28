package sayys.depthsupdate;

import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.jetbrains.annotations.Nullable;

public class DepthsUpdateLoadingPlugin implements IFMLLoadingPlugin {
    @Override
    public @Nullable String[] getASMTransformerClass() {
        return null;
    }

    @Override
    public @Nullable String getModContainerClass() {
        return null;
    }

    @Override
    public @Nullable String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> map) {}

    @Override
    public @Nullable String getAccessTransformerClass() {
        return null;
    }
}
