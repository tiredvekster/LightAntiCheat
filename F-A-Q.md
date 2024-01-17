# F.A.Q.

## How do I enable alerts?
Chat notifications are disabled by default. You can enable not only punishment notifications, but also violation notifications in the config.<br>
```yml
alerts:
  broadcast-violations:
    enabled: true
    # ...
  broadcast-punishments:
    enabled: true
```

## How can I log violations or/and punishments in a Discord channel?
Check out `discord-webhook` config selection. You don't need to install additional plugins.

## Why are my players getting false punishments?
1. LAC detects mods like InventorySorter, SwingThroughGrass, etc. Every check is customizable and may be disabled. You can read descriptions of all the checks [here](CHECKS.md).
2. Some plugins that modify players' movement are not supported.
3. If you find any false positive and properly report this to me, I'll immediately fix it.

## Can I use this plugin only to check Bedrock players?
Every check is highly customizable, you can use a text editor (e.g. Notepad++) to replace `java: true` with `java: false` in `checks` selection of the config.

## Is this an open source project?
No. AntiCheat development takes a lot of testing. I don't mind sharing the code I wrote, but I don't want anyone to copy-paste checks I've been tuning for hours. If you have a certain reputation as a developer, I can provide the source code for non-commercial use.

## Does this plugin has a developer API?
Yes, you can use the API to make LAC compatible with your own plugins.<br>
API package: `me.vekster.liteanticheat.api`<br>
Cancellable events: `LACViolationEvent`, `LACPunishmentEvent`<br>
You can also get an instance of `LACApi` class to disable a check for a specific player.
