package xplane.InterfacePanel.mapBoxAPI;

import java.util.ArrayList;
import java.util.Objects;

import graph1.Node;
import static xplane.WaypointController.waypoints;
import static xplane.WaypointController.lastRecordedSpot;
import static xplane.WaypointController.initialWaypoints;

public class mapbox {
    private String defaultPathEncoded = "";
    private String LOCPathEncoded = "";
    public String createEndpoint(double currentLon, double currentLat, boolean loc){
        String endpoint;
        String accountStyles = "https://api.mapbox.com/styles/v1/adnsenior2023/";
        String mapID = "clfn20507001s01r1a5qqdc29/";
        String mapType = "static/";
        String planeMarker = "pin-s-airport+0000FF("+ currentLon + "," + currentLat + ")";

        String originalPath = "";
        String detouredPath = "";
        String cautionMarker = "";

        if (!initialWaypoints.isEmpty()) {
            originalPath = ",path-2+00FF00-0.5(" + (Objects.equals(defaultPathEncoded, "") ? defaultPathEncoded = polylineEncoder(initialWaypoints) : defaultPathEncoded) + ")";
        }

        ArrayList<ArrayList<Node>> waypointss = new ArrayList<>();

        //Add destination waypoint to end of path
        Node temp1 = new Node(lastRecordedSpot);
        ArrayList<Node> tempList = new ArrayList<>();
        tempList.add(temp1);
        waypointss.add(tempList);

        for (int i = 0; i < waypoints.size(); i++) {
            waypointss.add(waypoints.get(i));
        }

        if (loc) {
            cautionMarker = ",pin-s-caution+FF0000("+ lastRecordedSpot[1] + "," + lastRecordedSpot[0] + ")";
            detouredPath = ",path-2+FF0000-0.5(" + polylineEncoder(waypointss) + ")";
        }

        String areaCenter = "-97.2679,34.045,6.40,0/";
        String imageDimension = "1000x500";

        // Get API key from file MAPBOXAPI.txt
        String accessToken = "access_token=" + System.getenv("MAPBOX_TOKEN");

        endpoint = accountStyles + mapID + mapType + planeMarker + cautionMarker + originalPath + detouredPath + "/" + areaCenter +  imageDimension + "?" + accessToken;
        return endpoint;

    }

    private String polylineEncoder(ArrayList<ArrayList<Node>> waypoints) {
        if (waypoints.isEmpty()) {
            return "";
        }

        StringBuilder encodedPoints = new StringBuilder();
        long prevLat = 0;
        long prevLon = 0;

        for (ArrayList<Node> waypoint : waypoints) {
            for (Node node : waypoint) {
                long lat = Math.round(node.getCoordinates()[0] * 1e5);
                long lon = Math.round(node.getCoordinates()[1] * 1e5);

                long latDiff = lat - prevLat;
                long lonDiff = lon - prevLon;

                encodeValue(latDiff, encodedPoints);
                encodeValue(lonDiff, encodedPoints);

                prevLat = lat;
                prevLon = lon;
            }
        }
        return encodedPoints.toString();
    }

    private static void encodeValue(long value, StringBuilder sb) {
        value = value < 0 ? ~(value << 1) : value << 1;

        while(value >= 0x20) {
            sb.append(Character.toChars((int) ((0x20 | (value & 0x1f)) + 63)));
            value >>= 5;
        }
        sb.append(Character.toChars((int) (value + 63)));
    }
}
