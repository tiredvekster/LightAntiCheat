# F.A.Q.

## How can I log violations or/and punishments in a Discord channel?
Check out `discord-webhook` config selection. You don't need to install additional plugins.

## Why are my players getting false punishments?
1. LAC detects mods like InventorySorter, SwingThroughGrass, etc. Every check is customizable and may be disabled. You can read descriptions of all the checks [here](CHECKS.md).
2. Some plugins that modify players' movement are not supported.
3. If you find any false positive and properly report this to me, I'll immediately fix it.

## Can I use this plugin only to check Bedrock players?
Every check is highly customizable, you can use a text editor (e.g. Notepad++) to replace `java: true` with `java: false` in `checks` selection of the config.

## What is the right way to install Geyser?
* If Geyser is installed on a proxy, you need to set `forward-player-ping` to `true`. Otherwise, it may cause false positives.
* If you want to make the detection a bit more accurate, you can install Floodgate so that the plugin can differentiate *Pocket Edition players* from *PC/Console Bedrock players*.

## Which plugins are compatible with the anticheat?
If a plugin modifies players' movement, fall damage, block interaction or combat, this can lead to false potisives.<br>
However, ViaVersion, Geyser, GSit, mcMMO, VeinMiner, AureliumSkills, ExecutableItems and EnchantsSquared are tested for compatibility.

## Is there an AntiXray check?
No, I recommend using Paper's anti-xray. This looks like the best option right now.<br>
Here is a setup guide: [https://docs.papermc.io/paper/anti-xray](https://docs.papermc.io/paper/anti-xray)

## Is this an open source project?
No. AntiCheat development takes a lot of testing. I don't mind sharing the code I wrote, but I don't want anyone to copy-paste checks I've been tuning for hours. If you have a certain reputation as a developer, I can provide the source code for non-commercial use.

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
