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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.SwingConstants;

import swingx.Marker;

/**
 * The root of {@link swingx.docking.Slice}s, {@link swingx.docking.Dock}s and
 * {@link swingx.docking.Bridge}s.
 * 
 * @author Sven
 * 
 */
public class Docking extends JLayeredPane {

	private DockingPane dockingPane;

	/**
	 * A panel that is layered on top of all other components while dropping.
	 */
	public DropLayer dropLayer = new DropLayer();

	/**
	 * The root of the <code>Slice</code> hierarchy.
	 */
	private JComponent root;

	private Rectangle screenBounds = new Rectangle();

	/**
	 * Constructor.
	 */
	public Docking() {
		setLayout(new DockLayout());

		add(dropLayer, JLayeredPane.DRAG_LAYER);
	}

	public void setMarkerXOR(boolean xor) {
		dropLayer.xor = xor;
	}

	public void setMarkerStroke(BasicStroke stroke) {
		dropLayer.stroke = stroke;
	}

	public void setMarkerForeground(Color foreground) {
		dropLayer.foreground = foreground;
	}

	public void setMarkerBackground(Color background) {
		dropLayer.background = background;
	}

	/**
	 * Set the owning dockingPane.
	 * 
	 * @param dockingPane
	 *            the owning dockingPane
	 */
	public void setDockingPane(DockingPane dockingPane) {
		if (this.dockingPane != null) {
			throw new Error("dockingPane already set");
		}
		this.dockingPane = dockingPane;
	}

	/**
	 * Set the root.
	 * 
	 * @param root
	 */
	public void setRoot(JComponent root) {
		this.root = root;
		add(root);
	}

	/**
	 * Set the bounds on screen.
	 * 
	 * @param bounds
	 *            bounds
	 */
	public void setScreenBounds(Rectangle bounds) {
		if (bounds == null) {
			throw new IllegalArgumentException("bounds must not be null");
		}
		screenBounds = bounds;
	}

	/**
	 * Get the screen bounds.
	 * 
	 * @return bounds
	 */
	public Rectangle getScreenBounds() {
		return screenBounds;
	}

	/**
	 * Get the root.
	 * 
	 * @return root
	 */
	public JComponent getRoot() {
		return root;
	}

	/**
	 * Start a drop.
	 */
	public void startDrop() {
		dropLayer.setVisible(true);
	}

	/**
	 * End a drop.
	 */
	public void endDrop() {
		dropLayer.clearMark();
		dropLayer.setVisible(false);
	}

	/**
	 * Returns all keys of contained dockables.
	 * 
	 * @return keys
	 */
	public List<Object> getDockableKeys() {
		List<Object> keys = new ArrayList<Object>();

		getDockableKeys(root, keys);

		return keys;
	}

	private void getDockableKeys(JComponent component, List<Object> keys) {
		if (component instanceof Slice) {
			Slice slice = (Slice) component;

			getDockableKeys(slice.getMain(), keys);
			getDockableKeys(slice.getRemainder(), keys);
		} else if (component instanceof Dock) {
			Dock dock = (Dock) component;

			keys.addAll(dock.getDockableKeys());
		}
	}

	public void clearDockables() {
		for (Object key : getDockableKeys()) {
			getDock(key).removeDockable(key);
		}
	}

	public boolean hasDockable() {
		return hasDockable(root);
	}

	protected boolean hasDockable(JComponent component) {
		if (component instanceof Slice) {
			Slice slice = (Slice) component;

			if (hasDockable(slice.getMain())) {
				return true;
			}
			if (hasDockable(slice.getRemainder())) {
				return true;
			}
		} else if (component instanceof Dock) {
			Dock dock = (Dock) component;

			return dock.hasDockable();
		}
		return false;
	}

	public boolean updateVisibility() {
		boolean hasVisible = hasVisibleDockOrBridge(root);

		Dock dock = null;
		if (!hasVisible) {
			dock = getDock(null, root);
		}
		updateVisibility(root, dock);

		return hasVisible;
	}

	protected void updateVisibility(JComponent component, Dock keepVisible) {
		if (component instanceof Slice) {
			Slice slice = (Slice) component;

			updateVisibility(slice.getMain(), keepVisible);
			updateVisibility(slice.getRemainder(), keepVisible);

			slice.setVisible(slice.getMain().isVisible()
					|| slice.getRemainder().isVisible());
		} else if (component instanceof Dock) {
			Dock dock = (Dock) component;

			component.setVisible(dock == keepVisible
					|| dock.getVisibleDockableCount() > 0);
		} else if (component instanceof Bridge) {
			Bridge bridge = (Bridge) component;

			bridge.setVisible(bridge.getBridged() != null);
		}
	}

	protected boolean hasVisibleDockOrBridge(JComponent component) {
		if (component instanceof Slice) {
			Slice slice = (Slice) component;

			if (hasVisibleDockOrBridge(slice.getMain())) {
				return true;
			}
			if (hasVisibleDockOrBridge(slice.getRemainder())) {
				return true;
			}
		} else if (component instanceof Dock) {
			Dock dock = (Dock) component;

			if (dock.getVisibleDockableCount() > 0) {
				return true;
			}
		} else if (component instanceof Bridge) {
			Bridge bridge = (Bridge) component;

			if (bridge.getBridged() != null) {
				return true;
			}
		}
		return false;
	}

	public Slice slice(JComponent child, JComponent main) {
		Slice newSlice = this.dockingPane.createSlice();

		if (child == root) {
			remove(root);

			newSlice.setMain(main);
			newSlice.setRemainder(child);

			root = newSlice;
			add(root);

			repaint();
			revalidate();
		} else {
			Slice parentSlice = getSlice(child);
			parentSlice.replace(child, newSlice);

			newSlice.setMain(main);
			newSlice.setRemainder(child);
		}

		return newSlice;
	}

	public void unslice(JComponent child) {
		if (child == root) {
			remove(root);

			root = dockingPane.createDock();
			add(root);

			repaint();
			revalidate();
		} else {
			Slice slice = getSlice(child);

			JComponent other = slice.getOther(child);
			if (slice == root) {
				remove(root);

				root = other;
				add(other);
			} else {
				Slice sliceSlice = getSlice(slice);
				sliceSlice.replace(slice, other);
			}
		}
	}

	private Slice getSlice(JComponent component) {
		Component parent = component.getParent();
		while (!(parent instanceof Slice)) {
			parent = parent.getParent();
		}
		return (Slice) parent;
	}

	public Bridge getBridge(Object key) {
		return getBridge(key, root);
	}

	private Bridge getBridge(Object key, JComponent component) {
		Bridge bridge = null;

		if (component instanceof Bridge) {
			Bridge candidate = (Bridge) component;

			if (key.equals(candidate.getKey())) {
				bridge = candidate;
			}
		} else if (component instanceof Slice) {
			Slice slice = (Slice) component;

			bridge = getBridge(key, slice.getMain());
			if (bridge == null) {
				bridge = getBridge(key, slice.getRemainder());
			}
		}
		return bridge;
	}

	public Dock getDock(Object key) {
		return getDock(key, root);
	}

	private Dock getDock(Object key, JComponent component) {
		Dock dock = null;

		if (component instanceof Dock) {
			Dock candidate = (Dock) component;

			if (key == null || candidate.containsDockable(key)) {
				dock = candidate;
			}
		} else if (component instanceof Slice) {
			Slice slice = (Slice) component;

			dock = getDock(key, slice.getMain());
			if (dock == null) {
				dock = getDock(key, slice.getRemainder());
			}
		}

		return dock;
	}

	protected class DockLayout implements LayoutManager {

		public void addLayoutComponent(String name, Component comp) {
		}

		public void removeLayoutComponent(Component comp) {
		}

		public Dimension minimumLayoutSize(Container parent) {
			Dimension size = new Dimension();

			Insets insets = getInsets();
			size.width = insets.left + insets.top;
			size.height = insets.top + insets.bottom;

			if (root.isVisible()) {
				Dimension rootSize = root.getMinimumSize();

				size.width += rootSize.width;
				size.height += rootSize.height;
			}

			return size;
		}

		public Dimension preferredLayoutSize(Container parent) {
			Dimension size = new Dimension();

			Insets insets = getInsets();

			size.width = insets.left + screenBounds.width + insets.right;
			size.height = insets.top + screenBounds.height + insets.bottom;

			return size;
		}

		public void layoutContainer(Container parent) {
			int x = 0;
			int y = 0;
			int width = parent.getWidth();
			int height = parent.getHeight();

			dropLayer.setBounds(x, y, width, height);

			Insets insets = parent.getInsets();
			x += insets.left;
			y += insets.top;
			width -= insets.left + insets.right;
			height -= insets.top + insets.bottom;

			root.setBounds(x, y, width, height);
		}
	}

	protected class DropLayer extends JComponent {

		/**
		 * Should xor be used for markers.
		 */
		private boolean xor = false;

		/**
		 * The background color to use for markers.
		 */
		private Color background = new Color(0, 0, 255, 32);

		/**
		 * The foreground color to use for markers.
		 */
		private Color foreground = new Color(0, 0, 255, 128);

		/**
		 * The stroke to use for markers.
		 */
		private BasicStroke stroke = new BasicStroke(2.0f);

		/**
		 * The current marker.
		 */
		private Marker marker;

		private DropLayer() {
			setVisible(false);
		}

		protected void paintComponent(Graphics g) {
			super.paintComponent(g);

			if (marker != null) {
				marker.paint((Graphics2D) g);
			}
		}

		public void clearMark() {
			if (marker != null) {
				marker.release();
				marker = null;
			}
		}

		public void setMark(Rectangle rect, int orientation, float weight) {
			clearMark();

			if (rect == null) {
				throw new IllegalArgumentException("rect must not be null");
			}

			if (orientation == SwingConstants.TOP) {
				rect.height = (int) (rect.height * weight);
			} else if (orientation == SwingConstants.BOTTOM) {
				rect.y = rect.y + rect.height - (int) (rect.height * weight);
				rect.height = (int) (rect.height * weight);
			} else if (orientation == SwingConstants.LEFT) {
				rect.width = (int) (rect.width * weight);
			} else if (orientation == SwingConstants.RIGHT) {
				rect.x = rect.x + rect.width - (int) (rect.width * weight);
				rect.width = (int) (rect.width * weight);
			}

			marker = Marker.create(this, xor, background, foreground, stroke, rect);
		}
	}

	public JComponent getDropReceiver() {
		return dropLayer;
	}
}