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
        //url start
        String accountStyles = "https://api.mapbox.com/styles/v1/nortghetti/";
        //style_id
        String style_id = "clv4w2kjo02fu01pkgauwaz11/";
        //static should be good
        String mapType = "static/";
        //overlay
        String planeMarker = "pin-s-airport+FFFFFF("+ currentLon + "," + currentLat + ")";

        String originalPath = "";
        String detouredPath = "";
        String cautionMarker = "";

        if (!initialWaypoints.isEmpty()) {
            originalPath = ",path-3+00FF00-1(" + (Objects.equals(defaultPathEncoded, "") ? defaultPathEncoded = polylineEncoder(initialWaypoints) : defaultPathEncoded) + ")";
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
            detouredPath = ",path-3+FFFFFF-1(" + polylineEncoder(waypointss) + ")";
        }

        String areaCenter = "-97.2679,34.045,6.40,0/";
        String imageDimension = "600x500";

        // Get API key from file MAPBOXAPI.txt
        String accessToken = "access_token=" + System.getenv("MAPBOX_TOKEN");

        //mapbox://styles/nortghetti/clv4th1u602gi01nu00gz64l3
        //https://api.mapbox.com/styles/v1/nortghetti/clv4w2kjo02fu01pkgauwaz11/wmts?access_token=pk.eyJ1Ijoibm9ydGdoZXR0aSIsImEiOiJjbHV1ZWlyZnMwOTVvMnFwZjk2c25za20yIn0.kNyP3F8mqv4EN-psDebN-Q
        endpoint = accountStyles + style_id + mapType + planeMarker + cautionMarker + originalPath + detouredPath + "/" + areaCenter +  imageDimension + "?" + accessToken;
        System.out.println(endpoint);
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
