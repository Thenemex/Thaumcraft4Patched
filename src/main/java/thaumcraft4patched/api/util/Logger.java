package thaumcraft4patched.api.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

import static thaumcraft4patched.Thaumcraft4Patched.modID;

@SuppressWarnings("unused")
public class Logger {

    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(modID);

    public static void logInfo(Object ... messages) {
        String message = "";
        for (Object o : messages)
            message = (o != null) ? message.concat(o.toString()) : "null";
        logger.log(Level.INFO, modID.concat(" : ").concat(message));
    }
    public static void logError(Object message) {
        logger.log(Level.ERROR, modID.concat(" : ").concat(message.toString()));
    }
}
