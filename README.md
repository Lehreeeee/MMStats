
# MMStats

MMStats is a Minecraft plugin that allows MythicMobs to have stats. The stats will be used to either make the mobs take more or less damage from players. Making this plugin because MythicMobs doesn't support mob stats that is compatible to MMOLib damage.

## Features
- Damage dealt to the mobs will be affected by the mob stats set in `config.yml`.
- Putting negative values in the stats will make the mobs take extra damage on the damage type.

## To be done
- Add more unique stats as below

![image](https://github.com/user-attachments/assets/c15633ca-5eb9-4bd5-abf1-78fc3cf24a9e)

- Commands to temporarily adjust mob stats (Useful for support/debuff items.)


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
