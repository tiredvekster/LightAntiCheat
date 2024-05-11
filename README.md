# LightAntiCheat
A lightweight and customizable anticheat, designed to detect common hacks.<br>
Supported MC versions: 1.8-1.20.6. Folia, Geyser and [other plugins](F-A-Q.md) are also compatible.

## Links
* [SpigotMC page](https://www.spigotmc.org/resources/lightanticheat.112053/)
* [PaperMC page](https://hangar.papermc.io/Vekster/LightAntiCheat)
* [Modrinth page](https://modrinth.com/plugin/lightanticheat)
* [Discord server](https://discord.gg/EQExhK8Ghm)
* List of checks: [CHECKS.md](CHECKS.md)
* Frequently asked questions: [F-A-Q.md](F-A-Q.md)

## Support
The fastest way to get help with Light is through messaging me on [Discord](https://discord.gg/EQExhK8Ghm).

## Compatibility
Some unpopular Spigot's forks might or might not be compatible.<br>
Spigot, Paper, Tuinity, Purpur, Pufferfish and Folia are tested for compatibility.

## Documentation
### Features:
* Accurate detection with rare false positives
* Multithreaded, optimized and stable code
* No additional libraries or plugins are required
* Most of the checks are compatible with Geyser
* Light can be installed on a server that runs Folia
* Supports Discord webhook for notifications
* Provides convenient utils for moderators

### Commands:
* /light reload - reloads the plugin configuration
* /light teleport - teleports to the flag location
* /light checks - shows all the enabled and disabled checks
* /light alerts - toggles alerts on and off
* /light tps - shows the TPS calculated by this plugin
* /light client - shows player's client brand
* /light ping - shows player's ping and connection stablity
* /light cps - shows player's CPS

### Permissions:
* lightanticheat.checks - use /light checks command
* lightanticheat.reload - use /light reload command
* lightanticheat.alerts - grants all the alert permissions
* lightanticheat.alerts.notify - see debug messages
* lightanticheat.alerts.toggle - use /light toggle command
* lightanticheat.alerts.teleport - use /light teleport command
* lightanticheat.tps - use /light tps command
* lightanticheat.ping - use /light ping command
* lightanticheat.client - use /light client command
* lightanticheat.cps - use /light cps command
* lightanticheat.bypass - bypass the detection<br>
Specific bypass permissions can be enabled in the config
* lightanticheat.* - all the above
