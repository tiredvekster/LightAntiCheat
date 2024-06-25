package me.vekster.lightanticheat.check;

import java.util.List;

public class CheckSetting {

    public CheckSetting(CheckName name) {
        this.name = name;
    }

    public CheckName name;
    public boolean enabled;
    public boolean punishable;
    public int punishmentVio;
    public double minTps;
    public int maxPing;
    public boolean detectJava;
    public boolean detectBedrock;
    public boolean setback;
    public int setbackVio;
    public List<String> punishmentCommands;

}
