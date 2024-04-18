package xplane.InterfacePanel;

import xplane.InterfacePanel.mapBoxAPI.mapbox;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;

import static com.sun.speech.freetts.InputMode.URL;

public class MapPanel extends JPanel implements Runnable {
    private ImageIcon map;
    private JLabel mapLabel;
    private Image image = null;

    private double[] currentCoords;
    private boolean showRoute;

    public MapPanel (double[] coordinate, boolean showLocRoute) {
        currentCoords = coordinate;
        showRoute = showLocRoute;

        if (coordinate != null) {
            Image image = null;
            String mapEndpoint = new mapbox().createEndpoint(coordinate[1], coordinate[0], showLocRoute);

            try {
                java.net.URL url = new URL(mapEndpoint);
                image = ImageIO.read(url);
                map = new ImageIcon(image);
                mapLabel = new JLabel(map);
                mapLabel.setBounds(0, 500, 500, 1000);

                add(mapLabel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateMapPanel(double[] coordinate, boolean showLocRoute) {
        currentCoords = coordinate;
        showRoute = showLocRoute;

        if (coordinate != null) {
            Image image = null;
            String mapEndpoint = new mapbox().createEndpoint(coordinate[1], coordinate[0], showLocRoute);

            try {
                java.net.URL url = new URL(mapEndpoint);
                image = ImageIO.read(url);
                map.setImage(image);
                mapLabel.setIcon(map);

                add(mapLabel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(10000);
                updateMapPanel(currentCoords, showRoute);
                revalidate();
                repaint();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
