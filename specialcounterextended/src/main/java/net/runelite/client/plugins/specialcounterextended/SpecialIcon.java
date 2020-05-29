package net.runelite.client.plugins.specialcounterextended;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.awt.image.BufferedImage;

@AllArgsConstructor
public class SpecialIcon {

    @Getter(AccessLevel.PUBLIC)
    private BufferedImage image;

    @Getter(AccessLevel.PUBLIC)
    private String text;

    @Getter(AccessLevel.PUBLIC)
    private long startTime;

}
