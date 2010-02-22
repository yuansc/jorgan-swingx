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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import swingx.dnd.ObjectTransferable;
import swingx.docking.dock.TabbedDock;

/**
 * A container for {@link Dockable}s, that can be dragged and stacked. To dock
 * a dockable it has to be associated with a key:
 * 
 * <pre>
 * map.putDockable(&quot;KEY&quot;, dockable);
 * </pre>
 * 
 * A dockable can be removed via its associated key:
 * 
 * <pre>
 * map.removeDockable(&quot;KEY&quot;);
 * </pre>
 * 
 * A <code>DockingPane</code> can also handle <code>null</code> dockables,
 * i.e. a key of a dockable is already registered but the dockable is not yet
 * contained:
 * 
 * <pre>
 * map.putDockable(&quot;KEY&quot;, null);
 * </pre>
 * 
 * If you call this method while a dockable is already associated with the key,
 * the dockable will be removed but the key will stay in this
 * <code>DockingPane</code>.
 * <p>
 * Note that keys may be instances of any class but a {@link Persister}may
 * restrict them to be of a special class.
 * <p>
 * Standard Swing components can also be added to a <code>DockingPane</code>,
 * although these components will not be draggable and will not be stacked in
 * {@link Dock}s:
 * 
 * <pre>
 * dockingPane.putComponent(&quot;KEY&quot;, component);
 * </pre>
 * 
 * @see #putDockable(Object, Dockable)
 * @see #removeDockable(Object)
 * @see #putComponent(Object, JComponent)
 * @see #removeComponent(Object)
 */
public class DockingPane extends JPanel {

	private static ResourceBundle resources = ResourceBundle
			.getBundle("swingx.docking.resources");

	private DragSource dragSource = new DragSource();

	private DragDropHandler dragDropHandler = new DragDropHandler();

	private DialogHandler dialogHandler = new DialogHandler();

	private PopupHandler popupHandler = new PopupHandler();

	/**
	 * Currently handled keys by {@link DragDropHandler} or {@link PopupHandler}.
	 */
	private List<Object> keys = new ArrayList<Object>();

	private List<Docking> dockings = new ArrayList<Docking>();

	private Map<Docking, JDialog> dockingToDialog = new HashMap<Docking, JDialog>();

	private Map<JDialog, Docking> dialogToDocking = new HashMap<JDialog, Docking>();

	/**
	 * Create a new dockingPane.
	 */
	public DockingPane() {
		setLayout(new BorderLayout());

		Docking docking = createDocking();
		docking.setRoot(createDock());

		dockings.add(docking);
		add(docking);
	}

	public void addNotify() {
		super.addNotify();

		for (Docking docking : dockings) {
			updateVisibility(docking);
		}
	}

	public void removeNotify() {
		for (Docking docking : dockings) {
			updateVisibility(docking);
		}

		super.removeNotify();
	}

	public void updateUI() {
		super.updateUI();

		if (dockings == null) {
			// called from constructor
			return;
		}
		
		for (int d = 1; d < dockings.size(); d++) {
			Docking docking = dockings.get(d);
			JDialog dialog = dockingToDialog.get(docking);
			if (dialog == null) {
				SwingUtilities.updateComponentTreeUI(docking);
			} else {
				SwingUtilities.updateComponentTreeUI(dialog);
			}
		}

		SwingUtilities.updateComponentTreeUI(popupHandler.popupMenu);
	}

	protected List<Docking> getDockings() {
		return Collections.unmodifiableList(dockings);
	}

	protected void setDockings(List<Docking> dockings) {
		if (dockings == null || dockings.size() == 0) {
			throw new IllegalArgumentException("dockings must not be empty");
		}

		for (int d = this.dockings.size() - 1; d >= 0; d--) {
			Docking docking = this.dockings.get(d);
			docking.clearDockables();

			if (d == 0) {
				remove(docking);
			}
		}
		keys.clear();

		this.dockings = new ArrayList<Docking>(dockings);
		add(dockings.get(0));

		for (Docking docking : dockings) {
			updateVisibility(docking);
		}

		repaint();
		revalidate();
	}

	/**
	 * Test if a dockable under the given key is contained
	 * 
	 * @param key
	 *            key to test
	 * @return <code>true</code> if dockable is contained
	 */
	public boolean containsDockable(Object key) {
		for (Docking docking : dockings) {
			if (docking.getDock(key) != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns all keys of contained dockables.
	 * 
	 * @return keys
	 */
	public List<Object> getDockableKeys() {
		List<Object> keys = new ArrayList<Object>();

		for (Docking docking : dockings) {
			keys.addAll(docking.getDockableKeys());
		}

		return keys;
	}

	/**
	 * Get a dockable that is associated with the given key.
	 * 
	 * @param key
	 *            key to get dockable for
	 * @return dockable or <code>null</code> if no dockable is associated with
	 *         the given key
	 */
	public Dockable getDockable(Object key) {

		for (Docking docking : dockings) {
			Dock dock = docking.getDock(key);
			if (dock != null) {
				return dock.getDockable(key);
			}
		}
		return null;
	}

	/**
	 * Associate a dockable with the given key.
	 * 
	 * @param key
	 *            key to associate dockable with
	 * @param dockable
	 *            dockable to put, may be <code>null</code>
	 */
	public void putDockable(Object key, Dockable dockable) {
		if (key == null) {
			throw new IllegalArgumentException("key must not be null");
		}

		Dock dock = null;
		for (Docking docking : dockings) {
			dock = docking.getDock(key);
			if (dock != null) {
				break;
			}
		}

		if (dock == null) {
			Docking docking = dockings.get(0);

			dock = docking.getDock(null);
			if (dock == null) {
				dock = createDock();

				docking.slice(docking.getRoot(), dock);
			}
		}

		dock.putDockable(key, dockable);
		if (dockable != null) {
			dock.setSelectedDockable(dockable);
		}
	}

	/**
	 * Remove the dockable that is associated with the given key.
	 * 
	 * @param key
	 *            key to remove associated dockable for
	 * @return removed dockable or <code>null</code> if no dockable is
	 *         associated with the given key
	 */
	public Dockable removeDockable(Object key) {

		Dockable dockable = null;

		Dock dock = null;
		for (Docking docking : dockings) {
			dock = docking.getDock(key);
			if (dock != null) {
				break;
			}
		}
		if (dock != null) {
			dockable = dock.removeDockable(key);
		}

		keys.remove(key);

		return dockable;
	}

	/**
	 * The component associated with the given key.
	 * 
	 * @param key
	 *            key to get component for
	 * @return component or <code>null</code> if no component with is
	 *         assiciated with the given key
	 */
	public JComponent getComponent(Object key) {
		for (Docking docking : dockings) {
			Bridge bridge = docking.getBridge(key);
			if (bridge != null) {
				return bridge.getBridged();
			}
		}
		return null;
	}

	/**
	 * Associate a component with the given key.
	 * 
	 * @param key
	 *            key to associate component with
	 * @param component
	 *            component to put, may be <code>null</code>
	 */
	public void putComponent(Object key, JComponent component) {
		if (key == null) {
			throw new IllegalArgumentException("key must not be null");
		}

		Bridge bridge = null;
		for (Docking docking : dockings) {
			bridge = docking.getBridge(key);
			if (bridge != null) {
				break;
			}
		}

		if (bridge == null) {
			Docking docking = dockings.get(0);

			bridge = createBridge();
			docking.slice(docking.getRoot(), bridge);
		}
		bridge.setBridged(key, component);
	}

	/**
	 * Remove the component that is associated with the given key.
	 * 
	 * @param key
	 *            key to remove associated component for
	 * @return removed component or <code>null</code> if no component is
	 *         associated with the given key
	 */
	public JComponent removeComponent(Object key) {

		JComponent component = null;

		Bridge bridge = null;
		for (Docking docking : dockings) {
			bridge = docking.getBridge(key);
			if (bridge != null) {
				break;
			}
		}
		if (bridge != null) {
			component = bridge.clearBridged();
		}

		return component;
	}

	private Dock getDock(Component component) {
		while (!(component instanceof Dock)) {
			component = component.getParent();
		}
		return (Dock) component;
	}

	private Docking getDocking(Component component) {
		while (component != null && !(component instanceof Docking)) {
			component = component.getParent();
		}
		return (Docking) component;
	}

	/**
	 * Factory method to create a dialog to host a docking.
	 * 
	 * @return dialog created dialog
	 * @see #dismissDialog(JDialog)
	 */
	protected JDialog createDialog() {
		return new JDialog((JFrame) SwingUtilities
				.windowForComponent(DockingPane.this));
	}

	/**
	 * Hook method to dismiss a previously created dialog.
	 * 
	 * @param dialog
	 *            dialog to dismiss
	 * @see #createDialog()
	 */
	protected void dismissDialog(JDialog dialog) {
		dialog.dispose();
	}

	/**
	 * Factory method to create a new docking.
	 * 
	 * @return docking
	 */
	public final Docking createDocking() {
		Docking docking = createDockingImpl();
		docking.setDockingPane(this);

		new DropTarget(docking.getDropReceiver(), dragDropHandler);

		return docking;
	}

	/**
	 * Factory method to create a new slice.
	 * 
	 * @return slice
	 */
	public final Slice createSlice() {
		Slice slice = createSliceImpl();
		return slice;
	}

	/**
	 * Factory method to create a new dock.
	 * 
	 * @return dock
	 */
	public final Dock createDock() {
		Dock dock = createDockImpl();

		dock.setDockingPane(this);

		JComponent initiator = dock.getDragInitiator();
		initiator.addMouseListener(popupHandler);
		dragSource.createDefaultDragGestureRecognizer(initiator,
				DnDConstants.ACTION_MOVE, dragDropHandler);

		return dock;
	}

	/**
	 * Factory method to create a bridge.
	 * 
	 * @return bridge
	 */
	public final Bridge createBridge() {
		Bridge bridge = new Bridge();
		bridge.setDockingPane(this);
		return bridge;
	}

	protected Docking createDockingImpl() {
		return new Docking();
	}

	protected Slice createSliceImpl() {
		return new Slice();
	}

	protected Dock createDockImpl() {
		return new TabbedDock();
	}

	protected Bridge createBridgeImpl() {
		return new Bridge();
	}

	protected void bridgeChanged(Bridge bridge) {
		Docking docking = getDocking(bridge);
		if (dockings.contains(docking)) {
			if (!bridge.hasBridged()) {
				docking.unslice(bridge);
			}
			updateVisibility(docking);
		}
	}

	protected void dockChanged(Dock dock) {
		Docking docking = getDocking(dock);
		if (dockings.contains(docking)) {
			if (!dock.hasDockable()) {
				docking.unslice(dock);
			}
			updateVisibility(docking);
		}
	}

	protected void updateVisibility(Docking docking) {
		boolean hasVisible = docking.updateVisibility();

		if (docking != dockings.get(0)) {
			if (hasVisible) {
				if (isDisplayable()) {
					JDialog dialog = dockingToDialog.get(docking);
					if (dialog == null) {
						dialog = createDialog();
						dockingToDialog.put(docking, dialog);
						dialogToDocking.put(dialog, docking);

						dialog
								.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
						dialog.getContentPane().add(docking);
						dialog.pack();
						Point offset = SwingUtilities.convertPoint(docking
								.getRoot(), 0, 0, dialog);
						Rectangle screenBounds = docking.getScreenBounds();
						dialog.setLocation(screenBounds.x - offset.x,
								screenBounds.y - offset.y);
						dialog.addWindowListener(dialogHandler);
						dialog.addComponentListener(dialogHandler);
						dialog.setVisible(true);
					}
				}
			} else {
				JDialog dialog = dockingToDialog.get(docking);
				if (dialog != null) {
					dialog.setVisible(false);
					dialog.removeComponentListener(dialogHandler);
					dialog.removeWindowListener(dialogHandler);
					dialog.getContentPane().remove(docking);

					dockingToDialog.remove(docking);
					dialogToDocking.remove(dialog);
					dismissDialog(dialog);
				}

				if (!docking.hasDockable()) {
					dockings.remove(docking);
				}
			}
		}
	}

	protected class PopupHandler extends MouseAdapter implements ActionListener {

		private boolean popup;

		private Dock dock;

		/**
		 * The popup menu for closing.
		 */
		private JPopupMenu popupMenu = new JPopupMenu();

		private JMenuItem closeAllMenuItem = new JMenuItem(resources
				.getString("closeAll"));

		private JMenuItem closeOthersMenuItem = new JMenuItem(resources
				.getString("closeOthers"));

		private JMenuItem closeMenuItem = new JMenuItem(resources
				.getString("close"));

		private JMenuItem undockMenuItem = new JMenuItem(resources
				.getString("undock"));

		public PopupHandler() {
			closeMenuItem.addActionListener(this);
			popupMenu.add(closeMenuItem);
			closeOthersMenuItem.addActionListener(this);
			popupMenu.add(closeOthersMenuItem);
			closeAllMenuItem.addActionListener(this);
			popupMenu.add(closeAllMenuItem);
			popupMenu.addSeparator();
			undockMenuItem.addActionListener(this);
			popupMenu.add(undockMenuItem);
		}

		public void mousePressed(MouseEvent e) {
			popup = false;

			dock = getDock(e.getComponent());

			Point point = SwingUtilities.convertPoint(e.getComponent(), e
					.getPoint(), dock);

			keys.clear();
			Object key = dock.getDockableKey(point);
			if (key == null) {
				keys.addAll(dock.getVisibleDockableKeys());
			} else {
				keys.add(key);
			}

			checkPopup(e);
		}

		public void mouseReleased(MouseEvent e) {
			if (!popup) {
				checkPopup(e);
			}
		}

		private void checkPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				popup = true;

				closeMenuItem.setEnabled(keys.size() == 1);
				closeOthersMenuItem.setEnabled(keys.size() < dock
						.getVisibleDockableCount());
				closeAllMenuItem.setEnabled(keys.size() > 1);
				popupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		}

		public void actionPerformed(ActionEvent e) {
			if (closeMenuItem == e.getSource()) {
				for (int k = keys.size() - 1; k >= 0; k--) {
					dock.closeDockable(dock.getDockable(keys.get(k)));
				}
			} else if (closeOthersMenuItem == e.getSource()) {
				List<Object> visibles = dock.getVisibleDockableKeys();
				for (int k = visibles.size() - 1; k >= 0; k--) {
					if (!keys.contains(visibles.get(k))) {
						dock.closeDockable(dock.getDockable(visibles.get(k)));
					}
				}
			} else if (closeAllMenuItem == e.getSource()) {
				List<Object> visibles = dock.getVisibleDockableKeys();
				for (int k = visibles.size() - 1; k >= 0; k--) {
					dock.closeDockable(dock.getDockable(visibles.get(k)));
				}
			} else if (undockMenuItem == e.getSource()) {
				Rectangle bounds = new Rectangle();
				bounds.x = dock.getLocationOnScreen().x;
				bounds.y = dock.getLocationOnScreen().y;
				bounds.width = dock.getWidth();
				bounds.height = dock.getHeight();

				Dock undocked = createDock();
				for (int k = keys.size() - 1; k >= 0; k--) {
					Object key = keys.get(k);
					undocked.putDockable(key, dock.removeDockable(key));
				}

				Docking docking = createDocking();
				docking.setRoot(undocked);
				docking.setScreenBounds(bounds);

				dockings.add(docking);
				updateVisibility(docking);
			}
		}
	}

	protected class DialogHandler implements WindowListener, ComponentListener {

		// WindowListener
		public void windowActivated(WindowEvent e) {
		}

		public void windowDeactivated(WindowEvent e) {
		}

		public void windowDeiconified(WindowEvent e) {
		}

		public void windowIconified(WindowEvent e) {
		}

		public void windowOpened(WindowEvent e) {
		}

		public void windowClosed(WindowEvent e) {
		}

		public void windowClosing(WindowEvent e) {
			Docking docking = dialogToDocking.get(e.getWindow());

			List<Object> keys = docking.getDockableKeys();

			for (int k = 0; k < keys.size(); k++) {
				Object key = keys.get(k);
				Dock dock = docking.getDock(key);
				dock.closeDockable(dock.getDockable(key));
			}
		}

		// ComponentListener
		public void componentHidden(ComponentEvent e) {
		}

		public void componentShown(ComponentEvent e) {
		}

		public void componentMoved(ComponentEvent e) {
			updateScreenBounds(e);
		}

		public void componentResized(ComponentEvent e) {
			updateScreenBounds(e);
		}

		private void updateScreenBounds(ComponentEvent e) {
			Docking docking = dialogToDocking.get(e.getSource());

			if (docking.isShowing()) {
				// Component#getLocationOnScreen() throws
				// IllegalComponentStateException if not currently showing
				Point location = docking.getLocationOnScreen();

				Rectangle screenBounds = new Rectangle();
				screenBounds.x = location.x;
				screenBounds.y = location.y;
				screenBounds.width = docking.getWidth();
				screenBounds.height = docking.getHeight();

				docking.setScreenBounds(screenBounds);
			}
		}
	}

	protected class DragDropHandler implements DropTargetListener,
			DragSourceListener, DragGestureListener {

		private ObjectTransferable transferable;

		private Docking docking;

		private Dock drag;

		private JComponent drop;

		private int orientation = -1;

		private float weight;

		// DragGestureListener
		public void dragGestureRecognized(DragGestureEvent dge) {
			drag = getDock(dge.getComponent());
			drop = null;
			orientation = -1;

			Point point = SwingUtilities.convertPoint(dge.getComponent(), dge
					.getDragOrigin(), drag);

			keys.clear();
			Object key = drag.getDockableKey(point);
			if (key == null) {
				keys.addAll(drag.getVisibleDockableKeys());
			} else {
				keys.add(key);

				drag.setSelectedDockable(drag.getDockable(key));
			}

			for (Docking docking : dockings) {
				docking.startDrop();
			}

			transferable = new ObjectTransferable(keys);

			dge.startDrag(null, null, new Point(), transferable, this);
		}

		// DragSourceListener
		public void dragDropEnd(DragSourceDropEvent dsde) {
			transferable.clear();
			transferable = null;

			drag = null;
		}

		public void dragEnter(DragSourceDragEvent dsde) {
		}

		public void dragExit(DragSourceEvent dse) {
		}

		public void dragOver(DragSourceDragEvent dsde) {
		}

		public void dropActionChanged(DragSourceDragEvent dsde) {
		}

		// DropTargetListener
		public void dragEnter(DropTargetDragEvent dtde) {
			docking = getDocking(dtde.getDropTargetContext().getComponent());
		}

		public void dragOver(DropTargetDragEvent dtde) {
			Point location = dtde.getLocation();
			int x = location.x;
			int y = location.y;

			drop = getDrop(x, y, docking.getRoot());
			weight = getWeight();
			orientation = getOrientation(x, y);

			Rectangle rect = SwingUtilities.convertRectangle(drop.getParent(),
					drop.getBounds(), docking.dropLayer);
			docking.dropLayer.setMark(rect, orientation, weight);
		}

		public void dragExit(DropTargetEvent dte) {
			docking.dropLayer.clearMark();
			docking = null;

			drop = null;
		}

		public void dropActionChanged(DropTargetDragEvent dtde) {
		}

		public void drop(DropTargetDropEvent arg0) {
			if (drop != null) {
				if (orientation != SwingConstants.CENTER) {
					Dock newDock = createDock();

					Slice slice = docking.slice(drop, newDock);
					slice.setOrientation(orientation);
					slice.setWeight(weight);

					drop = newDock;
				}

				if (drag != drop
						|| drag.getVisibleDockableCount() > keys.size()) {
					Dockable selectedDockable = drag.getSelectedDockable();

					for (int k = keys.size() - 1; k >= 0; k--) {
						Object key = keys.get(k);

						Dockable dockable = drag.removeDockable(key);
						((Dock) drop).putDockable(key, dockable);
					}

					if (selectedDockable != null) {
						((Dock) drop).setSelectedDockable(selectedDockable);
					}
				}
			}

			drop = null;
			orientation = -1;

			for (Docking docking : dockings) {
				docking.endDrop();
			}
		}

		private int getOrientation(int x, int y) {
			int orientation = SwingConstants.CENTER;

			Point point = SwingUtilities.convertPoint(docking, x, y, drop);
			int width = drop.getWidth();
			int height = drop.getHeight();

			if (drag == drop) {
				if (drag.getVisibleDockableCount() == keys.size()) {
					return orientation;
				}
			}

			if (drop instanceof Dock) {
				if (point.x > width / 4 && point.x < width * 3 / 4
						&& point.y > height / 4 && point.y < height * 3 / 4) {
					return orientation;
				}
			}

			float xd = (float) (point.x - (width / 2)) / (width / 2);
			float yd = (float) (point.y - (height / 2)) / (height / 2);
			if (xd > 0) {
				if (yd > 0) {
					if (xd - yd > 0) {
						orientation = SwingConstants.RIGHT;
					} else {
						orientation = SwingConstants.BOTTOM;
					}
				} else {
					if (xd + yd > 0) {
						orientation = SwingConstants.RIGHT;
					} else {
						orientation = SwingConstants.TOP;
					}
				}
			} else {
				if (yd > 0) {
					if (yd + xd > 0) {
						orientation = SwingConstants.BOTTOM;
					} else {
						orientation = SwingConstants.LEFT;
					}
				} else {
					if (yd - xd > 0) {
						orientation = SwingConstants.LEFT;
					} else {
						orientation = SwingConstants.TOP;
					}
				}
			}
			return orientation;
		}

		private float getWeight() {
			return (drop instanceof Dock) ? 0.5f : 0.25f;
		}

		private JComponent getDrop(int x, int y, JComponent component) {

			x -= component.getX();
			y -= component.getY();

			if (component instanceof Slice) {
				Slice slice = (Slice) component;

				JComponent main = slice.getMain();
				JComponent remainder = slice.getRemainder();

				if (main.isVisible() && main.getX() < x
						&& main.getX() + main.getWidth() > x && main.getY() < y
						&& main.getY() + main.getHeight() > y) {
					return getDrop(x, y, main);
				}

				if (remainder.isVisible() && remainder.getX() < x
						&& remainder.getX() + remainder.getWidth() > x
						&& remainder.getY() < y
						&& remainder.getY() + remainder.getHeight() > y) {
					return getDrop(x, y, remainder);
				}
			}

			return component;
		}
	}
}