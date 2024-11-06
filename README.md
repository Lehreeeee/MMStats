
# MMStats
**Note**: This project is currently still in development

MMStats is a Minecraft plugin that allows MythicMobs to have stats. The stats will be used to either make the mobs take more or less damage from players.
## Features
- When an entity is being tracked, a scoreboard is created using the UUID as the scoreboard name. All damage dealt by players to the entity is recorded, except for fire damage (e.g., from Fire Aspect and Flame enchantments).
- Automatically cleans up the list and existing scoreboards when the server is stopping.

## Dependencies

- **MythicMobs** [Link here](https://mythiccraft.io/index.php?resources/mythicmobs.1/)
- **MMOLib(MythicLib)** [Link here](https://www.spigotmc.org/resources/mmolib-mythiclib.90306/)

## Installation and setup

1. Download the MMStats plugin `.jar` file.
2. Place the `.jar` file into your server's `plugins` folder.
3. Restart the server to load the plugin.
4. Once the `config.yml` is generated in `MMStats` folder, set your mob's stats in the `config.yml`
5. `/mms reload` to reload the config
6. Damage dealt to your mobs should now be affected by the stats you have set.

## Commands Usage

- **/mms reload**: Reload the mob stats in `config.yml`
