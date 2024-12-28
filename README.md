
# MMStats

MMStats is a Minecraft plugin that allows MythicMobs to have stats. The stats will be used to either make the mobs take more or less damage from players. Making this plugin because MythicMobs doesn't support mob stats that is compatible to MMOLib damage.

## Features
- Damage dealt to the mobs will be affected by the mob stats set in `config.yml`.
- Putting negative values in the stats will make the mobs take extra damage on the damage type.

## To be done
- Add more unique stats as below

![image](https://github.com/user-attachments/assets/c15633ca-5eb9-4bd5-abf1-78fc3cf24a9e)

- ~~Commands to temporarily adjust mob stats (Useful for support/debuff items.)~~ ✅


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
- **/mms help** - Show command usage.**
- **/mms reload** - Reload the mob stats in `config.yml`
- **/mms stat [Mob Name]** - Check the base stats of the mob. Mob name should be the internal name you put in `config.yml`.
- **/mms temp [Mob UUID] [Stat name] [Value] [Duration in ms] [Identifier]** - Apply temp stats to the mob, for either buff or debuff.
  
  Example:
  ```
  /mms temp 788307ef-9dbc-4ae3-a2bf-0f2e3ad3d2ea damage_reduction -20 5000 debuff
  
  (This reduces damage reduction of the mob by 20% for 5 seconds with
  "wind_effect" being it's identifier, identifier is used for force remove)
  ```

- **/mms removetemp [Mob UUID] [Stat name] [Identifier]** - Remove temp stat from a mob.
  
  Example:
  ```
  /mms removetemp 788307ef-9dbc-4ae3-a2bf-0f2e3ad3d2ea damage_reduction wind_effect
    
  (This removes temp damage reduction of the mob that is associated with
  the identifier "wind_effect")
  ```

  