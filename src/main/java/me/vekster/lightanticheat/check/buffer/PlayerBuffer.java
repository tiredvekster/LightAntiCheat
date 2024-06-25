package me.vekster.lightanticheat.check.buffer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class PlayerBuffer {

    PlayerBuffer(boolean async) {
        updated = System.currentTimeMillis();
    }

    private final Map<String, PlayerVariable> PLAYER_VARIABLES = new HashMap<>();
    private final Map<String, PlayerVariable> ASYNC_PLAYER_VARIABLES = new ConcurrentHashMap<>();
    long updated;
    boolean async;

    public boolean containsKey(String key) {
        Map<String, PlayerVariable> playerVariableMap = !async ? PLAYER_VARIABLES : ASYNC_PLAYER_VARIABLES;
        updated = System.currentTimeMillis();
        return playerVariableMap.containsKey(key);
    }

    public PlayerVariable getOrDefault(String key, PlayerVariable playerVariable) {
        Map<String, PlayerVariable> playerVariableMap = !async ? PLAYER_VARIABLES : ASYNC_PLAYER_VARIABLES;
        updated = System.currentTimeMillis();
        return playerVariableMap.getOrDefault(key, playerVariable);
    }

    public void put(String key, PlayerVariable value) {
        Map<String, PlayerVariable> playerVariableMap = !async ? PLAYER_VARIABLES : ASYNC_PLAYER_VARIABLES;
        updated = System.currentTimeMillis();
        playerVariableMap.put(key, value);
    }

    static class PlayerVariable {
        public PlayerVariable(Object object) {
            this.object = object;
        }

        Object object;
    }

}
