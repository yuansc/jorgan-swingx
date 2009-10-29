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
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

/**
 * A <code>Bridge</code> is used by a {@link DockingPane} to hold an arbitrary
 * component (not a {@link Dockable}).
 * 
 * @see #setBridged(Object, JComponent)
 */
public class Bridge extends JPanel {

	private DockingPane dockingPane;

	private Object key;

	private JComponent bridged;

	/**
	 * Create a new bridge.
	 */
	public Bridge() {
		setLayout(new BridgeLayout());
	}

	/**
	 * Get the containing dockingPane.
	 * 
	 * @return the dockingPane this bridge is contained in
	 */
	public DockingPane getDockingPane() {
		return dockingPane;
	}

	/**
	 * Set the containing dockingPane.
	 * 
	 * @param dockingPane
	 *            the owning dockingPane
	 */
	public void setDockingPane(DockingPane dockingPane) {
		this.dockingPane = dockingPane;
	}

	public JComponent clearBridged() {
		JComponent bridged = this.bridged;

		setBridged(key, null);

		key = null;

		revalidate();
		repaint();

		fireBridgeChanged();

		return bridged;
	}

	public boolean hasBridged() {
		return key != null;
	}

	/**
	 * Associate a component with this bridge.
	 * 
	 * @param component
	 *            component to set, may be <code>null</code>
	 */
	public void setBridged(Object key, JComponent component) {

		if (key == null) {
			throw new IllegalArgumentException("key must not be null");
		}
		this.key = key;

		if (this.bridged != null) {
			remove(this.bridged);
		}

		this.bridged = component;

		if (this.bridged != null) {
			add(this.bridged);
		}

		revalidate();
		repaint();

		fireBridgeChanged();
	}

	/**
	 * Get the key.
	 * 
	 * @return key
	 */
	public Object getKey() {
		return key;
	}

	/**
	 * Get the bridged component.
	 * 
	 * @return the component
	 */
	public JComponent getBridged() {
		return bridged;
	}

	private class BridgeLayout extends MouseInputAdapter implements
			LayoutManager {

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

			if (bridged != null) {
				bridged.setBounds(x, y, width, height);
			}
		}

		public Dimension minimumLayoutSize(Container parent) {
			Dimension size = new Dimension();

			Insets insets = getInsets();
			size.width = insets.left + insets.right;
			size.height = insets.top + insets.bottom;

			if (bridged != null) {
				Dimension componentSize = bridged.getMinimumSize();

				size.width += componentSize.width;
				size.height += componentSize.height;
			}

			return size;
		}

		public Dimension preferredLayoutSize(Container parent) {
			return minimumLayoutSize(parent);
		}
	}

	protected void fireBridgeChanged() {
		DockingPane dockingPane = getDockingPane();
		if (dockingPane != null) {
			dockingPane.bridgeChanged(this);
		}
	}
}