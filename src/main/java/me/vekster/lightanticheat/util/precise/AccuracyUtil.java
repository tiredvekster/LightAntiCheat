package me.vekster.lightanticheat.util.precise;

import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.CheckSetting;
import me.vekster.lightanticheat.check.buffer.Buffer;
import me.vekster.lightanticheat.version.identifier.LACVersion;
import me.vekster.lightanticheat.version.identifier.VerIdentifier;

public class AccuracyUtil {

    public static boolean isViolationCancel(CheckSetting checkSetting, Buffer buffer) {
        CheckName checkName = checkSetting.name;
        if (VerIdentifier.getVersion().isNewerThan(LACVersion.V1_8)) {
            if (checkName == CheckName.FLIGHT_A && getRecentViolations(buffer) <= 5)
                return true;
            if (checkName == CheckName.FLIGHT_C && getRecentViolations(buffer) <= 3)
                return true;
            if (checkName == CheckName.SPEED_B && getRecentViolations(buffer) <= 3)
                return true;
            if (checkName == CheckName.SPEED_C && getRecentViolations(buffer) <= 2)
                return true;
        } else {
            if (checkName == CheckName.FLIGHT_A && getRecentViolations(buffer) <= 7)
                return true;
            if (checkName == CheckName.FLIGHT_C && getRecentViolations(buffer) <= 4)
                return true;
            if (checkName == CheckName.SPEED_B && getRecentViolations(buffer) <= 4)
                return true;
            if (checkName == CheckName.SPEED_C && getRecentViolations(buffer) <= 3)
                return true;
            if (checkName == CheckName.SPEED_A && getRecentViolations(buffer) <= 2)
                return true;
            if (checkName == CheckName.KILLAURA_B && getRecentViolations(buffer) <= 1)
                return true;
        }
        return false;
    }

    private static int getRecentViolations(Buffer buffer) {
        buffer.put("accuracyUtilViolations", buffer.getInt("accuracyUtilViolations") + 1);
        if (System.currentTimeMillis() - buffer.getLong("accuracyUtilTime") > 5000) {
            buffer.put("accuracyUtilTime", System.currentTimeMillis());
            buffer.put("accuracyUtilViolations", 1);
        }
        return buffer.getInt("accuracyUtilViolations");
    }

}
