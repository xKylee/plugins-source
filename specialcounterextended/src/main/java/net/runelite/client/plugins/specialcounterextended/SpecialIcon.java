package net.runelite.client.plugins.specialcounterextended;

import java.awt.image.BufferedImage;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class SpecialIcon
{

	@Getter(AccessLevel.PUBLIC)
	private BufferedImage image;

	@Getter(AccessLevel.PUBLIC)
	private String text;

	@Getter(AccessLevel.PUBLIC)
	private long startTime;

}