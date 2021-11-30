package net.runelite.client.plugins.playerattacktimer;

import net.runelite.api.widgets.WidgetInfo;

public enum AttackPrayer
{
	Rigour,
	Augury,
	Piety;

	public WidgetInfo getWidgetInfo()
	{
		switch (this)
		{
			case Augury:
				return WidgetInfo.PRAYER_AUGURY;
			case Piety:
				return WidgetInfo.PRAYER_PIETY;
			case Rigour:
				return WidgetInfo.PRAYER_RIGOUR;
		}
		return null;
	}
}

