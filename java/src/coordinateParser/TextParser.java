package coordinateParser;

import graph1.Edge;
import graph1.Node;
import xplane.WaypointController;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// This function takes in a .txt file and uses java's pattern and matcher class to scan each line for the <lat> and
// <lon> coordinates. The .txt file is intended to store one polygon from the FAA map that was manually plotted.
// It takes these coordinates and stores them in a linked list which represents the polygon. To use, call the
// txtFileToListofDoubleArray function with the file passed as the argument.
public class TextParser {

    public static Node txtFileToLinkedList(File file) throws IOException {
        Scanner scan = new Scanner(file);

        Pattern lat_pattern = Pattern.compile("<lat>(-?\\d*\\.?\\d*)");
        Pattern lon_pattern = Pattern.compile("<lon>(-?\\d*\\.?\\d*)");
        Matcher lat_matcher;
        Matcher lon_matcher;

        // Init temp variables
        String line;
        double lat;
        double lon;
        Node temp_node= new Node(new double[]{0, 0});
        Node start_node = new Node(new double[]{0, 0});

        int counter = 0;

        while(scan.hasNextLine()){
            line = scan.nextLine();

            if(line.equals("  </waypoint-table>")){
                break;
            }

            lat_matcher = lat_pattern.matcher(line);
            line = scan.nextLine();

            lon_matcher = lon_pattern.matcher(line);

            if (lat_matcher.find() && lon_matcher.find()){ //if a match is found for both lat and lon
                lat = Double.parseDouble(lat_matcher.group(1));
                lon = Double.parseDouble(lon_matcher.group(1));

                if (counter == 0){ //first node in linked list
                    start_node = new Node(new double[]{lat, lon});
                    start_node.addEdge(new Edge(start_node, -1));
                    temp_node = start_node;
                }
                else{ //everything in middle of linked list
                    Node curr_node = new Node(new double[]{lat, lon});
                    curr_node.addEdge(new Edge(temp_node, 0));
                    temp_node.addEdge(new Edge(curr_node, 0));
                    temp_node = curr_node;
                }
                counter++;
            }
        }
        // Links start node with last node to provide circular doubly linked list
        temp_node.addEdge(new Edge(start_node, 0));
        start_node.edges.get(0).setDestination(temp_node);

        scan.close();
        //start_node.print(start_node);
        return start_node;
    }


//    private static double findWeight(Node start, Node end){
//        // init
//        WaypointController calc = new WaypointController();
//
//        // get calculations and return value as a double in Km
//        double distance = calc.getDistanceBetweenTwoPoints(start.getCoordinates()[0], start.getCoordinates()[1], end.getCoordinates()[0], end.getCoordinates()[1]);
//        return distance;
//    }
}
