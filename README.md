# HGJabba

This is a functional and intuitive Hunger Games Plugin for your own Minecraft Spigot Server to play together with your friends without much effort.

**Features:**
- Players can only play when all players are online, else the game is paused
- Completely variable teams with custom team names and colors
- Sidebar scoreboard to display current game information
- Simple commands to create the game
- Unsuitable generated worlds (e.g. too much ocean) are discarded
- Configurable shrinking worldborder in overworld and nether
- First-person spectator view for died players to watch their teammates
- Free spectator view for died players whose team has been eliminated

## Download

The plugin can be downloaded directly [here](https://github.com/bluntphenomena/HGJabba/releases/download/v1.0/HGJabba-1.0-SNAPSHOT.jar)

## Build Instructions

You can also build the plugin yourself. Git, Java and Maven are necessary for building.

Build Spigot for 1.8.8 according to instructions from https://www.spigotmc.org/wiki/buildtools/ because it creates the required dependencies in your local maven repository.

Then clone this repository and build it with maven.

```bash
$ git clone https://github.com/bluntphenomena/HGJabba.git
$ cd HGJabba
$ mvn package
```

After a successful build the plugin file is located at `./target/HGJabba-1.0-SNAPSHOT.jar`

## How to use

You must be `/op` on the server to have permission for all commands!
```
List of available commands:

/hg help - displays help for /hg
/hg start - starts the game
/hg team create - creates a new team
/hg team list - lists all teams
/hg team delete - deletes a new team
/hg player add - adds a player to a team
/hg player list - lists all player and their team
/hg player remove - removes a player from team
```

Starting a new game is pretty easy. `/hg team create <teamname> [teamcolor]` creates a new team which you can add players with `/hg player add <player> <team>`. If everything is ready, the game can be started with `/hg start`.

**Important notes:**
- During the game building or destroying a nether portal is forbidden
- Worldborder settings can be configured in the plugins config.yml
- It's recommended to shut down the server properly, otherwise important game data may not be saved
