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
package swingx.docking.dock;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import swingx.docking.Dock;
import swingx.docking.Dockable;
import swingx.docking.Docked;
import swingx.docking.border.LineBorder;
import swingx.docking.layout.FloatingLayout;

/**
 * A <code>Dock</code> that utilizes a {@link JTabbedPane} to layer its
 * {@link Dockables} in tabs.
 * 
 * @see #putDockable(Object, Dockable)
 * @see #removeDockable(Object)
 */
public class TabbedDock extends Dock {

	private static EmptyBorder emptyBorder = new EmptyBorder(0, 0, 0, 0);

	private JTabbedPane tabbedPane = createTabbedPane();

	/**
	 * All tabs.
	 */
	private List<Tab> tabs = new ArrayList<Tab>();

	/**
	 * Constructor.
	 */
	public TabbedDock() {
		setLayout(new BorderLayout());

		tabbedPane = createTabbedPane();

		add(tabbedPane);
	}

	@Override
	public JComponent getDragInitiator() {
		return tabbedPane;
	}

	/**
	 * Factory method to create the tabbed pane.
	 * 
	 * @return created tabbed pane
	 */
	protected JTabbedPane createTabbedPane() {
		return new JTabbedPane();
	}

	@Override
	public List<Object> getDockableKeys() {
		List<Object> keys = new ArrayList<Object>();
		for (Tab tab : tabs) {
			keys.add(tab.getKey());
		}
		return keys;
	}

	@Override
	public void clearDockables() {
		for (Tab tab : new ArrayList<Tab>(tabs)) {
			tab.setDockable(null);
			tab.dispose();	
		}
		tabs.clear();

		fireDockChanged();
	}

	@Override
	public boolean containsDockable(Object key) {
		return getTab(key) != null;
	}

	@Override
	public void putDockable(Object key, Dockable dockable) {
		if (key == null) {
			throw new IllegalArgumentException("key must not be null");
		}

		Tab tab = getTab(key);
		if (tab == null) {
			tab = new Tab(key);
			tabs.add(0, tab);
		}

		tab.setDockable(dockable);

		fireDockChanged();

		revalidate();
		repaint();
	}

	@Override
	public Dockable getSelectedDockable() {
		Tab tab = (Tab) tabbedPane.getSelectedComponent();
		if (tab == null) {
			return null;
		} else {
			return tab.getDockable();
		}
	}

	@Override
	public void setSelectedDockable(Dockable dockable) {
		if (dockable == null) {
			throw new IllegalArgumentException("dockable must not be null");
		}

		tabbedPane.setSelectedComponent(getTab(dockable));
	}

	@Override
	public Dockable getDockable(Object key) {
		if (key == null) {
			throw new IllegalArgumentException("key must not be null");
		}

		Tab tab = getTab(key);
		if (tab == null) {
			throw new IllegalArgumentException("unkown key");
		}
		return tab.getDockable();
	}

	@Override
	public Dockable removeDockable(Object key) {
		if (key == null) {
			throw new IllegalArgumentException("key must not be null");
		}

		Tab tab = getTab(key);
		if (tab == null) {
			throw new IllegalArgumentException("unkown key");
		}

		Dockable dockable = tab.getDockable();
		tab.setDockable(null);
		tab.dispose();	
		tabs.remove(tab);

		fireDockChanged();

		return dockable;
	}

	@Override
	public boolean hasDockable() {
		return !tabs.isEmpty();
	}

	@Override
	public int getDockableCount() {
		return tabs.size();
	}

	@Override
	public List<Object> getVisibleDockableKeys() {
		List<Object> keys = new ArrayList<Object>();

		for (Tab tab : tabs) {
			if (tab.getDockable() != null) {
				keys.add(tab.getKey());
			}
		}

		return keys;
	}

	@Override
	public int getVisibleDockableCount() {
		return tabbedPane.getTabCount();
	}

	@Override
	public void closeDockable(Dockable dockable) {
		if (dockable == null) {
			throw new IllegalArgumentException("dockable must not be null");
		}

		Tab tab = getTab(dockable);
		if (tab == null) {
			throw new IllegalArgumentException("unkown dockable '" + dockable
					+ "'");
		}

		if (tab.close()) {
			fireDockChanged();

			revalidate();
			repaint();
		}
	}

	@Override
	public Object getDockableKey(Point point) {
		int index = tabbedPane.getUI().tabForCoordinate(tabbedPane, point.x,
				point.y);

		if (index == -1) {
			return null;
		} else {
			Tab tab = (Tab) tabbedPane.getComponentAt(index);
			return tab.getKey();
		}
	}

	private Tab getTab(Object key) {
		for (Tab tab : tabs) {
			if (tab.hasKey(key)) {
				return tab;
			}
		}
		return null;
	}

	private Tab getTab(Dockable dockable) {
		for (Tab tab : tabs) {
			if (tab.getDockable() != null && tab.getDockable().equals(dockable)) {
				return tab;
			}
		}
		return null;
	}

	/**
	 * Customize the given content.
	 * 
	 * @param content
	 *            the content to customize
	 * @return the customized content
	 */
	protected JComponent customizeContent(JComponent content) {
		if (content instanceof JScrollPane) {
			JScrollPane scrollPane = (JScrollPane) content;

			scrollPane.setBorder(emptyBorder);
			scrollPane.setViewportBorder(emptyBorder);
		}
		return content;
	}

	private class Tab extends JPanel implements Docked {

		private Object key;

		private Dockable dockable;

		private JPanel header = new JPanel();

		private JToolBar toolBar = new JToolBar() {
			@Override
			protected void paintComponent(Graphics g) {
				// radical, but the only way to make sure that no decoration
				// is painted on the toolbar background
			}
		};

		private JLabel statusLabel = new JLabel();

		private JComponent content;

		private Tab(Object key) {
			if (key == null) {
				throw new IllegalArgumentException("key must not be null");
			}
			this.key = key;

			setLayout(new BorderLayout());
			setOpaque(false);

			header.setLayout(new FloatingLayout());
			header.setBorder(new LineBorder());
			header.setOpaque(false);
			header.setVisible(false);
			add(header, BorderLayout.NORTH);

			if (UIManager.getLookAndFeel().getName().indexOf("Windows") != -1) {
				toolBar.setRollover(true);
			}
			toolBar.setFloatable(false);
			toolBar.setOpaque(false);
			toolBar.setVisible(false);
			toolBar.setBorder(emptyBorder);
			toolBar.setAlignmentX(FloatingLayout.FLOAT_RIGHT);
			header.add(toolBar);

			statusLabel.setOpaque(false);
			statusLabel.setVisible(false);
			statusLabel.setAlignmentX(FloatingLayout.FLOAT_LEFT);
			header.add(statusLabel);
		}

		public void dispose() {
			key = null;
		}
		
		public boolean hasKey(Object key) {
			return this.key.equals(key);
		}

		private Object getKey() {
			return key;
		}

		private boolean close() {
			if (dockable == null) {
				throw new IllegalArgumentException("dockable is null");
			}

			Dockable dockable = this.dockable;
			if (dockable.undocking()) {
				setDockable(null);
				return true;
			}

			return false;
		}

		private Dockable getDockable() {
			return dockable;
		}

		private void setDockable(Dockable dockable) {

			Tab selectedTab = (Tab) tabbedPane.getSelectedComponent();

			if (this.dockable != null) {
				tabbedPane.remove(this);

				toolBar.removeAll();
				setStatus(null);

				if (content != null) {
					remove(content);
					content = null;
				}

				this.dockable.undocked();
			}

			this.dockable = dockable;

			if (this.dockable != null) {
				int visibleIndex = 0;
				for (Tab tab : tabs) {
					if (tab == this) {
						break;
					}
					if (tab.getDockable() != null) {
						visibleIndex++;
					}
				}
				tabbedPane.add(this, visibleIndex);

				dockable.docked(this);
			}

			if (selectedTab != null && selectedTab != this) {
				tabbedPane.setSelectedComponent(selectedTab);
			}

			revalidate();
			repaint();
		}

		public void setTitle(String title) {
			int index = tabbedPane.indexOfComponent(this);

			tabbedPane.setTitleAt(index, title);
		}

		public void setIcon(Icon icon) {
			int index = tabbedPane.indexOfComponent(this);

			tabbedPane.setIconAt(index, icon);
		}

		public void setStatus(String status) {
			statusLabel.setText(status);
			statusLabel.setVisible(status != null);

			updateHeaderVisibility();
		}

		public void setMenu(JMenu menu) {
		}

		public JButton addTool(Action action) {
			JButton button = toolBar.add(action);
			button.setOpaque(false);
			
			// allow keyboard focus ...
			button.setFocusable(true);
			// ... but not on click
			button.setRequestFocusEnabled(false);
			
			toolBar.setVisible(true);

			updateHeaderVisibility();
			
			return button;
		}

		public JComponent addTool(JComponent component) {
			toolBar.add(component);

			// allow keyboard focus ...
			component.setFocusable(true);
			// ... but not on click
			component.setRequestFocusEnabled(false);
			
			toolBar.setVisible(true);

			updateHeaderVisibility();
			
			return component;
		}

		public void addToolSeparator() {
			toolBar.addSeparator();
			toolBar.setVisible(true);

			updateHeaderVisibility();
		}

		public void clearTools() {
			toolBar.removeAll();
			toolBar.setVisible(false);

			updateHeaderVisibility();
		}
		
		private void updateHeaderVisibility() {
			header.setVisible(toolBar.isVisible() || statusLabel.isVisible());
		}

		public void setContent(JComponent content) {
			if (this.content != null) {
				remove(this.content);
			}

			this.content = customizeContent(content);

			if (this.content != null) {
				add(this.content, BorderLayout.CENTER);
			}

			revalidate();
			repaint();
		}

	}
}