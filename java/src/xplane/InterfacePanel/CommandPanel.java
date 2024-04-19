package xplane.InterfacePanel;

import pathFinder.newPathfinder;
import util.CustomRadioButtonIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Set;

public class CommandPanel extends JPanel implements Runnable, ActionListener {

    private newPathfinder pathfinder = newPathfinder.getPathfinderInstance();
    private boolean strLandProd = false, strTakeOffProd = false;
    private static boolean dpLOC= true, strLOC = false, showLocRoute = false;
    private final JRadioButton radio1, radio2, radio3;
    private final JButton takeoffButton, landingButton, locButton;
    private final JLabel currentCoordinates, nextCoordinates, distance;
    private final JLabel aircraftSPD, aircraftALT, aircraftETL, descendRate;
    private final JLabel fuel1, fuel2, fuel3, fuelMeter;
    private final JProgressBar fuelBar;
    private int[] aircraftDetails;
    private double latitude, longitude, nextLatitude, nextLongitude, targetDist;
    private float[] fuelTank;
    private float maxFuel;
    private boolean updateTank = true, updateUI = true;
    private final ArrayList<String> cities = new ArrayList<>(Arrays.asList("Dallas", "Wichita", "Oklahoma"));
    private int cityIndex = 0;

    public CommandPanel(double[] coordinate, double[] positions, double distanceToTarget, int[] flightDetails, float[] fuelCount) {
        latitude = coordinate[0];
        longitude = coordinate[1];
        nextLatitude = positions[0];
        nextLongitude = positions[1];
        targetDist = distanceToTarget;
        aircraftDetails = flightDetails;
        fuelTank = fuelCount;

        // Font Style
        final Font subText = new Font("Courier", Font.PLAIN, 16);
        final Font textFont1 = new Font("Courier", Font.BOLD, 25);
        final Font textFont2 = new Font("Courier", Font.PLAIN, 20);

        // Write the label on the gui in the upper half
        descendRate = new JLabel("Descent Rate: " + aircraftDetails[0] + "ft/min");
        aircraftSPD = new JLabel("Airspeed: " + aircraftDetails[1] + "ft above Ground");
        aircraftALT = new JLabel("Altitude: " + aircraftDetails[2] + "ft above Sea Level");
        aircraftETL = new JLabel("Elevation: " + aircraftDetails[3] + "ft above Ground");
        currentCoordinates = new JLabel(String.format("Position:%s, %s", formatPosition(latitude), formatPosition(longitude)));
        nextCoordinates = new JLabel(String.format("Position:%s, %s", formatPosition(nextLatitude), formatPosition(nextLongitude)));
        distance = new JLabel(String.format("Distance to target: %.4f Km", targetDist));
        fuel1 = new JLabel("Fuel 1: " + (fuelTank == null ? 0.00 : Math.floor(fuelTank[0]* 100)/100d) + "lbs");
        fuel2 = new JLabel("Fuel 2: " + (fuelTank == null ? 0.00 : Math.floor(fuelTank[1]* 100)/100d) + "lbs");
        fuel3 = new JLabel("Fuel 3: " + (fuelTank == null ? 0.00 : Math.floor(fuelTank[2]* 100)/100d) + "lbs");

        // Fuel Meter
        String barString = getFuelBarString(fuelTank == null ? 0 : fuelTank[0], fuelTank == null ? 0 : fuelTank[1], fuelTank == null ? 0 : fuelTank[2]);
        fuelMeter = new JLabel(barString);
        fuelBar = new JProgressBar();
        fuelBar.setSize(50,50);
        fuelBar.setVisible(true);

        // Button on the gui
        radio1 = new JRadioButton("Dallas");
        radio2 = new JRadioButton("Wichita");
        radio3 = new JRadioButton("Oklahoma");
        takeoffButton = new JButton("Initiate Take off");
        landingButton = new JButton("Initiate Landing");
        locButton = new JButton("Lose Communication");

        radio1.addActionListener(this);
        radio2.addActionListener(this);
        radio3.addActionListener(this);

        takeoffButton.addActionListener(this);
        takeoffButton.setFocusPainted(false);
        takeoffButton.setContentAreaFilled(false);
        takeoffButton.setOpaque(true);
        takeoffButton.setBackground(new Color(211,211,211));
        takeoffButton.setFont(new Font("Arial", Font.BOLD, 20));
        takeoffButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        landingButton.addActionListener(this);
        landingButton.setFocusPainted(false);
        landingButton.setContentAreaFilled(false);
        landingButton.setOpaque(true);
        landingButton.setBackground(new Color(211,211,211));
        landingButton.setFont(new Font("Arial", Font.BOLD, 20));
        landingButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        locButton.addActionListener(this);
        locButton.setFocusPainted(false);
        locButton.setContentAreaFilled(false);
        locButton.setOpaque(true);
        locButton.setBackground(new Color(211,211,211));
        locButton.setFont(new Font("Arial", Font.BOLD, 20));
        locButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));


        // Gui labels
        Hashtable<String, int[]> labels = new Hashtable<String, int[]>(){{
            put("Flight Data", new int[]{650, 95});
            put("Command Panel", new int[]{385, 25});
            put("Fuel", new int[]{567, 125});
            put("Current Location", new int[]{480, 370});
            put("Next Location", new int[]{730, 370});
            put("Choose Destination", new int[]{110, 370});
            put("Operations", new int[]{160, 95});
            put(" |", new int[]{500, 147});
            put("| ", new int[]{670, 147});
        }};

//        takeOffButton = new button("Initiate Take off",80,190, 300, 30, Color.green);
//        LandButton = new button("Initiate Landing",80, 235, 300, 30, Color.cyan);
//        LOCButton = new button("Lose Communication",80, 280, 300, 30, Color.red);

        radio1.setFocusPainted(false);
        radio2.setFocusPainted(false);
        radio3.setFocusPainted(false);

        radio1.setIcon(new CustomRadioButtonIcon().customRadioButton());
        radio2.setIcon(new CustomRadioButtonIcon().customRadioButton());
        radio3.setIcon(new CustomRadioButtonIcon().customRadioButton());

        takeoffButton.setVisible(false);
        landingButton.setVisible(false);
        locButton.setVisible(false);

        takeoffButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                takeoffButton.setBackground(Color.GREEN);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                takeoffButton.setBackground(new Color(211, 211, 211));
            }
        });

        landingButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                landingButton.setBackground(Color.CYAN);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                landingButton.setBackground(new Color(211, 211, 211));
            }
        });

        locButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                locButton.setBackground(Color.RED);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                locButton.setBackground(new Color(211, 211, 211));
            }
        });

        takeoffButton.setBounds(80, 160, 300, 40);
        landingButton.setBounds(80, 220, 300, 40);
        locButton.setBounds(80, 280, 300, 40);
        radio1.setBounds(50,424, 100, 30);
        radio2.setBounds(290,424, 130, 30);
        radio3.setBounds(155,424, 150, 30);
        fuelMeter.setBounds(515, 155, 400, 30);
        fuel1.setBounds(750, 125, 400, 30);
        fuel2.setBounds(750, 155, 400, 30);
        fuel3.setBounds(750, 185, 400, 30);
        descendRate.setBounds(580, 330, 300, 20);
        aircraftSPD.setBounds(580, 300, 300, 20);
        aircraftALT.setBounds(580, 270, 300, 20);
        aircraftETL.setBounds(580, 240, 300, 20);
        currentCoordinates.setBounds(480, 415, 300, 20);
        nextCoordinates.setBounds(730, 415, 300, 20);
        distance.setBounds(730, 435, 300, 20);

        radio1.setFont(textFont2);
        radio2.setFont(textFont2);
        radio3.setFont(textFont2);
        fuelMeter.setFont(textFont1);
        fuel1.setFont(textFont1);
        fuel2.setFont(textFont1);
        fuel3.setFont(textFont1);
        descendRate.setFont(textFont2);
        aircraftSPD.setFont(textFont2);
        aircraftALT.setFont(textFont2);
        aircraftETL.setFont(textFont2);
        currentCoordinates.setFont(subText);
        nextCoordinates.setFont(subText);
        distance.setFont(subText);

        ButtonGroup group = new ButtonGroup();
        group.add(radio1);
        group.add(radio2);
        group.add(radio3);

        add(takeoffButton);
        add(landingButton);
        add(locButton);
        add(radio1);
        add(radio2);
        add(radio3);
        add(fuelMeter);
        add(fuel1);
        add(fuel2);
        add(fuel3);
        add(descendRate);
        add(aircraftSPD);
        add(aircraftALT);
        add(aircraftETL);
        add(currentCoordinates);
        add(nextCoordinates);
        add(distance);

        // Add all the labels at once
        Set<String> keys = labels.keySet();
        for(String key: keys) {
            JLabel label = new JLabel(key);
            label.setBounds(labels.get(key)[0], labels.get(key)[1], 300, 42);
            label.setFont(textFont1);
            add(label);
        }
        revalidate();
    }

    // Updates the command panel
    public void updateLabel(double[] coordinate, double[] positions, double distanceToTarget, int[] flightDetails, float[] fuelCount) {
        latitude = coordinate[0];
        longitude = coordinate[1];
        nextLatitude = positions[0];
        nextLongitude = positions[1];
        targetDist = distanceToTarget;
        aircraftDetails = flightDetails;
        fuelTank = fuelCount;

        fuel1.setText("Fuel 1: "+ (fuelTank == null ? 0.00 : Math.floor(fuelTank[0]* 100)/100d) +"lbs");
        fuel2.setText("Fuel 2: "+ (fuelTank == null ? 0.00 : Math.floor(fuelTank[1]* 100)/100d) +"lbs");
        fuel3.setText("Fuel 3: "+ (fuelTank == null ? 0.00 : Math.floor(fuelTank[2]* 100)/100d) +"lbs");

        if (fuelTank != null && updateTank) {
            maxFuel = fuelTank[0] +  fuelTank[1] + fuelTank[2];
            updateTank = false;
        }

        String barString = getFuelBarString(fuelTank == null ? 0 : fuelTank[0], fuelTank == null ? 0 : fuelTank[1], fuelTank == null ? 0 : fuelTank[2]);

        fuelMeter.setText(barString);
        currentCoordinates.setText(String.format("%s, %s", formatPosition(latitude), formatPosition(longitude)));
        nextCoordinates.setText(String.format("%s, %s", formatPosition(nextLatitude), formatPosition(nextLongitude)));
        distance.setText(String.format("Distance to target: %.4f Km", targetDist));
        descendRate.setText("Descent Rate: " + aircraftDetails[0] + "ft/min");
        aircraftSPD.setText("Airspeed: " + aircraftDetails[1] + "ft above Ground");
        aircraftALT.setText("Altitude: " + aircraftDetails[2] + "ft above Sea Level");
        aircraftETL.setText("Elevation: " + aircraftDetails[3] + "ft above Ground");
        revalidate();
    }

    private void showOperationButton() {
        takeoffButton.setVisible(true);
        landingButton.setVisible(true);
        locButton.setVisible(true);
        updateUI = false;
        cityIndex = getSelectedCity();
        // Remove as needed
        remove(radio1);
        remove(radio2);
        remove(radio3);
        JLabel destination = new JLabel("Selected Destination: " + cities.get(cityIndex));
        destination.setBounds(95, 430, 300, 30);
        destination.setFont(new Font("Courier", Font.PLAIN, 20));
        add(destination);
        revalidate();
    }

    public int getCityIndex() {
        return cityIndex;
    }

    public boolean[] getConditionFlag() {
        return new boolean[] {strTakeOffProd, strLandProd, dpLOC, strLOC, showLocRoute};
    }

    private int getSelectedCity() {
        int currentIndex = 0;
        if (radio1.isSelected()) {
            currentIndex  = cities.indexOf(radio1.getText());
        }
        if (radio2.isSelected()) {
            currentIndex  = cities.indexOf(radio2.getText());
        }
        if (radio3.isSelected()) {
            currentIndex  = cities.indexOf(radio3.getText());
        }
        return currentIndex ;
    }

    // determines the percentage of fuel left
    private String getFuelBarString(float tank1, float tank2, float tank3){
        float currentTotalFuel = tank1 + tank2 + tank3;
        float currentFuelPercentage = (currentTotalFuel/maxFuel)*100;
        int barCount = (int)(currentFuelPercentage/10);
        StringBuilder barString = new StringBuilder();

        for (int i = 0; i < barCount; i++) {
            barString.append("=");
        }
        return barString.toString();
    }

    private String formatPosition(double pos){
        String ans = "";
        ans += (int)pos + "°";

        if (pos < 0) {
            pos *= -1;
        }
        ans += (int)(pos*60)%60 + "'";
        ans += (int)(pos*3600)%60+"\"";
        return ans;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(6);
                // Update the text of the labels
                updateLabel(new double[]{this.latitude, this.longitude}, new double[]{nextLatitude, nextLongitude}, targetDist, aircraftDetails, fuelTank);
                revalidate();
                repaint();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (radio1.isSelected() && updateUI) {
            showOperationButton();
        }
        if (radio2.isSelected() && updateUI) {
            showOperationButton();
        }
        if (radio3.isSelected() && updateUI) {
            showOperationButton();
        }
        if (e.getSource() == takeoffButton) {
            remove(takeoffButton);
            strTakeOffProd = true;
        }

        if (e.getSource() == locButton) {
            strLOC = true;
            showLocRoute = true;
            dpLOC = false;
            remove(locButton);
        }

        if (e.getSource() == landingButton) {
            strLandProd = true;
            remove(landingButton);
        }

        repaint();
        revalidate();
    }

    public void paint(Graphics g) {
        super.paint(g);
        g.fillRect(450,75,4,525); //vertical line in middle
        g.fillRect(0,75,1000,4); // line underneath of command pannel
        g.fillRect(450,225,550,4); //below fuel
        g.fillRect(0,365,1000,4); //above current location
        g.fillRect(0,482,1000,4); //above map
    }
}
