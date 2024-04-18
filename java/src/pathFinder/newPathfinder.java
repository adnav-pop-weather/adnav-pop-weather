package org.geotools;

import org.geotools.api.data.*;
import org.geotools.api.data.DataStoreFactorySpi;
import org.geotools.api.feature.Feature;
import org.geotools.api.feature.FeatureVisitor;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.filter.Filter;
import org.geotools.api.filter.FilterFactory;
import org.geotools.api.style.*;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.feature.visitor.*;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.geopkg.GeoPkgDataStoreFactory;
import org.geotools.graph.build.basic.BasicGraphBuilder;
import org.geotools.graph.path.AStarShortestPathFinder;
import org.geotools.graph.path.Path;
import org.geotools.graph.structure.*;
import org.geotools.graph.traverse.standard.AStarIterator;
import org.locationtech.jts.geom.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.*;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.styling.SLD;
import org.geotools.swing.JMapFrame;

public class Main {
    static int exitCode;
    public static void main(String[] args) {
        // /anaconda3 and /anaconda3/Scripts must be added to User path in order to run the python code.
        String existingPath = System.getenv("PATH");
        String[] python_script_cmd = {"cmd.exe", "/C", "\"set", "PATH=" + existingPath, "&&",
                "conda", "run", "--name", "Final_Senior_Project", "python",
                "C:/Users/Swans/OneDrive/Desktop/Final_Senior_Project/java/src/pathFinder/main.py",
                "32.893895180625904", "-97.0509447245488",
                "33.86057477654421", "-98.49042166496733\""};
        // System.out.println("Executing command: " + String.join(" ", python_script_cmd));
        ProcessBuilder pB = new ProcessBuilder(python_script_cmd);
        try {
            Process process = pB.start();

            Thread riskMapThread = new Thread(() -> {
                try {
                    exitCode = process.waitFor();
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        System.err.println(line);
                    }
                    System.out.println(exitCode);
                } catch (InterruptedException | IOException e) {
                    throw new RuntimeException(e);
                }
            });

            // start the thread
            riskMapThread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Continuing with Rest of Java Program");




    }
    public static void main2(String[] args) throws Exception {
        File directory = new File("C:\\Users\\Swans\\OneDrive\\Desktop\\Florida Tech\\Senior Project\\" +
                "geo_tools_tutorial");
        for (Iterator<DataStoreFactorySpi> it = DataStoreFinder.getAvailableDataStores(); it.hasNext(); ) {
            DataStoreFactorySpi factory = it.next();
            if( factory instanceof GeoPkgDataStoreFactory){
                GeoPkgDataStoreFactory geopkgFactory = (GeoPkgDataStoreFactory) factory;
                geopkgFactory.setBaseDirectory( directory );
            }
        }
        String myDataset = "kontur_population_us_20231115.gpkg";
        DataStore store = null;
        File in_file = new File("C:\\Users\\Swans\\OneDrive\\Desktop\\Florida Tech\\Senior Project\\" +
                "geo_tools_tutorial\\" + myDataset);

        HashMap<String, Object> map = new HashMap<>();
        map.put(GeoPkgDataStoreFactory.DBTYPE.key, "geopkg");
        map.put(GeoPkgDataStoreFactory.DATABASE.key, in_file);
        map.put("read-only", true);

        try {
            store = DataStoreFinder.getDataStore(map);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // construct a filter of the bounding box of the DFW-Wichita Falls area.
        Filter smallGraphFilter = CQL.toFilter("BBOX(geom, -10933407, 3999259 , -10929611, 4005096)");
        Filter wFallsFilter = CQL.toFilter("BBOX(geom, -10974356, 3990815, -10932662, 4012752)");
        Filter dfwFilter = CQL.toFilter("BBOX(geom, -11020629.4915, 3759892.0931, -10613373.0048, 4062133.2737)");
        // gets us the only layer in the gpkg: "kontur_population_US_20220630 — population"

        assert store != null; // accept that the store isn't null so we don't get a nullptr exception ever.

        String[] typeNames = store.getTypeNames();
        String population_feature_name = typeNames[0];
        // access to the information in the database
        SimpleFeatureSource population_feature = store.getFeatureSource(population_feature_name);
        // pass the bounding box to get the Dallas Fort Worth - Wichita Falls area
        SimpleFeatureCollection result_of_query = population_feature.getFeatures(dfwFilter);

        // SimpleFeatureCollection all_hexes = population_feature.getFeatures();

        System.out.println(result_of_query.getBounds());
        // System.out.println(all_hexes.getBounds());
        System.out.println("Number of hexagons in query = " + result_of_query.size());
        // System.out.println("Number of hexagons in total dataset = " + all_hexes.size());

        StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory();
        FilterFactory ff = CommonFactoryFinder.getFilterFactory();
        GeometryFactory gf = new GeometryFactory(new PrecisionModel(), 3857);


        // subsection to get array of polygons to pass to the graph generator.
        /*
        ArrayList<Polygon> polygons = new ArrayList<>();
        // get access to features through an iterator.
        try (SimpleFeatureIterator feature_iterator = result_of_query.features()) {
            while(feature_iterator.hasNext()) {
                polygons.add((Polygon) feature_iterator.next().getDefaultGeometry()); // cast to polygon.
            }
        }
        Polygon[] hexagons = new Polygon[polygons.size()]; // set the hexagon array to same size as polygon arraylist.
        hexagons = polygons.toArray(hexagons); // convert the polygon arraylist to array
        polygons = null; // free up memory :)

         */

        // make a polygon graph and get the waypoints from start loc to finish loc, and plot them.
        PopulationGraphGenerator.PolygonRelationship relationships = new PopulationGraphGenerator.PolygonRelationship() {

            @Override
            public boolean related(Polygon polygon, Polygon polygon1) {
                return polygon.touches(polygon1);
            }

            @Override
            public boolean equal(Polygon polygon, Polygon polygon1) {
                return polygon.within(polygon1);
            }
        };
        // create the graph generator
        PopulationGraphGenerator popGraphGenerator = new PopulationGraphGenerator(new BasicGraphBuilder(), relationships);

        // begin construction immediately o7
        /* COMMENT OUT to test a different graph construction method.
        for (int i = 0; i < hexagons.length; i++) {
            graphGenerator.add(hexagons[i]);
        } // graph is generated, now we need to access it

         */
        // use a graph visitor to take features from the feature collection to add them to the graph.
        result_of_query.accepts(
                new FeatureVisitor() {
                    public void visit(Feature feature) {
                        popGraphGenerator.add(feature);
                    }
                },
                null
        );

        Graph popGraph = popGraphGenerator.getGraph(); // wallah the graph

        // TODOO: In the future use a graphVisitor to find all the nodes that overlap the polygon??
//        Polygon weatherPoly = new Polygon();
//        popGraph.getVisitedNodes(weatherPoly);

        // get the start node. x=-10932850.4615 y=3999159.4581 small example
        // start node DFW example x=-10755302.2204 y=3836676.8001
        Point startPoint = gf.createPoint(new Coordinate(-10755302.2204, 3836676.8001));

        Node startNode = (Node) popGraphGenerator.getNode(startPoint.buffer(1.00));
        //Node startNode = popGraph.queryNodes();

        // get the destination node. x=-10929428.4591 y=4005559.8699
        // destination node DFW example x=-10981263.1745 y=4024021.9065
        Point endPoint = gf.createPoint(new Coordinate(-10981263.1745, 4024021.9065));
        Node destNode = (Node) popGraphGenerator.getNode(endPoint.buffer(1.00));

        // TODOO either update astar functions with population data or add population to the graph as well.
        AStarIterator.AStarFunctions pathfindingFunctions = new AStarIterator.AStarFunctions(destNode) {
            @Override
            public double cost(AStarIterator.AStarNode aStarNode, AStarIterator.AStarNode aStarNode1) {
                // the cost from moving from one node to another.
                // current cost is the distance
                SimpleFeature aSNFeature = (SimpleFeature) aStarNode.getNode().getObject();
                SimpleFeature aSN1Feature = (SimpleFeature) aStarNode1.getNode().getObject();

                Polygon aSNPoly = ((Polygon) aSNFeature.getDefaultGeometry());
                Polygon aSNPoly1 = ((Polygon)aSN1Feature.getDefaultGeometry());
                Double distance = aSNPoly.getCentroid().distance(aSNPoly1.getCentroid());
                Double population = (Double) aSN1Feature.getAttribute("population");
                return ((0.5 * distance) + (0.5 * population));
            }

            @Override
            public double h(Node node) {
                // heuristic function to estimate the cost from one node to goal.
                // currently using distance from current node to goal node.
                return ((Polygon)((SimpleFeature) node.getObject()).getDefaultGeometry()).getCentroid().distance(
                        ((Polygon)((SimpleFeature) this.getDest().getObject()).getDefaultGeometry()).getCentroid()
                );
            }
        };
        AStarIterator aStarIterator = new AStarIterator(startNode, pathfindingFunctions);
        // make the astar pathfinder
        AStarShortestPathFinder pathfinder = new AStarShortestPathFinder(popGraph, startNode, destNode, pathfindingFunctions);
        pathfinder.calculate();
        Path dronePathNodes = pathfinder.getPath();
        dronePathNodes.reverse();

        ArrayList<Polygon> dronePathPolys = new ArrayList<>(dronePathNodes.size());

        for (Node node : dronePathNodes) {
            dronePathPolys.add(((Polygon)((SimpleFeature) node.getObject()).getDefaultGeometry()));
        }

        LineString[] dronePathWaypoints = new LineString[dronePathNodes.size() - 1];

        for (int i=0; i < dronePathWaypoints.length; i++) {
            dronePathWaypoints[i] = gf.createLineString(new Coordinate[]{
                    dronePathPolys.get(i).getCentroid().getCoordinate(),
                    dronePathPolys.get(i+1).getCentroid().getCoordinate()
            });
        }
        dronePathNodes = null; // free up memory
        dronePathPolys = null;

        // std Deviation is ~339.527. Mean is ~81.111
        // formula for placing data into classes is (mean + (stdDev * +-range))
        // example: assign the lightest color available to "population < 81.111 + (339.527 * -0.10)"

        MapContent popMap = new MapContent();
        popMap.setTitle("population per hexagon within bounding box");

        Style style = SLD.createSimpleStyle(population_feature.getSchema()); // want to create a scaled color of map.

        // color_maps we want to use to color the data. These are passes to the build_rules function.
        String[] colors_green_red = {"#A5FFC0", "#FFF8A8", "#FEEC39", "#FF9B9A", "#FF6C21", "#FF1411"};
        String[] colors_yellow_red = {"#fbfba6", "#fbdb65", "#fc9806", "#e92a00", "#d60000"};
        String[] colors_trans_red = {"#00FFFFFF", "#FFF8A8", "#FEEC39", "#FF1411", "#D60000"};
        String[] colors_trans_red_2 = {"#00FFFFFF", "#FFEBBC", "#FFAE7D", "#FB512E", "#B71000"};

        ArrayList<Rule> rules = build_rules(colors_trans_red_2, "population", result_of_query, styleFactory, ff);

        FeatureTypeStyle featureTypeStyle = styleFactory.createFeatureTypeStyle();
        featureTypeStyle.rules().addAll(rules);

        style.featureTypeStyles().add(featureTypeStyle);

        // restrict the query for display, then pass into FeatureLayer creation.
        Filter displayFilter = ff.greater(ff.property("population"), ff.literal(100));
        SimpleFeatureCollection displayFeatures = result_of_query.subCollection(displayFilter);

        FeatureLayer layer = new FeatureLayer(displayFeatures, style);

        popMap.addLayer(layer);

        // feature type builder to be later used for adding constructed features to a feature collection.
        /* comment out the node an edge builders

        SimpleFeatureTypeBuilder nodeBuilder = new SimpleFeatureTypeBuilder();
        nodeBuilder.setName("Node");
        nodeBuilder.add("location", Polygon.class); // will need to replace with an accurate model of circle
        nodeBuilder.setSRS("EPSG:3857"); // same crs as result of query.
        SimpleFeatureType nodeType = nodeBuilder.buildFeatureType();

        SimpleFeatureTypeBuilder edgeBuilder = new SimpleFeatureTypeBuilder();
        edgeBuilder.setName("Edge");
        edgeBuilder.add("geometry", LineString.class); // pray this class works
        edgeBuilder.setSRS("EPSG:3857"); // same crs as result of query.
        edgeBuilder.setDefaultGeometry("geometry");
        SimpleFeatureType edgeType = edgeBuilder.buildFeatureType();

         */

        SimpleFeatureTypeBuilder pathBuilder = new SimpleFeatureTypeBuilder();
        pathBuilder.setName("Path");
        pathBuilder.add("geometry", LineString.class); // pray this class works
        pathBuilder.setSRS("EPSG:3857"); // same crs as result of query.
        pathBuilder.setDefaultGeometry("geometry");
        SimpleFeatureType pathType = pathBuilder.buildFeatureType();


        /* comment out the node and edge related stuff
        Collection<Node> nodesCollection = popGraph.getNodes();
        Collection<Edge> edgesCollection = popGraph.getEdges();

        DefaultFeatureCollection nodesFC = new DefaultFeatureCollection();
        DefaultFeatureCollection edgesFC = new DefaultFeatureCollection();

         */
        DefaultFeatureCollection pathFC = new DefaultFeatureCollection();


        // Add each node into the feature collection. Convert the hexagons to circles with some radius,
        // Where the center coordinates and the radius are in longitude latitude.
        /*
        for(Node node: nodesCollection) {
            Polygon geometry = (Polygon) node.getObject(); // gets us the geometry of the nodes.
            nodesFC.add(SimpleFeatureBuilder.build(nodeType, new Object[]{geometry.getCentroid().buffer(100.0)}, null ));
        }
        for (Edge edge: edgesCollection) {
            Coordinate nodeACenter = ((Polygon) edge.getNodeA().getObject()).getCentroid().getCoordinate(); // get the polygon first, then centroid
            Coordinate nodeBCenter = ((Polygon) edge.getNodeB().getObject()).getCentroid().getCoordinate();
            LineString geometry = gf.createLineString(new Coordinate[]{nodeACenter, nodeBCenter});
            edgesFC.add(SimpleFeatureBuilder.build(edgeType, new Object[]{geometry}, null));
        }

         */
        for (LineString pathComponent : dronePathWaypoints) {
            pathFC.add(SimpleFeatureBuilder.build(pathType, new Object[]{pathComponent}, null));
        }

        /*
        Style nodeStyle = SLD.createSimpleStyle(nodesFC.getSchema());
        Style edgeStyle = SLD.createSimpleStyle(edgesFC.getSchema());

         */
        Style pathStyle = SLD.createSimpleStyle(pathFC.getSchema());

        /*
        PolygonSymbolizer nodeSym = styleFactory.createPolygonSymbolizer();
        nodeSym.setFill(styleFactory.createFill(ff.literal("#000000")));

        LineSymbolizer edgeSym = styleFactory.createLineSymbolizer();
        edgeSym.setStroke(styleFactory.createStroke(ff.literal("#000000"), ff.literal(2.0), ff.literal(1.0)));

         */

        LineSymbolizer pathSym = styleFactory.createLineSymbolizer();
        pathSym.setStroke(styleFactory.createStroke(ff.literal("#00FF00"), ff.literal(5.0), ff.literal(1.0)));

        /*
        Rule nodeRule = styleFactory.createRule();
        Rule edgeRule = styleFactory.createRule();

         */
        Rule pathRule = styleFactory.createRule();

        /*
        nodeRule.symbolizers().add(nodeSym);
        edgeRule.symbolizers().add(edgeSym);

         */
        pathRule.symbolizers().add(pathSym);

        /*
        FeatureTypeStyle nodeTypeStyle = styleFactory.createFeatureTypeStyle();
        nodeTypeStyle.rules().add(nodeRule);
        nodeStyle.featureTypeStyles().add(nodeTypeStyle);

        FeatureTypeStyle edgeTypeStyle = styleFactory.createFeatureTypeStyle();
        edgeTypeStyle.rules().add(edgeRule);
        edgeStyle.featureTypeStyles().add(edgeTypeStyle);

         */

        FeatureTypeStyle pathTypeStyle = styleFactory.createFeatureTypeStyle();
        pathTypeStyle.rules().add(pathRule);
        pathStyle.featureTypeStyles().add(pathTypeStyle);

        /*
        FeatureLayer nodeLayer = new FeatureLayer(nodesFC, nodeStyle);
        FeatureLayer edgeLayer = new FeatureLayer(edgesFC, edgeStyle);

         */
        FeatureLayer pathLayer = new FeatureLayer(pathFC, pathStyle);

        // popMap.addLayer(edgeLayer);
        // popMap.addLayer(nodeLayer);
        popMap.addLayer(pathLayer);

        JMapFrame.showMap(popMap);

    }

    private static Double[] geometricClassification(SimpleFeatureCollection fc,
                                                    String attribute,
                                                    int numClasses) throws IOException, CQLException {
        Double[] result = new Double[numClasses + 2]; // make an array of boundaries, where the range is
        // equal to the number of classes.
        MinVisitor min_visitor = new MinVisitor(CQL.toExpression(attribute));
        fc.accepts(min_visitor, null);
        Double min = min_visitor.getResult().toDouble(); // add one to prevent raising power of 0 to 1/numClasses.
        Double boundary_min = min + 1.00;

        MaxVisitor max_visitor = new MaxVisitor(CQL.toExpression(attribute));
        fc.accepts(max_visitor, null);
        Double max = max_visitor.getResult().toDouble() + 1.00; // this way we include the max value of the data.

        Double doubleNumClasses = (double) numClasses;
        Double X = Math.pow((max / boundary_min), (1 / doubleNumClasses)); // need power, and numClasses to float
        result[0] = min; // set the min range of the colors.
        for(int k = 1; k < result.length - 1; k++) {
            result[k] = boundary_min * Math.pow(X, k);
        }
        return result;
    }

    /**
     * Returns a list of rules that describe the ways to color the polygons using geometric intervals,
     *  and the attribute to read.
     */
    private static ArrayList<Rule> build_rules(String[] colors, String attribute, SimpleFeatureCollection fc,
    StyleFactory sf, FilterFactory ff) throws CQLException, IOException {
        ArrayList<Rule> rules = new ArrayList<>(); // empty array list to contain our rules

        Double[] classBreaks = geometricClassification(fc, attribute, colors.length); // get class breaks.

        for(int i=0; i < colors.length; i++) {
            // make a new polygon symbolizer
            PolygonSymbolizer newSymbolizer = sf.createPolygonSymbolizer();
            newSymbolizer.setName(String.format("symbolizer_%d", i));

            Fill newFill = sf.createFill(ff.literal(colors[i]));
            newFill.setOpacity(ff.literal(0.9));
            newSymbolizer.setFill(newFill);

            // borders are set to be (transparent, with no edge width, no opacity (full transparency))
            newSymbolizer.setStroke(sf.createStroke(ff.literal("#00FFFFFF"), ff.literal(0.0), ff.literal(0.0)));

            Rule newRule = sf.createRule();

            Filter lhsFilter = CQL.toFilter(String.format("population >= %f", classBreaks[i]));
            Filter rhsFilter = CQL.toFilter(String.format("population < %f", classBreaks[i+1]));
            Filter combined = ff.and(lhsFilter, rhsFilter);

            newRule.setFilter(combined);

            newRule.symbolizers().add(newSymbolizer);
            rules.add(newRule);
        }

        return rules;
    }
}