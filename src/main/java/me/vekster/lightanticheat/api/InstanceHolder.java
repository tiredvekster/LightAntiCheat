package me.vekster.lightanticheat.api;

public class InstanceHolder {

    private static LACApi api;

    public static void setApi(LACApi api) {
        InstanceHolder.api = api;
    }

    public static LACApi getApi() {
        return api;
    }

}
