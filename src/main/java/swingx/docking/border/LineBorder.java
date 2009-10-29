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
package swingx.docking.border;

import java.awt.*;
import javax.swing.border.*;

/**
 * A line border.
 */
public class LineBorder implements Border {

  private static final Insets insets = new Insets(0, 0, 1, 0);
  
  /**
   * Paint.
   */
  public void paintBorder(Component c, Graphics g,
                          int x, int y, int width, int height) {

    g.setColor(getShadowColor(c));
    g.drawLine(x, y + height - 1, x + width - 1, y + height - 1);
  }

  public Insets getBorderInsets(Component c) {
    return insets;
  }

  public boolean isBorderOpaque() {
    return true;
  }

  /**
   * Returns a shadow color of the specified component.
   *
   * @param c the component for which the shadow shall be derived
   */
  protected Color getShadowColor(Component c)   {
    return c.getBackground().darker();
  }
}