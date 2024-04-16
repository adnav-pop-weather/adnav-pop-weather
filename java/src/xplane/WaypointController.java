package xplane;

import coordinateParser.TextParser;
import graph1.Edge;
import graph1.Node;
import polygonHelper.insidePolygon;
import pathFinder.pathGenerator;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class WaypointController {
    private static WaypointController single_instance = null;
    public static double THRESHOLD = 3.5;
    public static ArrayList<Node> polygonArray;
    public Queue<Node> polygon_queue = new LinkedList<>();
    public static ArrayList<ArrayList<Node>> waypoints = new ArrayList<>();
    public static ArrayList<ArrayList<Node>> initialWaypoints = new ArrayList<>();
    public Hashtable<String, double[]> cityCoordinates = new Hashtable<String, double[]>(){{
        put("Dallas",new double[]{32.913056,-97.027778}); put("Wichita",new double[]{33.990278,-98.491389}); put("Oklahoma",new double[]{35.393056,-97.602778});
    }};
    public static int currentWaypointIndex = 0;
    public static int currentPolygonIndex = 0;
    private static final double EARTH_RADIUS = 6371.0;
    private static final double HEADING_NODE_COUNT = 100.0;
    private static boolean initialPolygonTextRead = true;
    public static double[] lastRecordedSpot;
    private boolean first_call = true, first_loc = true;
    public static WaypointController getSingle_instance(){
        if(single_instance == null){
            single_instance = new WaypointController();
        }
        return single_instance;
    }

    public WaypointController() {
//      Simulates pilot travel in a short distance
        ArrayList<Node> defaultWaypoints = new ArrayList<>();
        defaultWaypoints.add(new Node(new double[]{32.981510,-97.081169}));
        defaultWaypoints.add(new Node(new double[]{33.042831,-97.202705}));
        defaultWaypoints.add(new Node(new double[]{33.115757,-97.274288}));
        defaultWaypoints.add(new Node(new double[]{33.133440,-97.434448})); // Default Landing zone - Love Field
//        defaultWaypoints.add(new Node(new double[]{33.131284,-97.555984})); // option 1 Landing
//        defaultWaypoints.add(new Node(new double[]{33.129990,-97.598213})); // option 2 Landing
//        defaultWaypoints.add(new Node(new double[]{33.129808,-97.659302})); // option 3 Landing

        if(first_call) {
            waypoints.add(defaultWaypoints);
            initialWaypoints.add(defaultWaypoints);
            first_call = false;
        }
    }
    public void setInitialPolygonFileLoaded() throws IOException {
        if(initialPolygonTextRead){
            polygonArray = getPolygonsFromTxtFile();
            initialPolygonTextRead = false;
        }
    }

    public void updateNextWaypoints(double lat, double lon){
        System.err.println(currentWaypointIndex);
        // If plane is within threshold (1/2 km) from waypoint, then update waypoint to next point
        if (getDistanceToWaypoint(lat,lon) <= THRESHOLD) {
            if (currentWaypointIndex == waypoints.get(currentPolygonIndex).size()-1) {
                currentWaypointIndex = 0;
                currentPolygonIndex += 1;
            }
            else{
                currentWaypointIndex += 1;
            }
        }
    }

    //Upon completion of algorithm place in here. Pati says this is the spot!!!!
    public void updateWaypointList(String CityDestination, double[] startCoordinates) {
//        System.err.println(  cityCoordinates.get(CityDestination)[0]+" "+cityCoordinates.get(CityDestination)[1] + " " + CityDestination);

        //reset indexes to new list
        if (first_loc) {
            currentPolygonIndex = 0;
            currentWaypointIndex = 0;

            lastRecordedSpot = startCoordinates;

            //get polygons we will encounter in queue
            double destination_lat = cityCoordinates.get(CityDestination)[0];
            double destination_lon = cityCoordinates.get(CityDestination)[1];

            double determined_heading = calculateHeading(destination_lat, destination_lon, startCoordinates[0], startCoordinates[1]);

            // Test initialization of polygon queue and bearing ray cast
            Node current_location = plotNodesAlongPath(startCoordinates[0], startCoordinates[1], destination_lat, destination_lon, determined_heading);

            insidePolygon insidePolygon = new insidePolygon();
            polygon_queue = insidePolygon.passPolygonToQueue(current_location, polygonArray, polygon_queue);

            // Implementation of the shortest path algorithm
            pathGenerator pathGenerator = new pathGenerator();
            waypoints = pathGenerator.generateNewPath(startCoordinates, cityCoordinates.get(CityDestination), polygon_queue);
            first_loc = false;
        }
    }

////    determine if starting point or destination point is closer to tell SOAR where to land
//    public Node findDestination(Node curr_position, double[] start_position, String dest) {
//        double[] destinationCoordinates = cityCoordinates.get(dest);
//        double distanceToStart = getDistanceBetweenTwoPoints(curr_position., curr_position.lon, destinationCoordinates[0], destinationCoordinates[1]);// needs to have starting location as a waypoint object
//        double distanceToTarget= getDistanceBetweenTwoPoints(curr_position.lat, curr_position.lon, destinationCoordinates[0], destinationCoordinates[1]);
//
//        // if distance back to origin is less; turn back to the origin
//        if (distanceToStart < distanceToTarget){
//            Waypoint destination = new Waypoint(cityCoordinates.get(dest)[0], cityCoordinates.get(dest)[1], true);
//            return destination;
//        }
//        // else destination is closer so complete trip.
//        Waypoint destination = new Waypoint(start_position[0], start_position[1], true);
//        return destination;
//    }


    public static double calculateHeading(double dest_lat, double dest_lon, double lat, double lon){

        double lat2 = dest_lat;
        double lon2 = dest_lon;
        lat = Math.toRadians(lat);
        lat2 = Math.toRadians(lat2);
        lon = Math.toRadians(lon);
        lon2 = Math.toRadians(lon2);

        double Y = Math.cos(lat2) * Math.sin(lon2-lon);
        double X = (Math.cos(lat) * Math.sin(lat2)) - (Math.sin(lat) * Math.cos(lat2) *Math.cos(lon2-lon));
        double heading_angle = Math.atan2(Y,X);
        heading_angle = Math.toDegrees(heading_angle);
        //System.out.println("Determined Heading/bearing: "+ heading_angle);

        return heading_angle;
    }

    public double getDistanceToWaypoint(double lat, double lon){

        double lat2 = waypoints.get(currentPolygonIndex).get(currentWaypointIndex).getCoordinates()[0];
        double lon2 = waypoints.get(currentPolygonIndex).get(currentWaypointIndex).getCoordinates()[1];

        lat = Math.toRadians(lat);
        lat2 = Math.toRadians(lat2);
        lon = Math.toRadians(lon);
        lon2 = Math.toRadians(lon2);

        /* Haversine formula to calculate great-circle distance*/
        double distance;
        distance = Math.pow(Math.sin((lon-lon2)/2),2);
        distance *= Math.cos(lat)*Math.cos(lat2);
        distance += Math.pow(Math.sin((lat-lat2)/2),2);
        distance = Math.sqrt(distance);
        distance = Math.asin(distance);
        distance *= 2 * EARTH_RADIUS;

        return distance;
    }
    /**
     Using Hiemlers formula, taking in to sets of positional coordinates determine distance of both positions from each
     other with respect to the circular surface of the earth.
     @param lat; takes input as a double of the starting position latitude
     @param lon; takes input as a double of the starting position longitude
     @param lat2; takes input as a double of the follow-on position latitude
     @param lon2; takes input as a double of the follow-on position longitude
     @return distance, as a double representing kilometers of the distance between two positions.
     */
    public static double getDistanceBetweenTwoPoints(double lat, double lon, double lat2, double lon2){
        lat = Math.toRadians(lat);
        lat2 = Math.toRadians(lat2);
        lon = Math.toRadians(lon);
        lon2 = Math.toRadians(lon2);

        /* Haversine formula to calculate great-circle distance*/
        double distance;
        distance = Math.pow(Math.sin((lon-lon2)/2),2);
        distance *= Math.cos(lat)*Math.cos(lat2);
        distance += Math.pow(Math.sin((lat-lat2)/2),2);
        distance = Math.sqrt(distance);
        distance = Math.asin(distance);
        distance *= 2 * EARTH_RADIUS;

        return distance;
    }
    public static Node plotNodesAlongPath(double currentLat, double currentLon, double destinationLat, double destinationLon, double bearing){
        Node temp_node;
        Node start_node = new Node(new double[]{currentLat, currentLon});
        double[] forwardPosition = new double []{currentLat, currentLon};   //first of the set of forward positions is where we are currently

        // establish distance to target destination
        double node_spacing = getDistanceBetweenTwoPoints(currentLat, currentLon, destinationLat, destinationLon)/HEADING_NODE_COUNT;  //distance between each heading node in km
        double counter = HEADING_NODE_COUNT;
        temp_node = start_node;

        while(counter!=0){
            forwardPosition = calculateForwardDistance(forwardPosition, bearing, node_spacing);
            Node forward_node = new Node(new double[]{forwardPosition[0], forwardPosition[1]});
            temp_node.addEdge(new Edge(forward_node, node_spacing));
            temp_node = forward_node;
            counter--;
        }
        return start_node;
    }

    /**
     Using current position propagate the next position of a coordinate position of earth, given distance and direction.
     @param currentPosition; takes input as a double array of the starting position latitude
     @param heading; takes input as a double of heading/ direction you wish to extend
     @param distance; takes input as a double of the total distance outward you wish to propagate
     @return forwardPosition as a double array of size two denoting new propagated position.
     */
    public static double[] calculateForwardDistance(double[] currentPosition, double heading, double distance){
        double lat1 = Math.toRadians(currentPosition[0]);
        double lon1 = Math.toRadians(currentPosition[1]);
        double bearing = Math.toRadians(heading);
        double dist = distance/EARTH_RADIUS;

        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(dist) + Math.cos(lat1) * Math.sin(dist) * Math.cos(bearing));
        double lon2 = lon1 + Math.atan2(Math.sin(bearing) * Math.sin(dist) * Math.cos(lat1), Math.cos(dist) - Math.sin(lat1) * Math.sin(lat2));

        lat2 = Math.toDegrees(lat2);
        lon2 = Math.toDegrees(lon2);

        return new double[] { lat2, lon2 };
    }

    public ArrayList<Node> getPolygonsFromTxtFile() throws IOException {
        final File folder = new File("C:\\Users\\assist-lab\\Documents\\adnav-pop-weather\\java\\inputCoordinates");
        File[] files = listFilesForFolder(folder);
        ArrayList<Node> polygon_array = new ArrayList<>();

        for (File file : files) {
            Node temp = (TextParser.txtFileToLinkedList(file));
            polygon_array.add(temp);
        }

        return polygon_array;
    }

    public File[] listFilesForFolder(final File folder) {
        return folder.listFiles();
    }
    
}
