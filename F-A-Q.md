# F.A.Q.

## How do I enable alerts?
Chat notifications are disabled by default. You can enable not only punishment notifications, but also violation notifications in the config.<br>
`debug.broadcast.violation.enabled: true`<br>
`debug.broadcast.punishment.enabled: true`

## How can I log violations or/and punishments in a Discord channel?
Check out `discord-webhook` config selection. You don't need to install additional plugins.

## Why are my players getting false punishments?
LAC detects mods like InventorySorter, SwingThroughGrass, etc. Every check is customizable and may by disabled. You can read descriptions of all the checks [here](CHECKS.md). If you find any false positive and properly report this to me, I'll immediately fix it.

## Can I use this plugin only to check Bedrock players?
Every check is highly customizable, you can use a text editor (e.g. Notepad++) to replace `java: true` with `java: false` in `checks` selection of config.

## Is this an open source project?
No. AntiCheat development takes a lot of testing. I don't mind the sharing code I wrote, but I don't want anyone to copy-paste checks I've been tuning for hours. If you have a certain reputation as a developer, I can provide the source code for non-commercial use.

## Does this plugin has a developer API?
Yes, you can use the API to make LAC compatible with your own plugins.<br>
API package: `me.vekster.liteanticheat.api`<br>
Cancellable events: `LACViolationEvent`, `LACPunishmentEvent`<br>
You can also get an instance of the `LACApi` class to access more methods.
