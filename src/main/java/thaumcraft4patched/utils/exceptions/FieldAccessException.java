package thaumcraft4patched.utils.exceptions;

import nemexlib.api.util.exceptions.TCRException;

public class FieldAccessException extends TCRException {
    public FieldAccessException(String fieldName) {
        super("Cannot get field value from ".concat(fieldName));
    }
}
