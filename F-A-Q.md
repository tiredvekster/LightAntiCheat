# F.A.Q.

## How can I log violations or/and punishments in a Discord channel?
Check out `discord-webhook` config selection. You don't need to install additional plugins.

## Can I use this plugin only to check Bedrock players?
Every check is highly customizable, you can use a text editor (e.g. Notepad++) to replace `java: true` with `java: false` in `checks` selection of the config.

## Why are my players getting false punishments?
1. LAC detects mods like InventorySorter, SwingThroughGrass, etc. Every check is customizable and may be disabled. You can read descriptions of all the checks [here](CHECKS.md).
2. Some plugins that modify players' movement are not supported. Check out [this](https://github.com/tiredvekster/LightAntiCheat/blob/main/F-A-Q.md#which-plugins-are-compatible-with-the-anticheat) for more information.
3. If you find a false positive and [properly report this to me](https://github.com/tiredvekster/LightAntiCheat/blob/main/F-A-Q.md#how-to-report-false-positives--console-errors), I'll fix it as soon as possible.

## Which plugins are compatible with the anticheat?
If a plugin modifies players' movement, fall damage, block interaction or combat, this can lead to false potisives.<br>
However, ViaVersion, Geyser, GSit, mcMMO, VeinMiner, AureliumSkills, ExecutableItems and EnchantsSquared are tested for compatibility.

## How to report false positives / console errors?
1. You can [create an Issue on GitHub](https://github.com/tiredvekster/LightAntiCheat/issues) or [message me on Discord](https://discord.gg/EQExhK8Ghm).
2. If you report a false positive, let me know how to reproduce it.
3. Please be respectful. Though this is an open source project, I'll try to address your issues myself, even if they are caused by incompatibilities with other plugins.

## What is the right way to install Geyser?
* If Geyser is installed on a proxy, you need to set `forward-player-ping` to `true`. Otherwise, it may cause false positives.
* If you want to make the detection a bit more accurate, you can install Floodgate so that the plugin can differentiate *Pocket Edition players* from *PC/Console players*.

## Is there an AntiXray check?
No, I recommend using Paper's anti-xray. This looks like the best option right now.<br>
Here is a setup guide: [https://docs.papermc.io/paper/anti-xray](https://docs.papermc.io/paper/anti-xray)

## Does this plugin has a developer API?
Yes, you can use the API to make Light compatible with your own plugins.<br>
API package: `me.vekster.lightanticheat.api`<br>
Cancellable events: `LACViolationEvent`, `LACPunishmentEvent`<br>
You can also get an instance of `LACApi` class to disable a check for a specific player.
```java
    //    Example: Bypassing detection (temporary)
    public static void disableDetection(Player player, long durationMils) {
        LACApi lacApi = LACApi.getInstance();
        for (String checkName : lacApi.getCheckNames(CheckType.ALL))
            lacApi.disableDetection(player, checkName, durationMils);
    }
```
