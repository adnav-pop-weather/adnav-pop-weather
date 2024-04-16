package graph1;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private double[] coordinates;
    public List<Edge> edges = new ArrayList<>();
    public boolean isLandingPoint = true;
    public boolean isInitiallyInside = false;

    public Node(double[] coordinates){
        this.coordinates = coordinates;
    }
    public void addEdge(Edge edge ){
        this.edges.add(edge);
    }
    public double[] getCoordinates(){
        return this.coordinates;
    }

    public void print(Node start_node){
        Node node = start_node;
        System.out.println("(" + node.coordinates[0] + " " + node.coordinates[1] + ")");
        node = node.edges.get(1).getDestination();

        while(node != start_node){
            System.out.println("(" + node.coordinates[0] + " " + node.coordinates[1] + ")");
            node = node.edges.get(1).getDestination();
        }
    }

}

