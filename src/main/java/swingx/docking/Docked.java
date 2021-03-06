package swingx.docking;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;

public interface Docked {

	public void setTitle(String title);

	public void setIcon(Icon icon);

	public void setStatus(String status);
	
	public void setMenu(JMenu menu);
	
	public void setContent(JComponent component);

	public JButton addTool(Action action);

	public JComponent addTool(JComponent component);

	public void addToolSeparator();
	
	public void clearTools();
}
