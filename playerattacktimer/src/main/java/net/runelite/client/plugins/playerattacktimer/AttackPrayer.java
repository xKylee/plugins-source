package net.runelite.client.plugins.playerattacktimer;

import net.runelite.api.widgets.WidgetInfo;

public enum AttackPrayer
{
	Rigour,
	Augury,
	Piety,
	MysticMight,
	EagleEye,
	Chivalry,
	HawkEye;

	static final String[] prayerLetters = {"r", "a", "p", "m", "e", "c", "h"};

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
			case MysticMight:
				return WidgetInfo.PRAYER_MYSTIC_MIGHT;
			case EagleEye:
				return WidgetInfo.PRAYER_EAGLE_EYE;
			case Chivalry:
				return WidgetInfo.PRAYER_CHIVALRY;
			case HawkEye:
				return WidgetInfo.PRAYER_HAWK_EYE;
		}
		return null;
	}

}

