package thaumcraft4patched.api.util.exceptions;

import static thaumcraft4patched.Thaumcraft4Patched.modID;

public abstract class TC4PatchedException extends RuntimeException {

    public TC4PatchedException(String text) {
        super(modID.concat(" : ").concat(text));
    }
}
