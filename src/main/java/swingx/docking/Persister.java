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

import java.io.IOException;
import java.util.List;

import javax.swing.JComponent;

/**
 * A persister is able to load and savethe state of a <code>DockingPane</code>.
 * 
 * @see DockingPane
 */
public abstract class Persister {

	/**
	 * The dockable to persist state of.
	 */
	protected DockingPane dockingPane;

	/**
	 * Create a persister for the given <code>DockingPane</code>.
	 * 
	 * @param dockingPane
	 *            dockingPane to persist
	 */
	public Persister(DockingPane dockingPane) {
		this.dockingPane = dockingPane;
	}

	/**
	 * Load the state of the <code>DockingPane</code>.
	 * 
	 * @throws IOException
	 */
	public void load() throws IOException {
		// make sure old dockables are already undocked and dismissed as
		// loadDockings() might return identical instances
		for (Object key : dockingPane.getDockableKeys()) {
			dockingPane.removeDockable(key);
		}

		dockingPane.setDockings(loadDockings());
	}

	protected abstract List<Docking> loadDockings() throws IOException;

	/**
	 * Save the state of the <code>DockingPane</code>.
	 * 
	 * @throws IOException
	 */
	public void save() throws IOException {
		saveDockings(dockingPane.getDockings());
	}

	protected abstract void saveDockings(List<Docking> dockings)
			throws IOException;

	protected final Dockable resolveDockable(Object key) {
		return dockingPane.createDockable(key);
	}

	protected final JComponent resolveComponent(Object key) {
		return dockingPane.createComponent(key);
	}

	protected final Docking createDocking() {
		return dockingPane.createDocking();
	}

	protected final Slice createSlice() {
		return dockingPane.createSlice();
	}

	protected final Bridge createBridge() {
		return dockingPane.createBridge();
	}

	protected final Dock createDock() {
		return dockingPane.createDock();
	}
}