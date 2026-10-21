package com.zully.zulrah;

import java.awt.*;
import java.awt.image.BufferedImage;

/** Small toxic blowpipe sprite for the sidebar button. */
final class ZulrahIcon {
	private ZulrahIcon() { }
	static BufferedImage create() {
		BufferedImage image=new BufferedImage(16,16,BufferedImage.TYPE_INT_ARGB); Graphics2D g=image.createGraphics();
		g.setColor(new Color(9,18,14));g.fillRoundRect(1,6,12,5,3,3);g.setColor(new Color(52,82,58));g.fillRoundRect(2,7,11,3,2,2);
		g.setColor(new Color(123,239,77));g.fillOval(10,5,5,5);g.fillOval(12,8,3,3);g.setColor(new Color(219,255,151));g.fillRect(3,8,7,1);g.setColor(new Color(17,191,82));g.fillOval(1,10,4,4);g.dispose();return image;
	}
}
