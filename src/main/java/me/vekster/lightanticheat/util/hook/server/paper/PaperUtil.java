package me.vekster.lightanticheat.util.hook.server.paper;

public class PaperUtil {

    private static boolean paper;

    static {
        try {
            Class.forName("com.destroystokyo.paper.ParticleBuilder");
            paper = true;
        } catch (ClassNotFoundException ignored) {
        }
    }

    public static boolean isPaper() {
        return paper;
    }

}
