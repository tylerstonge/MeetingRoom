# Meeting Room

## Description
A simple plugin to allow private chat to take place on a server. Players standing on the meeting room platform will be able to communicate with each other in private while still hearing the server chat.

## Usage
To use this plugin create a floor of a single material (Default: Obsidian) where you would like to have the "meeting room." Then right click on the floor with the configured catalyst (Default: Diamond) in your hand to create the room. Now any chat sent from a player in (on) the meeting room will be redirected to only the players also in (on) the meeting room.

To rename your room from the default "Room" create a sign with line 1 as **[meetingroom]** and on the 2nd line put the name of your room. The sign can be removed after.

## Configuration
material: OBSIDIAN    (Floor material)
catalyst: DIAMOND    (Item to use to initialize meeting room)

The materials must be the properly formatted name from the material enum list, located [here](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html)
