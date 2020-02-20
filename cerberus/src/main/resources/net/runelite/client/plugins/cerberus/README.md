# READ ME: Cerberus Plugin
#### Made by [Im2be](https://github.com/Im2be)

## General:



This plugin was written to help you pray against the several attacks from Cerberus and its summoned souls. 
If you find a bug, have any questions or generally just wanna tell me how awesome this plugin is: feel free to DM me on discord @Im2be#1742.

Video: https://www.youtube.com/watch?v=4wj9kgIVIfY

### If this is your FIRST time learing cerb:

Make sure you know what Cerb and the plugin do. Read until the end and watch the video to know how to correctly use the plugin!

## How to install:

1. Install OpenOSRS (download the appropriate installer from https://github.com/open-osrs/launcher/releases)
2. Search for plugin 'Cerberus' in the External Plugin Manager
   - If you cannot find it, it may have been moved to [xKylee](https://github.com/xKylee)'s [repository](https://github.com/xKylee/plugins-release).
    Follow these steps and then search again: https://imgur.com/a/zQb7UoY
3. Install and enable the plugin in the Configuration-panel.

## Usage:

### [Upcoming Attacks](https://imgur.com/a/PlC2lbu)
The **upcoming attacks** panel shows you what attacks are coming next. 
The plugin can guess correctly 90% of the time since Cerberus has a static attack pattern (see below).
- There is a factor of randomness to the attacks since we cannot 100% know Cerb's HP for sure & there is some inherent randomness to the attacks (sometimes they just don't happen).


The number next to each attack, e.g. **7** in **GHOSTS (7)**, shows you what attack Cerb is on.


Below the icon, there may be some text similar to **GHO: +30hp**. This means this attack will switch to **GHOSTS** if you do ~30 more damage to Cerb.
In practice, this means you will want to keep Cerb above **400HP** until he has done attack 7. That way, attack 7 will be an **AUTO** instead of a devastating prayer drain by **GHOSTS** ([I do this in the video around 0:30](https://www.youtube.com/watch?v=4wj9kgIVIfY&t=27)).


### [Guitar Hero](https://imgur.com/a/9AkrobA)
'**Guitar Hero**'-mode is exactly what you think it means: By clicking your prayers when the descending boxes are about to hit your prayers, you will be praying correctly against Cerb without even having to think about it. It will prioritize praying correctly against **GHOSTS** above all other attacks. No more dying to Cerb!

## Cerberus Attack Patterns 
*Thanks to Gherkins and GeChallengeM*



### Normal Attack Weighting:
- Cerberus looks at your defence bonus against all 3 styles and uses the style that you're weakest against most commonly. 
- Since your mage defence will be lowest in any reasonable setup, she will always use her magic attack most commonly.
- When cerb does a magic attack, there is a slight chance that she performs a quick ranged attack one tick later.

### Special Attack Pattern:
- **TRIPLE** every 10 attacks (starting from 1st attack - 1, 11, 21, 31 etc.) - can occur from full HP
  - **TRIPLE** will always go from LEFT to RIGHT (**MAGIC** -> **RANGE** -> **MELEE**)
  - The **MELEE** attack will always be performed, even if you are out of range!
- **GHOSTS** every 7 attacks (starting from 7th attack - 7, 14, 21, 28 etc.) - can only occur below 400 HP
- **LAVA** every 5 attacks (starting from 5th attack - 5, 10, 15, 20 etc.) - can only occur below 200 HP
- If **TRIPLE** and **GHOSTS** overlap, **TRIPLE** has priority (e.g. attack #21)
- If Cerb's current HP makes it ineligible for **GHOSTS** or **LAVA**, the attack is replaced by a normal auto

### Rotation when Cerb is below 200HP

1) TRIPLE
2) normal
3) normal
4) normal
5) LAVA
6) normal
7) GHOSTS
8) normal
9) normal
10) LAVA
11) TRIPLE
12) normal
13) normal
14) GHOSTS
15) LAVA
16) normal
17) normal
18) normal
19) normal
20) LAVA
21) TRIPLE
22) normal
23) normal
24) normal
25) LAVA
26) normal
27) normal
28) GHOSTS
29) normal
30) LAVA
etc.

## Happy slaying!

Plugin made by http://github.com/Im2be

Thanks to **GeChallengeM** and **Gherkins** for info on the Special Attacks
