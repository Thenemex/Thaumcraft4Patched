package thaumcraft4patched.core;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.TransformerExclusions({"thaumcraft4patched.core"})
@IFMLLoadingPlugin.SortingIndex(1001) // Used by most coremods
public class TCPatchCorePlugin implements IFMLLoadingPlugin {

    @Override public String[] getASMTransformerClass() {
        return new String[]{"thaumcraft4patched.core.TCPatchTransformer"};
    }

    @Override public String getModContainerClass() {
        return null;
    }

    @Override public String getSetupClass() {
        return null;
    }

    @Override public void injectData(Map<String, Object> data) {}

    @Override public String getAccessTransformerClass() {
        return null;
    }
}
