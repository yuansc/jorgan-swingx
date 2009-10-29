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

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenu;

/**
 * Convenience implementation of a dockable.
 */
public class DefaultDockable extends AbstractDockable {

	/**
	 * The name.
	 */
	private String title;

	/**
	 * The icon.
	 */
	private Icon icon;

	/**
	 * The status.
	 */
	private String status;

	/**
	 * The menu.
	 */
	private JMenu menu;

	/**
	 * The component.
	 */
	private JComponent content;

	public void setTitle(String title) {
		this.title = title;
		
		if (isDocked()) {
			getDocked().setTitle(title);
		}
	}

	public String getTitle() {
		return title;
	}

	public void setIcon(Icon icon) {
		this.icon = icon;

		if (isDocked()) {
			getDocked().setIcon(icon);
		}
	}

	public Icon getIcon() {
		return icon;
	}

	public void setStatus(String status) {
		this.status = status;

		if (isDocked()) {
			getDocked().setStatus(status);
		}
	}

	public String getStatus() {
		return status;
	}

	public JMenu getMenu() {
		return menu;
	}

	public void setMenu(JMenu menu) {
		this.menu = menu;
	}

	public void setContent(JComponent content) {
		this.content = content;

		if (isDocked()) {
			getDocked().setContent(content);
		}
	}

	public JComponent getContent() {
		return content;
	}

	@Override
	public void docked(Docked docked) {
		super.docked(docked);

		docked.setTitle(title);
		docked.setIcon(icon);
		docked.setStatus(status);
		docked.setContent(content);

		addTools(docked);
	}

	protected void addTools(Docked docked) {

	}
}