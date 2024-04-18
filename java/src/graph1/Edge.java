package graph1;

public class Edge {
    public Node destination;
    private double weight;
    public Edge(Node destination, double weight){
        this.destination = destination;
        this.weight = weight;
    }
    public void setDestination(Node coordinates){
        this.destination = coordinates;
    }
    public void setWeight(double weight){
        this.weight = weight;
    }
    public Node getDestination(){
        return this.destination;
    }
    public Double getWeight(){return this.weight;}
}
