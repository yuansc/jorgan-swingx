/*
 * swingx - Swing eXtensions
 * Copyright (C) 2004 Sven Meier
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package swingx.docking.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.JComponent;

/**
 * A {@link LayoutManager} that floats its components to {@link #FLOAT_LEFT} or
 * {@link #FLOAT_RIGHT} according to their {@link JComponent#getAlignmentX()}.
 */
public class FloatingLayout implements LayoutManager {

	public static final float FLOAT_RIGHT = 1.0f;

	public static final float FLOAT_LEFT = 0.0f;

	public void addLayoutComponent(String name, Component comp) {
	}

	public void removeLayoutComponent(Component comp) {
	}

	public void layoutContainer(Container parent) {
		Insets insets = parent.getInsets();

		int x = insets.left;
		int y = insets.top;
		int maxWidth = parent.getWidth() - insets.left - insets.right;

		int currentX1 = x;
		int currentX2 = x + maxWidth;
		int currentY = y;
		int currentHeight = 0;
		for (int c = 0; c < parent.getComponentCount(); c++) {
			Component component = parent.getComponent(c);
			if (component.isVisible()) {
				Dimension cSize = component.getPreferredSize();

				if ((currentX1 > x || currentX2 < x + maxWidth)
						&& (currentX1 + cSize.width) > currentX2) {
					currentX1 = x;
					currentX2 = x + maxWidth;
					currentY += currentHeight;
					currentHeight = 0;
				}

				if (component.getAlignmentX() < 0.5f) {
					component.setBounds(currentX1, currentY, Math.min(
							cSize.width, currentX2 - currentX1), cSize.height);

					currentX1 += cSize.getWidth();
				} else {
					component.setBounds(currentX2 - cSize.width, currentY,
							cSize.width, cSize.height);

					currentX2 -= cSize.getWidth();
				}

				currentHeight = Math.max(currentHeight, cSize.height);
			}
		}
	}

	public Dimension minimumLayoutSize(Container parent) {
		Dimension size = new Dimension();

		return size;
	}

	public Dimension preferredLayoutSize(Container parent) {
		Dimension size = new Dimension();

		Insets insets = parent.getInsets();

		int maxWidth = parent.getWidth() - (insets.left + insets.right);

		int currentWidth = 0;
		int currentHeight = 0;
		for (int c = 0; c < parent.getComponentCount(); c++) {
			Component component = parent.getComponent(c);
			if (component.isVisible()) {
				Dimension cSize = component.getPreferredSize();

				if (currentWidth > 0 && (currentWidth + cSize.width) > maxWidth) {
					size.height += currentHeight;
					size.width = Math.max(size.width, currentWidth);

					currentWidth = 0;
					currentHeight = 0;
				}

				currentWidth += cSize.width;
				currentHeight = Math.max(currentHeight, cSize.height);
			}
		}
		size.height += currentHeight;

		size.width += insets.left + insets.right;
		size.height += insets.top + insets.bottom;

		return size;
	}
}