package me.vekster.lightanticheat.version.identifier;

public enum LACVersion {

    V1_8(0),
    V1_9(1),
    V1_10(3),
    V1_11(4),
    V1_12(5),
    V1_13(6),
    V1_14(7),
    V1_15(8),
    V1_16(9),
    V1_17(10),
    V1_18(11),
    V1_19(12),
    V1_20(13);

    public final int number;

    LACVersion(int number) {
        this.number = number;
    }

    public boolean isOlderThan(LACVersion lacVersion) {
        return this.number < lacVersion.number;
    }

    public boolean isNewerThan(LACVersion lacVersion) {
        return this.number > lacVersion.number;
    }

    public boolean isOlderOrEqualsTo(LACVersion lacVersion) {
        return this.number <= lacVersion.number;
    }

    public boolean isNewerOrEqualsTo(LACVersion lacVersion) {
        return this.number >= lacVersion.number;
    }

}
