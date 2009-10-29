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
     * @param dockingPane   dockingPane to persist
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

    protected abstract void saveDockings(List<Docking> dockings) throws IOException;
    
    /**
     * Resolve a {@link Dockable} on load of the <code>DockingPane</code>
     * if it was non-null on the previous save.
     * 
     * @param key   key of dockable to resolve 
     * @return      dockable or <code>null</code>
     */
    protected Dockable resolveDockable(Object key) {
        return null;
    }
    
    /**
     * Resolve a <code>JComponent</code> on load of the <code>DockingPane</code>
     * if it was non-null on the previous save.
     * 
     * @param key   key of component to resolve 
     * @return      component or <code>null</code>
     */
    protected JComponent resolveComponent(Object key) {
        return null;
    }

    protected Docking createDocking() {
        return dockingPane.createDocking();
    }
    
    protected Slice createSlice() {
        return dockingPane.createSlice();
    }
    
    protected Bridge createBridge() {
        return dockingPane.createBridge();
    }

    protected Dock createDock() {
        return dockingPane.createDock();
    }
}