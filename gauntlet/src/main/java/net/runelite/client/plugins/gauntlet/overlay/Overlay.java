package net.runelite.client.plugins.gauntlet.overlay;


import net.runelite.client.plugins.Plugin;

public abstract class Overlay extends net.runelite.client.ui.overlay.Overlay
{
	public abstract void determineLayer();

	public Overlay(final Plugin plugin)
	{
		super(plugin);
	}
}
