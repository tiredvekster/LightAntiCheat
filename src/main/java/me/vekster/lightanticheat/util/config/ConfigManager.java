package me.vekster.lightanticheat.util.config;

import com.tchristofferson.configupdater.ConfigUpdater;
import me.vekster.lightanticheat.Main;
import me.vekster.lightanticheat.check.Check;
import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.CheckSetting;
import me.vekster.lightanticheat.util.config.placeholder.PlaceholderConvertor;
import me.vekster.lightanticheat.util.hook.plugin.FloodgateHook;
import me.vekster.lightanticheat.util.logger.LogType;
import me.vekster.lightanticheat.util.logger.Logger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;

public class ConfigManager extends PlaceholderConvertor {

    public static class Config {
        public static boolean enabled;

        public static class Messages {
            public static String prefix;
            public static boolean hexColorCodes;

            public static class CommandMessages {
                public static class Help {
                    public static String message;
                }

                public static class Reload {
                    public static String message;
                }

                public static class Alerts {
                    public static String toggledOnMessage;
                    public static String toggledOffMessage;
                }

                public static class Tps {
                    public static String message;
                }

                public static class Ping {
                    public static String message;

                    public static class ConnectionStability {
                        public static String high;
                        public static String medium;
                        public static String low;
                    }
                }

                public static class Client {
                    public static String message;
                }

                public static class Cps {
                    public static String message;
                }

                public static class Checks {
                    public static String message;
                }
            }

            public static class ErrorMessages {
                public static String noPermission;
                public static String invalidFormat;
            }

        }

        public static class Alerts {
            public static class BroadcastViolations {
                public static boolean enabled;
                public static String message;
                public static int cooldown;
                public static String onHover;
                public static String onClick;
            }

            public static class BroadcastPunishments {
                public static boolean enabled;
                public static String message;
                public static int cooldown;
                public static String onHover;
                public static String onClick;
            }

        }

        public static class Log {
            public static boolean enabled;
            public static String file;

            public static class LogViolations {
                public static boolean enabled;
                public static String message;
                public static int cooldown;
            }

            public static class LogPunishments {
                public static boolean enabled;
                public static String message;
                public static int cooldown;
            }
        }

        public static class DiscordWebhook {
            public static boolean enabled;

            public static class SendViolations {
                public static boolean enabled;
                public static String webhookUrl;
                public static String message;
                public static int cooldown;
            }

            public static class SendPunishments {
                public static boolean enabled;
                public static String webhookUrl;
                public static String message;
                public static int cooldown;
            }
        }

        public static class Permission {
            public static boolean perCheckBypassPermission;
            public static boolean disableAllBypassPermissions;
        }

        public static class Violation {
            public static class Reset {
                public static int resetInterval;
            }

            public static class Cache {
                public static boolean enabled;
                public static int cacheDuration;
            }
        }

        public static class LagProtection {
            public static int tickThreshold;
            public static int ignoreTimeOnJoin;
            public static int ignoreTimeOnTeleport;
            public static boolean preventEnteringIntoUnloadedChucks;
            public static boolean prioritizeAccuracy;
        }

        public static class GeyserHook {
            public static boolean enabled;

            public static class Floodgate {
                public static boolean enabled;
            }

            public static class UUID {
                public static boolean enabled;
            }

            public static class Prefix {
                public static boolean enabled;
                public static String prefixString;
            }
        }

        public static class UpdateChecker {
            public static boolean enabled;

            public static class Notification {
                public static class Console {
                    public static boolean enabled;
                    public static String message;
                }

                public static class OnJoin {
                    public static boolean enabled;
                    public static String message;
                    public static boolean requirePermission;
                }
            }
        }

        public static class Bstats {
            public static boolean enabled;
        }

        public static class Api {
            public static boolean enabled;
        }
    }

    public static void loadConfig() {
        Main instance = Main.getInstance();
        instance.saveDefaultConfig();
        FloodgateHook.loadFloodgateHook();

        try {
            ConfigUpdater.update(instance, "config.yml",
                    new File(instance.getDataFolder(), "config.yml"), Collections.emptyList());
        } catch (IOException e) {
            Logger.logConsole(LogType.ERROR, "(" + instance.getName() + ") config.yml is invalid! " +
                    "Something went wrong while updating the file! ");
        }

        FileConfiguration config = instance.getConfig();
        loadConfig(Config.class, Config.class, config);
    }

    private static void loadConfig(Class<?> aClass, final Class<?> configClass, final FileConfiguration config) {
        for (Class<?> innerClass : aClass.getClasses()) {
            loadConfig(innerClass, configClass, config);
        }
        for (Field field : aClass.getDeclaredFields()) {
            for (Method method : config.getClass().getMethods()) {
                if (!method.getName().startsWith("get"))
                    continue;
                if (!method.getReturnType().getName().equals(field.getType().getName()))
                    continue;
                if (method.getParameterCount() != 1)
                    continue;
                try {
                    String path = aClass.getName().replaceAll("\\$", ".") + "." + field.getName();
                    path = path.replace(configClass.getName().replace("$", "."), "").substring(1);
                    for (int i = path.length() - 1; i >= 0; i--) {
                        char c = path.charAt(i);
                        if (!Character.isUpperCase(c))
                            continue;
                        if (i == 0 || path.charAt(i - 1) == '.')
                            path = path.substring(0, i) + Character.toLowerCase(c) + path.substring(i + 1);
                        else
                            path = path.substring(0, i) + "-" + Character.toLowerCase(c) + path.substring(i + 1);
                    }
                    field.set(aClass, method.invoke(config, path));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    Logger.logConsole(LogType.ERROR, "(" + Main.getInstance().getName() + ") config.yml is invalid! " +
                            "Something went wrong while loading the file!");
                }
                break;
            }
        }
    }

    public static void reloadConfig() {
        Main instance = Main.getInstance();
        instance.reloadConfig();
        loadConfig();
        for (CheckName checkName : CheckName.values()) {
            CheckSetting checkSetting = Check.getCheckSetting(checkName);
            if (checkSetting == null) {
                Logger.logConsole(LogType.ERROR, "(" + instance.getName() + ") config.yml is invalid! " +
                        "Something went wrong while loading " + checkName.title + " settings!");
                continue;
            }
            loadCheck(checkSetting);
            Check.registerListener(checkName, Check.getListener(checkName));
        }
        Logger.logFile("");
    }

    public static CheckSetting loadCheck(CheckSetting checkSetting) {
        CheckName checkName = checkSetting.name;
        ConfigurationSection section = Main.getInstance().getConfig()
                .getConfigurationSection("checks" +
                        "." + checkName.type.name().toLowerCase() +
                        "." + checkName.group.toLowerCase() +
                        "." + checkName.group.toLowerCase() + "_" + checkName.check.toString().toLowerCase());
        if (section == null || section.getKeys(false).size() == 0) {
            Logger.logConsole(LogType.ERROR, "(" + Main.getInstance().getName() + ") config.yml is invalid! " +
                    "The config selection of " + checkName.title + " check is invalid!");
            return checkSetting;
        }
        checkSetting.enabled = section.getBoolean("enabled");
        checkSetting.punishable = section.getBoolean("punishment.punishable");
        checkSetting.punishmentVio = section.getInt("punishment.punishment-vio");
        checkSetting.minTps = section.getDouble("detection.min-tps");
        checkSetting.maxPing = section.getInt("detection.max-ping");
        checkSetting.detectJava = section.getBoolean("detection.java");
        checkSetting.detectBedrock = section.getBoolean("detection.bedrock");
        checkSetting.setback = section.getBoolean("setback.setback");
        checkSetting.setbackVio = section.getInt("setback.setback-vio");
        checkSetting.punishmentCommands = section.getStringList("punishment.commands");
        return checkSetting;
    }

}
