package swingx.docking.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.LayoutManager;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

public class EclipseDockUI extends BasicTabbedPaneUI {

	private boolean activated = false;
	private boolean focused = false;
	
    protected ColorSchema selectedColors;
    protected ColorSchema activeColors;
    protected ColorSchema inactiveColors;
    
    private FocusHandler focusHandler = new FocusHandler();
    
    protected void installListeners() {
    	super.installListeners();
    	
    	KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(focusHandler);
    }
    
    protected void uninstallListeners() {
    	KeyboardFocusManager.getCurrentKeyboardFocusManager().removePropertyChangeListener(focusHandler);

    	super.uninstallListeners();    	
    }
    
    protected void installDefaults() {
        super.installDefaults();
        
        tabInsets = new Insets(4, 4, 4, 4);
        tabAreaInsets = new Insets(0, 0, 0, 20);
        contentBorderInsets = new Insets(3,3,3,3);
        selectedTabPadInsets = new Insets(0, 0, 0, 0);
        
        selectedColors = new ColorSchema(UIManager.getColor("TabbedPane.foreground"),
        								 UIManager.getColor("TabbedPane.highlight"),
        								 UIManager.getColor("Panel.background"));

        activeColors = new ColorSchema(UIManager.getColor("InternalFrame.activeTitleForeground"),
        							   UIManager.getColor("InternalFrame.activeTitleBackground"),
        							   UIManager.getColor("InternalFrame.activeTitleGradient"));

        inactiveColors = new ColorSchema(UIManager.getColor("InternalFrame.inactiveTitleForeground"),
				   						 UIManager.getColor("InternalFrame.inactiveTitleBackground"),
				   						 UIManager.getColor("InternalFrame.inactiveTitleGradient"));
    }

    protected LayoutManager createLayoutManager() {
        return new EclipseLayout();
    }
    
    protected boolean shouldRotateTabRuns( int tabPlacement, int selectedRun ) {
        return false;
    }

    protected boolean shouldPadTabRun( int tabPlacement, int run ) {
        return false;
    }

    protected int getTabLabelShiftX(int tabPlacement, int tabIndex, boolean isSelected) {
        return 0;
    }
    
    protected int getTabLabelShiftY(int tabPlacement, int tabIndex,
            boolean isSelected) {
        return 0;
    }
    
    protected void paintContentBorder(Graphics g, int tabPlacement,
            int selectedIndex) {
        
        int width = tabPane.getWidth();
        int height = tabPane.getHeight();
        Insets insets = tabPane.getInsets();

        int tabAreaHeight = calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);

        int x = insets.left;
        int y = insets.top;
        int w = width - insets.right - insets.left;
        int h = height - insets.top - insets.bottom;
        
        g.setColor(getSelectionColors().getBackground2());
        g.fillRect(x, y + tabAreaHeight, w, h - tabAreaHeight);
        
        g.setColor(tabPane.getBackground().darker());
        g.drawLine(x        , y + 2    , x        , y + h - 1);
        g.drawLine(x + w - 1, y + 2    , x + w - 1, y + h - 1);
        g.drawLine(x        , y + h - 1, x + w - 1, y + h - 1);
        g.drawLine(x + 2    , y        , x + w - 3, y        );
        g.drawLine(x + 1    , y + 1    , x + 1    , y + 1    );
        g.drawLine(x + w - 2, y + 1    , x + w - 2, y + 1    );
        
        Rectangle selRect = selectedIndex < 0? null :
            getTabBounds(selectedIndex, calcRect);
        
        if (selRect == null) {
            g.drawLine(x, y + tabAreaHeight, x + w - 1, y + tabAreaHeight);
        } else {
            g.drawLine(x, y + tabAreaHeight, selRect.x, y + tabAreaHeight);
            g.drawLine(selRect.x + selRect.width - 1, y + tabAreaHeight, x + w - 1, y + tabAreaHeight);
        }
    }

    protected void paintTabBackground(Graphics g, int tabPlacement,
            int tabIndex, int x, int y, int w, int h, boolean isSelected) {
        
        if (isSelected) {
            Graphics2D g2 = (Graphics2D)g;
            Paint savePaint = g2.getPaint();

            ColorSchema colors = getSelectionColors();
            GradientPaint titleGradient = new GradientPaint(0,0, colors.getBackground1(),
                                                            0,h, colors.getBackground2());
            g2.setPaint(titleGradient);
            g.fillRect(x + 1, y + 1, w - 2, h);
            
            g2.setPaint(savePaint);
        }
    }
    
    protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
            int x, int y, int w, int h, boolean isSelected) {
        
        g.setColor(tabPane.getBackground().darker());
        if (isSelected) {
            g.drawLine(x        , y + 2    , x        , y + h - 1);
            g.drawLine(x + w - 1, y + 2    , x + w - 1, y + h - 1);
            g.drawLine(x + 2    , y        , x + w - 3, y        );
            g.drawLine(x + 1    , y + 1    , x + 1    , y + 1    );
            g.drawLine(x + w - 2, y + 1    , x + w - 2, y + 1    );
        } else {
            if (tabIndex != tabPane.getSelectedIndex() -1) {
                g.drawLine(x + w - 1, y        , x + w - 1, y + h - 1);
            }            
        }
    }
    
    protected void paintFocusIndicator(Graphics g, int tabPlacement,
            Rectangle[] rects, int tabIndex, Rectangle iconRect,
            Rectangle textRect, boolean isSelected) {
    }
    
    protected class EclipseLayout extends TabbedPaneLayout {
        protected void calculateTabRects(int tabPlacement, int tabCount) {
            FontMetrics metrics = getFontMetrics();
            Dimension size = tabPane.getSize();
            Insets insets = tabPane.getInsets(); 
            Insets tabAreaInsets = getTabAreaInsets(tabPlacement);
            
            int x = insets.left + tabAreaInsets.left;
            int y = insets.top  + tabAreaInsets.top;
            int w = size.width - insets.left - tabAreaInsets.left - insets.right - tabAreaInsets.right;
            
            runCount = 1;
            maxTabHeight = calculateMaxTabHeight(tabPlacement);
            maxTabWidth  = tabCount == 0 ? 0 : w / tabCount;
            
            Rectangle rect;
            for (int t = 0; t < tabCount; t++) {
                rect = rects[t];

                rect.x = x;
                rect.y = y;
                rect.width  = Math.min(maxTabWidth, calculateTabWidth(tabPlacement, t, metrics));
                rect.height = maxTabHeight;
                
                x += rect.width;
            }
        }
    }
    
    protected ColorSchema getSelectionColors() {
    	if (focused && activated) {
    		return activeColors;
    	} else if (focused) {
    		return inactiveColors;
    	} else {
        	return selectedColors;
    	}
    }
    
    private class ColorSchema {
    	private Color foreground;
    	private Color background1;
    	private Color background2;

    	public ColorSchema(Color foreground, Color background1, Color background2) {
        	this.foreground = foreground;
        	this.background1 = background1;
        	this.background2 = background2 == null ? background1 : background2;
    	}
    	
    	public Color getForeground() {
    		return foreground;
    	}
    	
    	public Color getBackground1() {
    		return background1;
    	}

    	public Color getBackground2() {
    		return background2;
    	}
    }
    
    private class FocusHandler implements PropertyChangeListener {
    	public void propertyChange(PropertyChangeEvent evt) {
    		Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
    		if (focusOwner == null) {
    			setFocused(false);
    		} else {
    			setFocused(focusOwner == tabPane || SwingUtilities.isDescendingFrom(focusOwner, tabPane));
    		}
    		Window activeWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
    		if (activeWindow == null) {
    			setActive(false);
    		} else {
    			setActive(activeWindow == SwingUtilities.getWindowAncestor(tabPane));
    		}
       	}    	
    }
    
	protected void setFocused(boolean focused) {
		if (focused != this.focused) {
			this.focused = focused;
			
    		tabPane.repaint();
		}
	}
	
	protected void setActive(boolean active) {
		if (active != this.activated) {
			this.activated = active;
			
    		tabPane.repaint();
		}
	}
}
