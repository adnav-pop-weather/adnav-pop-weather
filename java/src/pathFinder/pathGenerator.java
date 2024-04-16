package pathFinder;

import graph1.Node;
import java.util.ArrayList;
import java.util.Queue;

import static polygonHelper.insidePolygon.coordinate_is_inside_polygon;
import static xplane.WaypointController.getDistanceBetweenTwoPoints;

public class pathGenerator {
    /**
     * Determines points of the polygon where it has the minimum & maximum latitude and longitude
     * @param polygonNode  The current polygon that is being traversed
     * @return A list of extreme points of the Polygon
     */
    private static ArrayList<Node> findExtremeNodes(Node polygonNode){
        ArrayList<Node> listOfExtremeNodes = new ArrayList<>();
        listOfExtremeNodes.add(polygonNode);   // Minimum Latitude
        listOfExtremeNodes.add(polygonNode);   // Maximum Latitude
        listOfExtremeNodes.add(polygonNode);   // Minimum Longitude
        listOfExtremeNodes.add(polygonNode);   // Maximum Longitude

        Node currentNode = polygonNode.edges.get(0).getDestination();
        
        // Loops through a given polygon, determining in the order of minimum and maximum latitude, and minimum and maximum longitude
        while (currentNode != polygonNode){
            if (listOfExtremeNodes.get(0).getCoordinates()[0] > currentNode.getCoordinates()[0] && !listOfExtremeNodes.contains(currentNode)){
                listOfExtremeNodes.set(0, currentNode);
            }
            if (listOfExtremeNodes.get(1).getCoordinates()[0] < currentNode.getCoordinates()[0]  && !listOfExtremeNodes.contains(currentNode)){
                listOfExtremeNodes.set(1, currentNode);
            }
            if (listOfExtremeNodes.get(2).getCoordinates()[1] > currentNode.getCoordinates()[1]  && !listOfExtremeNodes.contains(currentNode)){
                listOfExtremeNodes.set(2, currentNode);
            }
            if (listOfExtremeNodes.get(3).getCoordinates()[1] < currentNode.getCoordinates()[1]  && !listOfExtremeNodes.contains(currentNode)){
                listOfExtremeNodes.set(3, currentNode);
            }
            currentNode = currentNode.edges.get(0).getDestination();
        }
        return listOfExtremeNodes;
    }

    /**
     Determine the shortest path around polygon by comparing both paths opposite and choosing the shortest (Djikstra)
     @param listOfNodes list of extreme nodes
     @param currentPos  Contains current position of agent and destination
     @return The extreme point of where it should enter/exit
     */
    private static Node findEntryExitNode(ArrayList<Node> listOfNodes, double[] currentPos){
        double shortestDistance, nextDistance;
        
        // Set shortestDistance as the first node, assuming this is the shortest distance to current objective
        shortestDistance = getDistanceBetweenTwoPoints(listOfNodes.get(0).getCoordinates()[0] , listOfNodes.get(0).getCoordinates()[1], currentPos[0], currentPos[1]);
        Node shortestNode = listOfNodes.get(0);

        for (int i = 1; i < listOfNodes.size(); i++){
            nextDistance = getDistanceBetweenTwoPoints(listOfNodes.get(i).getCoordinates()[0] , listOfNodes.get(i).getCoordinates()[1], currentPos[0], currentPos[1]);
            if (shortestDistance > nextDistance){
                shortestNode = listOfNodes.get(i);
                shortestDistance = nextDistance;
            }
        }
        
        return shortestNode;
    }

    /**
     * In the case that the agent is initially in the polygon, iterate through the entire polygon
     @param polygon linked list of nodes that makes the shape of the polygon
     @param currentPos current position of the agent inside the polygon
     @return A node that the agent should fly to
     */
    private static Node findClosestNodeToAgent(Node polygon, double [] currentPos){
        double shortestDistance, nextDistance;

        // Set shortestDistance as the first node, assuming this is the shortest distance to current objective
        shortestDistance = getDistanceBetweenTwoPoints(polygon.getCoordinates()[0] , polygon.getCoordinates()[1], currentPos[0], currentPos[1]);
        Node shortestNode = polygon;
        Node currentNode = polygon.edges.get(0).destination;

        while(currentNode != polygon) {
            nextDistance = getDistanceBetweenTwoPoints(currentNode.getCoordinates()[0] , currentNode.getCoordinates()[1], currentPos[0], currentPos[1]);

            if (shortestDistance > nextDistance) {
                shortestNode = currentNode;
                shortestDistance = nextDistance;
            }
            currentNode = currentNode.edges.get(0).destination;
        }

        return shortestNode;
    }

    /**
     Determine the shortest path around polygon by comparing both paths opposite and choosing the shortest (Djikstra)
     @param startNode The entrance node determined to move into polygon
     @param endNode  The exit node determined to leave the polygon
     @return tempList[i] such that 0 < i < 2; determined based on the comparison of accumulated distance between both paths and returns the shortest path observed.
     */
    private static ArrayList<Node> shortestPath(Node startNode, Node endNode){
        ArrayList<Node>[] tempList = new ArrayList[2];
        tempList[0] = new ArrayList<>();
        tempList[1] = new ArrayList<>();

        tempList[0].add(startNode);
        tempList[1].add(startNode);

        Node currentNode1;
        Node currentNode2; //trailing node

        double[] distanceSumList = {0.0, 0.0};

        // iterate through list of sums and add each distance determined between waypoints, then store waypoints
        for (int i = 0; i < 2; i++){
            currentNode1 = startNode.edges.get(i).getDestination();
            currentNode2 = startNode;
            while (currentNode2 != endNode){
                tempList[i].add(currentNode1);
                distanceSumList[i] += getDistanceBetweenTwoPoints(currentNode1.getCoordinates()[0], currentNode1.getCoordinates()[1], currentNode2.getCoordinates()[0], currentNode2.getCoordinates()[1]);
                currentNode2 = currentNode1;
                currentNode1 = currentNode1.edges.get(i).getDestination();
            }
        }
        // compare each list of waypoints by distance accumulated and the shortest distance, waypoint list is returned
        if (distanceSumList[0] > distanceSumList[1]){
            return tempList[1];
        }
        else{
            return tempList[0];
        }
    }

    /**
     * Generates the flight path that the agent is able to use
     * @param currentPosition Contains the current position of the agent and its destination
     * @param polygon_queue Contains all the expected polygons that the agent will come across
     * @return A list of nodes that the agent will follow to reach its destination
     */
    public ArrayList<ArrayList<Node>> generateNewPath(double [] currentPosition, double [] destination, Queue<Node> polygon_queue) {
        // Holds the new generated path
        ArrayList<ArrayList<Node>> newWaypoints = new ArrayList<>();
        
        // Contains the extreme points of a given polygon
        ArrayList<Node> extremePoints;

        // Contains the entrance and exit node of the given polygon
        Node entranceNode;
        Node exitNode;

        Node currentNode = polygon_queue.peek();

        // Iterates through all the incoming polygons
        while(!polygon_queue.isEmpty())  {
            // Remove polygon from queue
            polygon_queue.remove();

            extremePoints = findExtremeNodes(currentNode);

            // If Agent is in polygon initially, do the inverse
            if (currentNode.isInitiallyInside) {
                entranceNode = findClosestNodeToAgent(currentNode, new double[]{currentPosition[0], currentPosition[1]});
            } else {
                entranceNode = findEntryExitNode(extremePoints, new double[]{currentPosition[0], currentPosition[1]});
            }
            exitNode = findEntryExitNode(extremePoints, new double[]{destination[0], destination[1]});

            // When que size is less than one find the shortest path to destination in node if destination is inside polygon
            if (polygon_queue.size() < 1){
                if (coordinate_is_inside_polygon(destination[0], destination[1], currentNode)){
                    exitNode = findClosestNodeToAgent(currentNode, new double[]{destination[0], destination[1]});
                }
            }
            newWaypoints.add(shortestPath(entranceNode, exitNode));
            currentNode = polygon_queue.peek();
        }

        //Add destination waypoint to end of path
        Node temp1 = new Node(destination);
        ArrayList<Node> tempList = new ArrayList<>();
        tempList.add(temp1);
        newWaypoints.add(tempList);
        
        return newWaypoints;
    }
}
