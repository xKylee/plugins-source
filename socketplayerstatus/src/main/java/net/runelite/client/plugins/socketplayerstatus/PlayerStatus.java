/*
 * Copyright (c) 2020, Charles Xu <github.com/kthisiscvpv>
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

package net.runelite.client.plugins.socketplayerstatus;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.runelite.client.plugins.socket.org.json.JSONObject;
import net.runelite.client.ui.overlay.components.PanelComponent;

@Setter(AccessLevel.PUBLIC)
@Getter(AccessLevel.PUBLIC)
@AllArgsConstructor
public class PlayerStatus
{

	private final PanelComponent panel = new PanelComponent();

	private int health;
	private int maxHealth;

	private int prayer;
	private int maxPrayer;

	private int run;
	private int special;

	private PlayerStatus()
	{
	}

	public JSONObject toJSON()
	{
		JSONObject json = new JSONObject();
		json.put("health", health);
		json.put("max-health", maxHealth);
		json.put("prayer", prayer);
		json.put("max-prayer", maxPrayer);
		json.put("run", run);
		json.put("special", special);
		return json;
	}

	public void parseJSON(JSONObject json)
	{
		health = json.getInt("health");
		maxHealth = json.getInt("max-health");
		prayer = json.getInt("prayer");
		maxPrayer = json.getInt("max-prayer");
		run = json.getInt("run");
		special = json.getInt("special");
	}

	public static PlayerStatus fromJSON(JSONObject json)
	{
		PlayerStatus ps = new PlayerStatus();
		ps.parseJSON(json);
		return ps;
	}
}
