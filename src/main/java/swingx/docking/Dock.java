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

import java.awt.Point;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * A <code>Dock</code> is used by a {@link DockingPane} to group all dockables
 * that should be located on the same location.
 * 
 * @see #putDockable(Object, Dockable)
 * @see #removeDockable(Object)
 */
public abstract class Dock extends JPanel {

	private DockingPane dockingPane;

	public Dock(DockingPane dockingPane) {
		this.dockingPane = dockingPane;
	}

	public DockingPane getDockingPane() {
		return dockingPane;
	}
	
	/**
	 * Return the keys of all dockables.
	 * 
	 * @return keys
	 */
	public abstract List<Object> getDockableKeys();

	/**
	 * Test if this dock contains a dockable under the given key
	 * 
	 * @param key
	 *            key to test
	 * @return <code>true</code> if dockable is contained
	 */
	public abstract boolean containsDockable(Object key);

	/**
	 * Associate a dockable with the given key.
	 * 
	 * @param key
	 *            key to associate dockable with
	 * @param dockable
	 *            dockable to put, may be <code>null</code>
	 */
	public abstract Dockable putDockable(Object key, Dockable dockable);

	/**
	 * Get the selected dockable.
	 * 
	 * @return the currently selected dockable
	 */
	public abstract Dockable getSelectedDockable();

	/**
	 * Set the selected dockable.
	 * 
	 * @param dockable
	 *            the new selected dockable
	 */
	public abstract void setSelectedDockable(Dockable dockable);

	/**
	 * Get a dockable that is associated with the given key.
	 * 
	 * @param key
	 *            key to get dockable for
	 * @return dockable or <code>null</code> if no dockable is associated with
	 *         the given key
	 */
	public abstract Dockable getDockable(Object key);

	/**
	 * Remove the dockable that is associated with the given key.
	 * 
	 * @param key
	 *            key to remove associated dockable for
	 * @return removed dockable or <code>null</code> if no dockable is
	 *         associated with the given key
	 */
	public abstract Dockable removeDockable(Object key);

	/**
	 * Test if any dockables are contained.
	 * 
	 * @return <code>true</code> if a dockable is contained
	 */
	public abstract boolean hasDockable();

	/**
	 * Get count of contained dockables.
	 * 
	 * @return dockables count
	 */
	public abstract int getDockableCount();

	/**
	 * Get the keys of all visible (i.e. non-null) dockables.
	 * 
	 * @return the visible dockables' keys
	 */
	public abstract List<Object> getVisibleDockableKeys();

	/**
	 * Get the count of visible (i.e. non-null) dockables.
	 * 
	 * @return count
	 */
	public abstract int getVisibleDockableCount();

	/**
	 * Hook method to get the drag initiating component.
	 * 
	 * @return component that initiates drags
	 */
	public abstract JComponent getDragInitiator();

	/**
	 * Close the given dockable.
	 * 
	 * @param dockable
	 *            dockable to close
	 */
	public abstract void closeDockable(Dockable dockable);

	/**
	 * Inform the containing docking pane about a change.
	 */
	protected void fireDockChanged() {
		if (dockingPane != null) {
			dockingPane.dockChanged(this);
		}
	}

	/**
	 * Get the key of the dockable for the given point.
	 * 
	 * @param point
	 *            point to get key for
	 * @return dockabley key
	 */
	public abstract Object getDockableKey(Point point);
}