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
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import swingx.docking.dock.TabbedDock;
import swingx.docking.persistence.XMLPersister;
import swingx.docking.ui.EclipseDockUI;

public class DockingDemo extends JFrame {

	private static Icon cup = null;

	private static boolean eclipseUI = false;

	static {
		try {
			cup = new ImageIcon(DockingDemo.class.getResource("cup.gif"));
		} catch (Exception ex) {
			throw new Error(ex);
		}
	}

	private DockingPane inner = new CustomizedDockingPane(0);

	private DockingPane outer = new CustomizedDockingPane(2);

	private JTextArea textArea = new JTextArea();

	private JCheckBoxMenuItem documentsMenuItem = new JCheckBoxMenuItem(
			"Show Documents");

	private JCheckBoxMenuItem eclipseUIMenuItem = new JCheckBoxMenuItem(
			"Eclipse UI");

	private DefaultDockable packageExplorer = createDockable(
			"Package Explorer", new JTree());

	private DefaultDockable outline = createDockable("Outline", new JTree());

	private DefaultDockable tasks = createDockable("Tasks", new JTable(10, 4));

	private DefaultDockable search = createDockable("Search", new JTextArea(
			"No simpler docking framework found."));

	private DefaultDockable console = createDockable("Console", textArea);

	public DockingDemo() {
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu menu = new JMenu("Dockable");
		menuBar.add(menu);

		menu.add(new Open());

		documentsMenuItem.setSelected(true);
		documentsMenuItem.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (documentsMenuItem.isSelected()) {
					outer.putComponent("Inner", inner);
				} else {
					outer.putComponent("Inner", null);
				}
			}
		});
		menu.add(documentsMenuItem);

		menu.addSeparator();

		JMenu showViewMenu = new JMenu("Show View");
		menu.add(showViewMenu);

		showViewMenu.add(new Show(packageExplorer));
		showViewMenu.add(new Show(outline));
		showViewMenu.add(new Show(tasks));
		showViewMenu.add(new Show(search));
		showViewMenu.add(new Show(console));

		menu.addSeparator();

		JMenu plafMenu = new JMenu("Look&Feel");
		menu.add(plafMenu);

		plafMenu.add(new SelectPLAF("Metal",
				"javax.swing.plaf.metal.MetalLookAndFeel"));
		plafMenu.add(new SelectPLAF("Windows",
				"com.sun.java.swing.plaf.windows.WindowsLookAndFeel"));
		plafMenu.add(new SelectPLAF("Motif",
				"com.sun.java.swing.plaf.motif.MotifLookAndFeel"));
		plafMenu.add(new SelectPLAF("GTK",
				"com.sun.java.swing.plaf.gtk.GTKLookAndFeel"));

		plafMenu.addSeparator();

		eclipseUIMenuItem.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				eclipseUI = eclipseUIMenuItem.isSelected();
				SwingUtilities.updateComponentTreeUI(DockingDemo.this);
			}
		});
		plafMenu.add(eclipseUIMenuItem);

		menu.addSeparator();

		menu.add(new Save());
		menu.add(new Load());

		try {
			Reader reader = new InputStreamReader(getClass()
					.getResourceAsStream("docking.xml"));
			DemoPersister persister = new DemoPersister(outer, reader);
			persister.load();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		getContentPane().add(outer, BorderLayout.CENTER);
	}

	private class Show extends AbstractAction {

		private DefaultDockable dockable;

		public Show(DefaultDockable dockable) {
			super(dockable.getTitle(), dockable.getIcon());

			this.dockable = dockable;
		}

		public void actionPerformed(ActionEvent e) {

			outer.putDockable(dockable.getTitle(), dockable);
		}
	}

	private class Save extends AbstractAction {

		public Save() {
			super("Save to Console");
		}

		public void actionPerformed(ActionEvent e) {

			try {
				StringWriter writer = new StringWriter();
				DemoPersister persister = new DemoPersister(outer, writer);
				persister.save();
				textArea.setText(writer.toString());
			} catch (IOException ex) {
				textArea.append(ex.getMessage());
			}

			outer.putDockable(console.getTitle(), console);
		}
	}

	private class Load extends AbstractAction {

		public Load() {
			super("Load from Console");
		}

		public void actionPerformed(ActionEvent e) {

			try {
				StringReader reader = new StringReader(textArea.getText());
				DemoPersister persister = new DemoPersister(outer, reader);
				persister.load();
			} catch (IOException ex) {
				textArea.append("\nException: " + ex.getMessage());
			}
		}
	}

	private class Open extends AbstractAction {

		private int i = 0;

		public Open() {
			super("Open document");
		}

		public void actionPerformed(ActionEvent e) {

			JTextArea textArea = new JTextArea();

			DefaultDockable dockable = new DefaultDockable();
			dockable.setTitle("Document" + i++);
			dockable.setIcon(cup);
			dockable.setContent(new JScrollPane(textArea));

			inner.putDockable(dockable.getTitle(), dockable);

			documentsMenuItem.setSelected(true);
		}
	}

	private class SelectPLAF extends AbstractAction {

		private String plaf;

		public SelectPLAF(String name, String plaf) {
			super(name);

			this.plaf = plaf;
		}

		public void actionPerformed(ActionEvent e) {
			try {
				UIManager.setLookAndFeel(plaf);

				SwingUtilities.updateComponentTreeUI(DockingDemo.this);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private DefaultDockable createDockable(final String name,
			final JComponent content) {

		DefaultDockable dockable = new DefaultDockable() {
			@Override
			public void docked(final Docked docked) {
				super.docked(docked);

				docked.addTool(new AbstractAction("Action") {
					public void actionPerformed(ActionEvent e) {
						docked.setStatus("Action performed");
					}
				});
			}
		};
		dockable.setTitle(name);
		dockable.setIcon(cup);
		dockable.setContent(new JScrollPane(content));
		return dockable;
	}

	public static void main(String[] args) {

		Toolkit.getDefaultToolkit().setDynamicLayout(true);

		DockingDemo demo = new DockingDemo();
		demo.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		demo.setSize(new Dimension(512, 512));
		demo.setVisible(true);
	}

	private class DemoPersister extends XMLPersister {

		public DemoPersister(DockingPane dockingPane, Reader reader) {
			super(dockingPane, reader, "1");
		}

		public DemoPersister(DockingPane dockingPane, Writer writer) {
			super(dockingPane, writer, "1");
		}

		protected JComponent resolveComponent(Object key) {
			if ("Inner".equals(key)) {
				return inner;
			}
			return null;
		}

		protected Dockable resolveDockable(Object key) {
			if (packageExplorer.getTitle().equals(key))
				return packageExplorer;
			if (outline.getTitle().equals(key))
				return outline;
			if (tasks.getTitle().equals(key))
				return tasks;
			if (search.getTitle().equals(key))
				return search;
			if (console.getTitle().equals(key))
				return console;

			return null;
		}
	}

	private class CustomizedDockingPane extends DockingPane {
		private int margin;

		public CustomizedDockingPane(int margin) {
			this.margin = margin;
		}

		protected Dock createDockImpl() {
			return new TabbedDock() {
				protected JTabbedPane createTabbedPane() {
					return new JTabbedPane() {
						public void updateUI() {
							if (eclipseUI) {
								setUI(new EclipseDockUI());
							} else {
								super.updateUI();
							}
						}
					};
				}
			};
		}

		protected Docking createDockingImpl() {
			Docking docking = super.createDockingImpl();
			docking.setBorder(new EmptyBorder(margin, margin, margin, margin));
			return docking;
		}
	}
}