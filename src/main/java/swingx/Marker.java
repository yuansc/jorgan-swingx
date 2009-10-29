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
package swingx;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;

import javax.swing.JComponent;

public class Marker {

	private JComponent component;

	private int x1;

	private int y1;

	private int x2;

	private int y2;

	private BasicStroke stroke;

	private Color foreground;

	private Color background;

	public Marker(JComponent component, Color background, Color foreground,
			BasicStroke stroke, int x1, int y1, int x2, int y2) {

		this.component = component;

		this.background = background;
		this.foreground = foreground;
		this.stroke = stroke;

		this.x1 = Math.min(x1, x2);
		this.y1 = Math.min(y1, y2);
		this.x2 = Math.max(x1, x2);
		this.y2 = Math.max(y1, y2);

		mark();
	}

	public JComponent getComponent() {
		return component;
	}

	public boolean contains(int x, int y) {
		return contains(x, y, 0, 0);
	}

	public boolean contains(int x, int y, int width, int height) {
		return this.x1 < x && this.x2 > x + width && this.y1 < y
				&& this.y2 > y + height;

	}

	public void release() {
		checkNotReleased();

		mark();

		component = null;
	}

	protected void mark() {
		component.repaint(x1, y1, x2 - x1, y2 - y1);
	}

	public void paint(Graphics2D g) {
		checkNotReleased();

		int thick = Math.round(stroke.getLineWidth());
		int x = x1 + thick / 2;
		int width = x2 - x1 - Math.max(1, thick);
		int y = y1 + thick / 2;
		int height = y2 - y1 - Math.max(1, thick);

		if (background != null) {
			g.setColor(background);
			g.fillRect(x, y, width, height);
		}

		Stroke originalStroke = g.getStroke();
		g.setStroke(stroke);
		g.setColor(foreground);
		g.drawRect(x, y, width, height);
		g.setStroke(originalStroke);
	}

	protected void checkNotReleased() {
		if (component == null) {
			throw new IllegalStateException("already released");
		}
	}

	private static class XORMarker extends Marker {

		public XORMarker(JComponent component, Color background,
				Color foreground, BasicStroke stroke, int x1, int y1, int x2,
				int y2) {
			super(component, background, foreground, stroke, x1, y1, x2, y2);
		}

		protected void mark() {
			checkNotReleased();

			Graphics2D g = (Graphics2D) getComponent().getGraphics();
			if (g != null) {
				paint(g);

				g.dispose();
			}
		}

		public void paint(Graphics2D g) {
			g.setXORMode(Color.white);

			super.paint(g);

			g.setPaintMode();
		}
	}

	public static Marker create(JComponent component, boolean xor,
			Color background, Color foreground, BasicStroke stroke,
			Rectangle rect) {
		return create(component, xor, background, foreground, stroke, rect.x,
				rect.y, rect.x + rect.width, rect.y + rect.height);
	}

	public static Marker create(JComponent component, boolean xor,
			Color background, Color foreground, BasicStroke stroke, int x1,
			int y1, int x2, int y2) {

		if (xor) {
			return new XORMarker(component, background, foreground, stroke, x1,
					y1, x2, y2);
		} else {
			return new Marker(component, background, foreground, stroke, x1,
					y1, x2, y2);
		}
	}
}