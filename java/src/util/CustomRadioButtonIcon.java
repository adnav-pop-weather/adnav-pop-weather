package util;

import javax.swing.*;
import java.awt.*;

public class CustomRadioButtonIcon {
    public Icon customRadioButton () {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                ButtonModel model = ((AbstractButton) c).getModel();
                if (model.isSelected()) {
                    g.setColor(Color.GREEN); // set fill color
                    g.fillOval(1, 4,  getIconWidth(), getIconHeight()); // draw circle
                    g.setColor(Color.BLACK);
                    g.drawOval(1, 4,  getIconWidth(), getIconHeight());
                } else {
                    g.setColor(Color.GRAY);
                    g.drawOval(1, 4,  getIconWidth(), getIconHeight());
                }
            }

            @Override
            public int getIconWidth() {
                return 20;
            }

            @Override
            public int getIconHeight() {
                return 20;
            }
        };
    }
}
