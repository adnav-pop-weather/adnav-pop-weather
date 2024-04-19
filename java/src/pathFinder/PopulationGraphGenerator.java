package pathFinder;
/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
// package org.geotools.graph.build.polygon;

import java.util.ArrayList;
import java.util.List;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.graph.build.GraphBuilder;
import org.geotools.graph.build.GraphGenerator;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.Graphable;
import org.geotools.graph.structure.Node;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.index.quadtree.Quadtree;

public class PopulationGraphGenerator implements GraphGenerator {

    /** Determines the relationship among two polygons. */
    public static interface PolygonRelationship {
        /** Determines if two polygons are related in any way. Rel */
        boolean related(Polygon p1, Polygon p2);

        boolean equal(Polygon p1, Polygon p2);
    }

    /** store polygon to node mapping in spatial index * */
    Quadtree index;
    /** relationship between polygons in graph * */
    PolygonRelationship rel;
    /** the node/edge builder * */
    GraphBuilder builder;

    public PopulationGraphGenerator(GraphBuilder builder, PolygonRelationship rel) {
        setGraphBuilder(builder);
        this.rel = rel;

        index = new Quadtree();
    }

    @Override
    public Graphable add(Object obj) {
        // expects a feature to be added
        SimpleFeature feature = (SimpleFeature) obj;
        Node node = (Node) get(feature);
        if (node == null) {
            node = builder.buildNode();
            builder.addNode(node);

            node.setObject(obj);
            relate(node);

            // TODO: the envelope should be buffered by some tolerance
            index.insert(((Polygon) feature.getDefaultGeometry()).getEnvelopeInternal(), node);
        }

        return node;
    }

    @Override
    public Graphable get(Object obj) {
        SimpleFeature feature = (SimpleFeature) obj;
        Polygon polygon = (Polygon) feature.getDefaultGeometry();
        return find(polygon);
    }

    public Graphable getNode(Object obj) {
        Polygon polygon = (Polygon) obj;
        return find(polygon);
    }

    @Override
    public Graphable remove(Object obj) {
        Node node = (Node) get(obj);
        if (node != null) {
            SimpleFeature feature = (SimpleFeature) node.getObject();
            Polygon polygon = (Polygon) feature.getDefaultGeometry();
            index.remove(polygon.getEnvelopeInternal(), node);

            builder.removeNode(node);
        }

        return node;
    }

    @Override
    public void setGraphBuilder(GraphBuilder builder) {
        this.builder = builder;
    }

    @Override
    public GraphBuilder getGraphBuilder() {
        return builder;
    }

    @Override
    public Graph getGraph() {
        return builder.getGraph();
    }


    // TODO: Make sure this gives us the nodes in the graph to update.
    public ArrayList<SimpleFeature> getFeatures(Polygon polygon) {
        List close = index.query(polygon.getEnvelopeInternal());
        ArrayList<SimpleFeature> overlappingFeatures = new ArrayList<>(close.size());
        for (Object o: close) {
            Node node = (Node) o;
            overlappingFeatures.add((SimpleFeature) node.getObject());
        }
        return overlappingFeatures;
    }


    protected Node find(Polygon polygon) {
        List close = index.query(polygon.getEnvelopeInternal());
        for (Object o : close) {
            Node node = (Node) o;
            SimpleFeature feature = (SimpleFeature) node.getObject();
            Polygon p = (Polygon) feature.getDefaultGeometry();

            if (rel.equal(polygon, p)) {
                return node;
            }
        }

        return null;
    }

    protected void relate(Node node) {
        SimpleFeature feature = (SimpleFeature) node.getObject();
        Polygon polygon = (Polygon) feature.getDefaultGeometry();
        List close = index.query(polygon.getEnvelopeInternal());

        for (Object o : close) {
            Node n = (Node) o;
            SimpleFeature nF = (SimpleFeature) n.getObject();
            Polygon p = (Polygon) nF.getDefaultGeometry();

            if (!rel.equal(polygon, p) && rel.related(polygon, p)) {
                Edge edge = builder.buildEdge(node, n);
                builder.addEdge(edge);
                builder.addEdge(edge);
            }
        }
    }
}