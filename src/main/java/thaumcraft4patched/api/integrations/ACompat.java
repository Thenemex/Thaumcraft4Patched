package thaumcraft4patched.api.integrations;

import cpw.mods.fml.common.Loader;
import thaumcraft4patched.api.util.Logger;
import thaumcraft4patched.api.util.exceptions.ParameterIsNullOrEmpty;

import java.util.Objects;

public abstract class ACompat {

    protected final String mod;

    public ACompat(String mod) {
        if (mod == null) throw new ParameterIsNullOrEmpty();
        this.mod = mod;
        init();
        Logger.logInfo("Successfully loaded integration for mod : ".concat(mod));
    }

    protected void init() {
        loadIntegration();
    }

    public abstract void loadIntegration();

    public static boolean modIsLoaded(String mod, boolean config) {
        return Loader.isModLoaded(mod) && config;
    }

    @Override
    public String toString() {
        return "Compat:".concat(mod);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ACompat)) return false;
        ACompat aCompat = (ACompat) o;
        return Objects.equals(mod, aCompat.mod);
    }
    @Override
    public int hashCode() {
        return Objects.hashCode(mod);
    }
}
