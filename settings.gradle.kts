/*
 * Copyright (c) 2019 Owain van Brakel <https:github.com/Owain94>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

rootProject.name = "xKylee Plugins"

include(":alchemicalhydra")
include(":aoewarnings")
include(":betterequipmentinspector")
include(":blackjack")
include(":castlewarsextended")
include(":cerberus")
include(":clanmanmode")
include(":coxhelper")
include(":dagannothkings")
include(":demonicgorilla")
include(":dropparty")
include(":dynamicmaxhit")
include(":effecttimers")
include(":entityfocuspile")
include(":entityhiderextended")
include(":environmentaid")
include(":fightcave")
include(":fightcavespawnrotation")
include(":gauntlet")
include(":godbook")
include(":grotesqueguardians")
include(":hallowedsepulchre")
include(":hideprayers")
include(":hideunder")
include(":hotkeytowalk")
include(":hydra")
include(":inferno")
include(":kalphitequeen")
include(":lizardmenshaman")
include(":lootassist")
include(":menuentryswapperextended")
include(":multiindicators")
include(":neverlogout")
include(":nightmare")
include(":npcstatus")
include(":pileindicators")
include(":playerattacktimer")
include(":playerscouter")
include(":prayagainstplayer")
include(":pvptools")
include(":reorderprayers")
include(":runedoku")
include(":smithing")
include(":socket")
include(":socketdeathindicator")
include(":socketplayerstatus")
include(":socketplayerstatusextended")
include(":socketsotetseg")
include(":socketspecialcounter")
include(":sorceressgarden")
include(":spawntimer")
include(":specbar")
include(":specorb")
include(":spellbook")
include(":strongholdofsecurity")
include(":tarnslair")
include(":templetrekking")
include(":theatre")
include(":ticktimers")
include(":vetion")
include(":vorkath")
include(":whalewatchers")
include(":wildernesslocations")
include(":zulrah")

for (project in rootProject.children) {
    project.apply {
        projectDir = file(name)
        buildFileName = "$name.gradle.kts"

        require(projectDir.isDirectory) { "Project '${project.path} must have a $projectDir directory" }
        require(buildFile.isFile) { "Project '${project.path} must have a $buildFile build script" }
    }
}
