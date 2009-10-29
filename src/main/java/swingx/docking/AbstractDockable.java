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


/**
 * Abstract base class.
 */
public abstract class AbstractDockable implements Dockable {

	private Docked docked;

	public Docked getDocked() {
		return docked;
	}
	
	public void docked(Docked docked) {
		this.docked = docked;
	}

	/**
	 * Always allow closing.
	 * 
	 * @return returns <code>true</code> allways
	 */
	public boolean undocking() {
		return true;
	}

	/**
	 * Does nothing.
	 */
	public void undocked() {
		docked = null;
	}
	
	public boolean isDocked() {
		return docked != null;
	}
}
