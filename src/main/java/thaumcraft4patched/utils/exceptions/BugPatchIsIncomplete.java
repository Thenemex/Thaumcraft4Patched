package thaumcraft4patched.utils.exceptions;

import nemexlib.api.util.exceptions.TCRException;

public class BugPatchIsIncomplete extends TCRException {

    public BugPatchIsIncomplete(String patchName, int patches, int totalPatches) {
        super(patchName + " is incomplete : only " + patches + " applied over " + totalPatches + ".");
    }
}
