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
package swingx.docking;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.MouseInputAdapter;

/**
 * A <code>JSplitPane</code> like component that devides its area into two
 * components and allows adjustment of layout through mouse drag.
 */
public class Slice extends JPanel {

	/**
	 * The spacing used to separate the main and remainder components.
	 */
	protected int spacing = 4;

	/**
	 * The weight of the main component.
	 */
	protected float weight = 0.5f;

	/**
	 * The orientation of the main component.
	 */
	protected int orientation;

	private SliceLayout layout = new SliceLayout();

	/**
	 * The main component.
	 */
	protected JComponent main;

	/**
	 * The remainder component.
	 */
	protected JComponent remainder;

	protected JComponent divider = new JPanel();

	/**
	 * Create a slice.
	 */
	public Slice() {
		setLayout(layout);

		divider.setOpaque(false);
		divider.addMouseListener(layout);
		divider.addMouseMotionListener(layout);
		add(divider);

		setOrientation(SwingConstants.TOP);
	}

	/**
	 * Set the main component.
	 * 
	 * @param main
	 *            main component
	 */
	public void setMain(JComponent main) {
		if (this.main != null) {
			remove(this.main);
		}

		this.main = main;

		if (main != null) {
			add(main);
		}

		repaint();
		revalidate();
	}

	/**
	 * Set the remainder component.
	 * 
	 * @param remainder
	 *            remainder component
	 */
	public void setRemainder(JComponent remainder) {
		if (this.remainder != null) {
			remove(this.remainder);
		}

		this.remainder = remainder;

		if (remainder != null) {
			add(remainder);
		}

		repaint();
		revalidate();
	}

	/**
	 * Set the spacing to be used to separate the main and remainder component.
	 * 
	 * @param spacing
	 *            spacing to use
	 */
	public void setSpacing(int spacing) {
		this.spacing = spacing;

		revalidate();
		repaint();
	}

	/**
	 * Get the spacing.
	 * 
	 * @return spacing
	 */
	public int getSpacing() {
		return spacing;
	}

	/**
	 * Set the position of the main component.
	 * 
	 * @param orientation
	 *            position of main component, SwingConstants.TOP,
	 *            SwingConstants.BOTTOM, SwingConstants.LEFT or
	 *            SwingConstants.RIGHT
	 */
	public void setOrientation(int orientation) {

		switch (orientation) {
		case SwingConstants.TOP:
			divider.setCursor(Cursor
					.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
			break;
		case SwingConstants.BOTTOM:
			divider.setCursor(Cursor
					.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
			break;
		case SwingConstants.LEFT:
			divider.setCursor(Cursor
					.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
			break;
		case SwingConstants.RIGHT:
			divider.setCursor(Cursor
					.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
			break;
		default:
			throw new IllegalArgumentException("unkown orientation '"
					+ orientation + "'");
		}
		this.orientation = orientation;

		revalidate();
	}

	/**
	 * Get the position of the main component.
	 * 
	 * @return position
	 */
	public int getOrientation() {
		return orientation;
	}

	/**
	 * Get the main component.
	 * 
	 * @return main component
	 */
	public JComponent getMain() {
		return main;
	}

	/**
	 * Get the remainder component.
	 * 
	 * @return remainder component
	 */
	public JComponent getRemainder() {
		return remainder;
	}

	/**
	 * Get the weight of the main component.
	 * 
	 * @return weight
	 */
	public float getWeight() {
		return weight;
	}

	/**
	 * Set the weight of the main component, i.e. how much available space is
	 * assigned to the main component.
	 * 
	 * @param weight
	 *            weight
	 */
	public void setWeight(float weight) {
		if (weight > 1.0f) {
			weight = 1.0f;
		}

		if (weight < 0.0f) {
			weight = 0.0f;
		}

		this.weight = weight;

		revalidate();
	}

	/**
	 * Get the other component than the given component.
	 * 
	 * @param child
	 *            child to get other component for
	 * @return the other component
	 */
	public JComponent getOther(JComponent child) {
		if (main == child) {
			return remainder;
		} else if (remainder == child) {
			return main;
		} else {
			throw new IllegalArgumentException("unkown child");
		}
	}

	/**
	 * Replace the given child with a new component.
	 * 
	 * @param child
	 *            child to replace
	 * @param newChild
	 *            new component
	 */
	public void replace(JComponent child, JComponent newChild) {
		if (main == child) {
			setMain(newChild);
		} else if (remainder == child) {
			setRemainder(newChild);
		} else {
			throw new IllegalArgumentException("unkown child");
		}
	}

	private class SliceLayout extends MouseInputAdapter implements
			LayoutManager {

		private Point dragStart;

		public void addLayoutComponent(String name, Component comp) {
		}

		public void removeLayoutComponent(Component comp) {
		}

		public void layoutContainer(Container parent) {
			Insets insets = getInsets();

			int x = insets.left;
			int y = insets.top;
			int width = parent.getWidth() - insets.left - insets.right;
			int height = parent.getHeight() - insets.top - insets.bottom;

			if (main != null && main.isVisible() && remainder != null
					&& remainder.isVisible()) {
				divider.setVisible(true);

				if (orientation == SwingConstants.TOP
						|| orientation == SwingConstants.BOTTOM) {
					height -= spacing;

					JComponent top;
					JComponent bottom;
					int topHeight;
					int bottomHeight;
					if (orientation == SwingConstants.TOP) {
						top = main;
						bottom = remainder;

						topHeight = (int) (height * weight);
						bottomHeight = height - topHeight;
					} else {
						bottom = main;
						top = remainder;

						bottomHeight = (int) (height * weight);
						topHeight = height - bottomHeight;
					}

					Dimension bottomSize = bottom.getMinimumSize();
					if (bottomSize.height > bottomHeight) {
						bottomHeight = Math.min(bottomSize.height, height);
						topHeight = height - bottomHeight;
					}

					Dimension topSize = top.getMinimumSize();
					if (topSize.height > topHeight) {
						topHeight = Math.min(topSize.height, height - spacing);
						bottomHeight = height - topHeight;
					}

					top.setBounds(x, y, width, topHeight);
					y += topHeight;
					divider.setBounds(x, y, width, spacing);
					y += spacing;
					bottom.setBounds(x, y, width, bottomHeight);
				} else if (orientation == SwingConstants.LEFT
						|| orientation == SwingConstants.RIGHT) {
					width -= spacing;

					JComponent left;
					JComponent right;
					int leftWidth;
					int rightWidth;
					if (orientation == SwingConstants.LEFT) {
						left = main;
						right = remainder;

						leftWidth = (int) (width * weight);
						rightWidth = width - leftWidth;
					} else {
						right = main;
						left = remainder;

						rightWidth = (int) (width * weight);
						leftWidth = width - rightWidth;
					}

					Dimension rightSize = right.getMinimumSize();
					if (rightSize.width > rightWidth) {
						rightWidth = Math.min(rightSize.width, width);
						leftWidth = width - rightWidth;
					}

					Dimension leftSize = left.getMinimumSize();
					if (leftSize.width > leftWidth) {
						leftWidth = Math.min(leftSize.width, width - spacing);
						rightWidth = width - leftWidth;
					}

					left.setBounds(x, y, leftWidth, height);
					x += leftWidth;
					divider.setBounds(x, y, spacing, height);
					x += spacing;
					right.setBounds(x, y, rightWidth, height);
				}
			} else {
				divider.setVisible(false);

				if (main != null) {
					main.setBounds(x, y, width, height);
				}
				if (remainder != null) {
					remainder.setBounds(x, y, width, height);
				}
			}
		}

		public Dimension minimumLayoutSize(Container parent) {
			Dimension size = new Dimension();

			if (main != null && main.isVisible()) {
				Dimension mainSize = main.getMinimumSize();
				if (orientation == SwingConstants.LEFT
						|| orientation == SwingConstants.RIGHT) {
					size.width += mainSize.width;
					;
					size.height = Math.max(mainSize.height, size.height);
				} else {
					size.width = Math.max(mainSize.width, size.width);
					size.height += mainSize.height;
				}
			}
			if (remainder != null && remainder.isVisible()) {
				Dimension remainderSize = remainder.getMinimumSize();
				if (orientation == SwingConstants.LEFT
						|| orientation == SwingConstants.RIGHT) {
					size.width += remainderSize.width;
					;
					size.height = Math.max(remainderSize.height, size.height);
				} else {
					size.width = Math.max(remainderSize.width, size.width);
					size.height += remainderSize.height;
				}
			}

			if (main != null && main.isVisible() && remainder != null
					&& remainder.isVisible()) {
				if (orientation == SwingConstants.LEFT
						|| orientation == SwingConstants.RIGHT) {
					size.width += spacing;
				} else {
					size.height += spacing;
				}
			}

			Insets insets = getInsets();
			size.width += insets.left + insets.right;
			size.height += insets.top + insets.bottom;

			return size;
		}

		public Dimension preferredLayoutSize(Container parent) {
			return minimumLayoutSize(parent);
		}

		public void mousePressed(MouseEvent e) {
			dragStart = e.getPoint();
		}

		public void mouseDragged(MouseEvent e) {
			if (dragStart == null) {
				// mousePressed might not be called if focus was in a popupmenu
				// previously
				dragStart = e.getPoint();
			}

			int x = divider.getX() + e.getX() - dragStart.x;
			int y = divider.getY() + e.getY() - dragStart.y;

			int height = getHeight() - spacing;
			int width = (getWidth() - spacing);

			switch (orientation) {
			case SwingConstants.TOP:
				weight = (float) y / height;
				break;
			case SwingConstants.BOTTOM:
				weight = 1.0f - (float) y / height;
				break;
			case SwingConstants.LEFT:
				weight = (float) x / width;
				break;
			case SwingConstants.RIGHT:
				weight = 1.0f - (float) x / width;
				break;
			}

			weight = Math.max(0.0f, Math.min(weight, 1.0f));

			layoutContainer(Slice.this);

			if (orientation == SwingConstants.TOP
					|| orientation == SwingConstants.BOTTOM) {
				weight = (float) main.getHeight() / height;
			} else {
				weight = (float) main.getWidth() / width;
			}

			main.validate();
			remainder.validate();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			dragStart = null;
		}
	}
}