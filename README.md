## Ferrlius Boids
![Minecraft_Forge1 20 1-Singleplayer2025-04-0919-45-22-ezgif com-optimize](https://github.com/user-attachments/assets/4fba1de0-7672-429f-81e5-b7b294cfcf3c)

A boids based configuration for flying mobs in Minecraft.


## How to use
right now mobs don't spawn with attribute on their own, you have to spawn mobs by yourself

`/summonboid <entity> <pos> <count>`

example: `/summonboid parrot ~ ~ ~ 24`

if you want to summon more than 24 mobs at once, you have to change vanilla maxEntityCramming gamerule so mobs don't die. 

`/gamerule maxEntityCramming 25` - default setting. this is when mobs start to take damage from cramming.

### !!! MORE MOBS YOU HAVE - MORE LAG YOU GET !!!
for me lag starts at ~1500 mobs


## me sory but
at this point, I don't have config, so to change parameters you have to make your own build. check `src/main/java/com/ferrlius/boids/ferboids/util/BoidsAssistanceHandler.java/BoidsAssistanceHandler`

parameters that I set up don't work on every mob. I've only tested it on parrots and allays, they should work?

## Download
[here](https://github.com/Ferrlius/Ferrlius-Boids/releases)

Forge 1.20.1 btw
