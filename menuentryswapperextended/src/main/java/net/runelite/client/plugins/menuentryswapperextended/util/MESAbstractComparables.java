package net.runelite.client.plugins.menuentryswapperextended.util;

import net.runelite.api.MenuEntry;
import net.runelite.api.MenuOpcode;
import net.runelite.client.menus.AbstractComparableEntry;

public class MESAbstractComparables
{

	public static final AbstractComparableEntry FOLLOW = new AbstractComparableEntry()
	{
		private final int hash = "FOLLOW".hashCode() * 79 + getPriority();

		@Override
		public int hashCode()
		{
			return hash;
		}

		@Override
		public boolean equals(Object entry)
		{
			return entry.getClass() == this.getClass() && entry.hashCode() == this.hashCode();
		}

		@Override
		public int getPriority()
		{
			return 90;
		}

		@Override
		public boolean matches(MenuEntry entry)
		{
			return
				entry.getOpcode() == MenuOpcode.FOLLOW.getId() ||
					entry.getOpcode() == MenuOpcode.FOLLOW.getId() + MenuOpcode.MENU_ACTION_DEPRIORITIZE_OFFSET;
		}
	};

	public static final AbstractComparableEntry TAKE = new AbstractComparableEntry()
	{
		private final int hash = "TAKE".hashCode() * 79 + getPriority();

		@Override
		public int hashCode()
		{
			return hash;
		}

		@Override
		public boolean equals(Object entry)
		{
			return entry.getClass() == this.getClass() && entry.hashCode() == this.hashCode();
		}

		@Override
		public int getPriority()
		{
			return 100;
		}

		@Override
		public boolean matches(MenuEntry entry)
		{
			int opcode = entry.getOpcode();
			if (opcode > MenuOpcode.MENU_ACTION_DEPRIORITIZE_OFFSET)
			{
				opcode -= MenuOpcode.MENU_ACTION_DEPRIORITIZE_OFFSET;
			}

			return
				opcode >= MenuOpcode.GROUND_ITEM_FIRST_OPTION.getId() &&
					opcode <= MenuOpcode.GROUND_ITEM_FIFTH_OPTION.getId();
		}
	};

	public static final AbstractComparableEntry TRADE = new AbstractComparableEntry()
	{
		private final int hash = "TRADE".hashCode() * 79 + getPriority();

		@Override
		public int hashCode()
		{
			return hash;
		}

		@Override
		public boolean equals(Object entry)
		{
			return entry.getClass() == this.getClass() && entry.hashCode() == this.hashCode();
		}

		@Override
		public int getPriority()
		{
			return 90;
		}

		@Override
		public boolean matches(MenuEntry entry)
		{
			return
				entry.getOpcode() == MenuOpcode.TRADE.getId() ||
					entry.getOpcode() == MenuOpcode.TRADE.getId() + MenuOpcode.MENU_ACTION_DEPRIORITIZE_OFFSET;
		}
	};

	public static final AbstractComparableEntry WALK = new AbstractComparableEntry()
	{
		private final int hash = "WALK".hashCode() * 79 + getPriority();

		@Override
		public int hashCode()
		{
			return hash;
		}

		@Override
		public boolean equals(Object entry)
		{
			return entry.getClass() == this.getClass() && entry.hashCode() == this.hashCode();
		}

		@Override
		public int getPriority()
		{
			return 99;
		}

		@Override
		public boolean matches(MenuEntry entry)
		{
			return
				entry.getOpcode() == MenuOpcode.WALK.getId() ||
					entry.getOpcode() == MenuOpcode.WALK.getId() + MenuOpcode.MENU_ACTION_DEPRIORITIZE_OFFSET;
		}
	};
}