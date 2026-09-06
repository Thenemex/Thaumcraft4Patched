package thaumcraft4patched.core;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.TransformerExclusions({
        "thaumcraft4patched.core",
        "thaumcraft4patched.asm"
})
@IFMLLoadingPlugin.SortingIndex(1001) // Used by most coremods
public class TCPatchCorePlugin implements IFMLLoadingPlugin {

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{
                "thaumcraft4patched.asm.GolemLumberTransformer",
                "thaumcraft4patched.asm.ExcavationFocusTransformer",
                "thaumcraft4patched.asm.EldritchRingLakeProtectionTransformer",
                "thaumcraft4patched.asm.ThaumonomiconRecipeLightingTransformer",
                "thaumcraft4patched.asm.HarvestLevelConfigTransformer",
                "thaumcraft4patched.asm.EnderZooExcavationCompatibilityTransformer",
                "thaumcraft4patched.asm.WitcheryRaiseLandTransformer",
                "thaumcraft4patched.asm.AngelicaSignEditorTransformer",
                "thaumcraft4patched.asm.MagicCookiesDarkShrineTransformer",
                "thaumcraft4patched.asm.MagicCookiesGolemDecorationTransformer",
                "thaumcraft4patched.asm.OblivionJarHarvestTransformer",
                "thaumcraft4patched.asm.MineTweakerCommandRollbackTransformer",
                "thaumcraft4patched.asm.GravestoneDeathNoteTransformer",
                "thaumcraft4patched.asm.ThaumicTinkererElementalFireTransformer",
                "thaumcraft4patched.asm.VillagerTradeStackMatchingTransformer"};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {}

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
