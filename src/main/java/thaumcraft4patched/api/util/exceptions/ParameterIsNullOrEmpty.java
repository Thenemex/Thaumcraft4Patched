package thaumcraft4patched.api.util.exceptions;

public class ParameterIsNullOrEmpty extends TC4PatchedException {

    public ParameterIsNullOrEmpty() {
        super("One of the parameters is null or empty.");
    }
}
