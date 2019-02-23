# HGJabba

This is a useful Hunger Games Plugin for Minecraft Spigot Servers.

Features:
- Players can only play when all players are online, else the game is paused
- Completely variable teams with team names and colors
- Sidebar scoreboard to display current game information
- Admin commands to control the game
- Unsuitable generated worlds (e.g. too much ocean) are discarded
- Shrinking worldborder in overworld and nether
- First-person spectator view for died players to watch their teammates
- Free spectator view for died players whose team has been eliminated

## Download

The plugin can be downloaded directly [here](https://github.com/bluntphenomena/HGJabba/releases/download/v1.0/HGJabba-1.0-SNAPSHOT.jar)

## Build Instructions

Git, Java and Maven are necessary for building.

Build Spigot for 1.8.8 according to instructions from https://www.spigotmc.org/wiki/buildtools/ because it creates the required dependencies in your local maven repository.

Then clone this repository and build it with maven.

```
$ git clone https://github.com/bluntphenomena/HGJabba.git
$ cd HGJabba
$ mvn package
```

After a successful build the plugin file is located at `./target/HGJabba-1.0-SNAPSHOT.jar`

