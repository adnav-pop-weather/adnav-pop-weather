package polygonHelper;
import graph1.Node;
import java.util.ArrayList;
import java.util.Queue;

/**
 * Created by Daniel Griessler Spring 2019
 * Creates polygons on map needed for GPS testing whether in populated area or not
 * To modify: create polygons using GPS coordinates.  I used SkyVector to place GPS points on map.  Then I opened
 * the generated navlog and copied in HTML into a test file.  There is one line in the html that includes the text with
 * all of the coordinates.  Use parse to separate and print all of the instructions.  They can be easily copy pasted into here.
 *
 * The calculation itself is courtesy of Eric Leschinski who answered a question on stack overflow found at: https://stackoverflow.com/questions/4287780/detecting-whether-a-gps-coordinate-falls-within-a-polygon-on-a-map
 * Other modifications: Daniel Griessler Spring 2019
 */
public class insidePolygon {
    public static double PI = 3.14159265;
    private static final double TWO_PI = 2 * PI;

    /**
     * Calculates if a provided gps coordinate is inside an ArrayList of gps coordinates which define a polygon
     * Unedited from source linked at the top in the comments
     *
     * @param latitude  the tested latitude
     * @param longitude the tested longitude
     * @param polygon   the polygon to be checked
     * @return if the provided point is inside the provided polygon
     */
    public static boolean coordinate_is_inside_polygon(double latitude, double longitude, Node polygon) {
        int i;
        double angle = 0;
        double point1_lat;
        double point1_long;
        double point2_lat;
        double point2_long;
        int n = countNodesInsidePolygon(polygon);

        Node current = polygon;

        for (i = 0; i < n; i++) {
            point1_lat = current.getCoordinates()[0] - latitude;
            point1_long = current.getCoordinates()[1] - longitude;
            point2_lat = current.edges.get(1).destination.getCoordinates()[0] - latitude;
            point2_long = current.edges.get(1).destination.getCoordinates()[1] - longitude;
            angle += Angle2D(point1_lat, point1_long, point2_lat, point2_long);
            current = current.edges.get(1).destination;
        }

        return !(Math.abs(angle) < PI);
    }

    /**
     * Utility function for checking if a given coordinate is inside a polygon
     *
     * @param y1 one of the latitudes
     * @param x1 one of the longitudes
     * @param y2 the other latitude
     * @param x2 the other longitude
     * @return angle
     */
    private static double Angle2D(double y1, double x1, double y2, double x2) {
        double dtheta, theta1, theta2;

        theta1 = Math.atan2(y1, x1);
        theta2 = Math.atan2(y2, x2);
        dtheta = theta2 - theta1;
        while (dtheta > PI)
            dtheta -= TWO_PI;
        while (dtheta < -PI)
            dtheta += TWO_PI;

        return (dtheta);
    }

    public static int countNodesInsidePolygon(Node start_node) {
        Node node = start_node;
        int counter = 0;
        node = node.edges.get(1).getDestination();
        counter++;

        while (node != start_node) {
            node = node.edges.get(1).getDestination();
            counter++;
        }

        return counter;
    }
    
    
    public Queue<Node> passPolygonToQueue(Node currentLocation, ArrayList<Node> polygonArray, Queue<Node> polygon_queue) {

        Node last_added = null;

        for (Node node : polygonArray) {
            if (coordinate_is_inside_polygon(currentLocation.getCoordinates()[0], currentLocation.getCoordinates()[1], node)) {
                last_added = addPolygonToQueue(node, polygon_queue, last_added);
                polygon_queue.peek().isInitiallyInside = true;
                currentLocation = currentLocation.edges.get(0).destination;
            }
        }

        while(currentLocation.edges.get(0).destination.edges.size() > 0 && currentLocation.edges.get(0).destination != null){
            for (Node node : polygonArray) {
                if (coordinate_is_inside_polygon(currentLocation.getCoordinates()[0], currentLocation.getCoordinates()[1], node)) {
                    //System.out.println("True at latitude: " + currentLocation.getCoordinates()[0] +" and longitude: "+ currentLocation.getCoordinates()[1]  + " for polygon: " + i + " " + polygonArray.get(i).getCoordinates()[0]+  " " +  polygonArray.get(i).getCoordinates()[1]);
                    last_added = addPolygonToQueue(node, polygon_queue, last_added);
                }
            }
            //System.out.println("latitude: "+currentLocation.getCoordinates()[0] +" and longitude: "+ currentLocation.getCoordinates()[1]);
            currentLocation = currentLocation.edges.get(0).destination;
        }
        return polygon_queue;
    }

    public Node addPolygonToQueue(Node polygon, Queue<Node> polygon_queue, Node last_added){
        if((polygon_queue.isEmpty()) || (last_added != polygon)){
            polygon_queue.add(polygon);
        }
        return polygon;
    }
}
