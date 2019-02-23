# HGJabba

## Build Instructions

Git, Java and Maven are necessary for building.

Build Spigot for 1.8.8 according to instructions from https://www.spigotmc.org/wiki/buildtools/
It creates the required dependencies in your local maven repository

Then clone this repository and build it with maven.

```
$ git clone https://github.com/bluntphenomena/HGJabba.git
$ cd HGJabba
$ mvn package
```

After a successful build the plugin file is located at `./target/HGJabba-1.0-SNAPSHOT.jar`

