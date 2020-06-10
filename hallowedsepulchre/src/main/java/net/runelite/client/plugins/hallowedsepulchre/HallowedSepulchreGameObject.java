package net.runelite.client.plugins.hallowedsepulchre;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.GameObject;

class HallowedSepulchreGameObject
{
	@Getter(AccessLevel.PACKAGE)
	private final GameObject gameObject;

	@Getter(AccessLevel.PACKAGE)
	@Setter(AccessLevel.PACKAGE)
	private int ticksUntilNextAnimation;

	@Getter(AccessLevel.PACKAGE)
	private final int animationId;

	@Getter(AccessLevel.PACKAGE)
	private final int animationSpeed;

	HallowedSepulchreGameObject(final GameObject gameObject, final int animationId, final int animationSpeed)
	{
		this.gameObject = gameObject;
		this.animationId = animationId;
		this.animationSpeed = animationSpeed;
		this.ticksUntilNextAnimation = -1;
	}
}