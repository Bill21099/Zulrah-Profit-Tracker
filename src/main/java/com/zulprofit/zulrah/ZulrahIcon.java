package com.zulprofit.zulrah;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Small snake-themed icon for the sidebar navigation button.
 * Drawn programmatically to keep the plugin self-contained.
 */
final class ZulrahIcon
{
	private ZulrahIcon() { }

	static BufferedImage create()
	{
		BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// Zulrah serpentine green body
		g.setColor(new Color(25, 95, 45));
		g.fillOval(3, 4, 10, 10);

		// Snake neck / head curve
		g.setColor(new Color(45, 140, 70));
		g.fillOval(6, 2, 7, 7);

		// Snout
		g.setColor(new Color(60, 180, 85));
		g.fillRect(10, 3, 4, 3);

		// Eye - red / orange
		g.setColor(new Color(230, 60, 20));
		g.fillOval(9, 3, 2, 2);

		// Highlights
		g.setColor(new Color(130, 220, 100, 180));
		g.fillOval(5, 6, 4, 3);

		g.dispose();
		return image;
	}
}

