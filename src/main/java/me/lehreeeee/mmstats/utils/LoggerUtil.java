package me.lehreeeee.mmstats.utils;

import me.lehreeeee.mmstats.MMStats;

import java.util.logging.Logger;

public class LoggerUtil {
    private static final Logger logger = MMStats.getPlugin().getLogger();

    public static void info(String message) {
        logger.info(message);
    }

    public static void warning(String message) {
        logger.warning(message);
    }

    public static void severe(String message) {
        logger.severe(message);
    }

    public static void debug(String message) {
        if (MMStats.getPlugin().shouldPrintDebug()) {
            logger.info("[DEBUG] " + message);
        }
    }
}
