package graphapi;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A very simple in-memory graph database.
 */
public class GraphDatabase<VT, ET> {

    private final Graph<VT, ET> graph;

    private Map<VT, Set<Node<VT>>> labelNodesMap;

    private Map<ET, Set<Edge<VT, ET>>> labelEdgesMap;

    private Map<Relation<VT, ET>, Set<Edge<VT, ET>>> relationEdgesMap;

    private Map<Relation<VT, ET>, Map<Node<VT>, Set<Node<VT>>>> relationSrcMultiDstNodesMap;

    private Map<Relation<VT, ET>, Map<Node<VT>, Set<Node<VT>>>> relationDstMultiSrcNodesMap;

    private Map<VT, Set<Relation<VT, ET>>> srcLabelRelationMap;

    private Map<VT, Set<Relation<VT, ET>>> dstLabelRelationMap;

    private GraphDatabase(Graph<VT, ET> graph) {
        this.graph = graph;
        this.labelNodesMap = null;
        this.labelEdgesMap = null;
        this.relationEdgesMap = null;
        this.relationSrcMultiDstNodesMap = null;
        this.relationDstMultiSrcNodesMap = null;
        this.srcLabelRelationMap = null;
        this.dstLabelRelationMap = null;
    }

    public static <VT, ET> GraphDatabase<VT, ET> buildFromGraph(Graph<VT, ET> graph) {
        return new GraphDatabase<>(graph).buildAllIndices();
    }

    private GraphDatabase<VT, ET> buildLabelNodesMap() {
        labelNodesMap = new HashMap<>();
        for (Node<VT> v : graph.nodeIter()) {
            labelNodesMap.putIfAbsent(v.getLabel(), new HashSet<>());
            labelNodesMap.get(v.getLabel()).add(v);
        }
        return this;
    }

    private GraphDatabase<VT, ET> buildLabelEdgesMap() {
        labelEdgesMap = new HashMap<>();
        for (Edge<VT, ET> e : graph.edgeIter()) {
            labelEdgesMap.putIfAbsent(e.getLabel(), new HashSet<>());
            labelEdgesMap.get(e.getLabel()).add(e);
        }
        return this;
    }

    private GraphDatabase<VT, ET> buildRelationEdgesMap() {
        relationEdgesMap = new HashMap<>();
        for (Edge<VT, ET> e : graph.edgeIter()) {
            Relation<VT, ET> r = Relation.fromEdge(e);
            relationEdgesMap.putIfAbsent(r, new HashSet<>());
            relationEdgesMap.get(r).add(e);
        }
        return this;
    }

    private GraphDatabase<VT, ET> buildRelationNodesMap() {
        if (relationEdgesMap == null) {
            buildRelationEdgesMap();
        }

        relationSrcMultiDstNodesMap = new HashMap<>();
        relationDstMultiSrcNodesMap = new HashMap<>();
        for (Relation<VT, ET> r : relationEdgesMap.keySet()) {
            relationSrcMultiDstNodesMap.putIfAbsent(r, new HashMap<>());
            relationDstMultiSrcNodesMap.putIfAbsent(r, new HashMap<>());
            for (Edge<VT, ET> e : relationEdgesMap.get(r)) {
                relationSrcMultiDstNodesMap.get(r).putIfAbsent(e.getSrcNode(), new HashSet<>());
                relationDstMultiSrcNodesMap.get(r).putIfAbsent(e.getDstNode(), new HashSet<>());
                relationSrcMultiDstNodesMap.get(r).get(e.getSrcNode()).add(e.getDstNode());
                relationDstMultiSrcNodesMap.get(r).get(e.getDstNode()).add(e.getSrcNode());
            }
        }

        return this;
    }

    private GraphDatabase<VT, ET> buildSrcLabelRelationsMap() {
        if (relationEdgesMap == null) {
            buildRelationEdgesMap();
        }

        srcLabelRelationMap = new HashMap<>();
        for (Relation<VT, ET> r : relationEdgesMap.keySet()) {
            srcLabelRelationMap.putIfAbsent(r.getSrcLabel(), new HashSet<>());
            srcLabelRelationMap.putIfAbsent(r.getDstLabel(), new HashSet<>());
            srcLabelRelationMap.get(r.getSrcLabel()).add(r);
        }
        return this;
    }

    private GraphDatabase<VT, ET> buildDstLabelRelationsMap() {
        if (relationEdgesMap == null) {
            buildRelationEdgesMap();
        }

        dstLabelRelationMap = new HashMap<>();
        for (Relation<VT, ET> r : relationEdgesMap.keySet()) {
            dstLabelRelationMap.putIfAbsent(r.getSrcLabel(), new HashSet<>());
            dstLabelRelationMap.putIfAbsent(r.getDstLabel(), new HashSet<>());
            dstLabelRelationMap.get(r.getDstLabel()).add(r);
        }
        return this;
    }

    private GraphDatabase<VT, ET> buildAllIndices() {
        return this.buildLabelNodesMap()
                .buildLabelEdgesMap()
                .buildRelationEdgesMap()
                .buildRelationNodesMap()
                .buildSrcLabelRelationsMap()
                .buildDstLabelRelationsMap();
    }

    public Graph<VT, ET> getGraph() {
        return graph;
    }

    public Set<VT> getNodeLabelSet() {
        return labelNodesMap.keySet();
    }

    public Set<Node<VT>> getNodesOfLabel(VT label) {
        if (!labelNodesMap.containsKey(label)) {
            return new HashSet<>();
        } else {
            return labelNodesMap.get(label);
        }
    }

    public Set<ET> getEdgeLabelSet() {
        return labelEdgesMap.keySet();
    }

    public Set<Relation<VT, ET>> getRelationSet() {
        return relationEdgesMap.keySet();
    }

    public Set<Edge<VT, ET>> getEdgesOfLabel(ET edgeLabel) {
        if (!labelEdgesMap.containsKey(edgeLabel)) {
            return new HashSet<>();
        } else {
            return labelEdgesMap.get(edgeLabel);
        }
    }

    public Set<Edge<VT, ET>> getEdgesOfRelation(Relation<VT, ET> r) {
        if (!relationEdgesMap.containsKey(r)) {
            return new HashSet<>();
        } else {
            return relationEdgesMap.get(r);
        }
    }

    public Set<Node<VT>> getSrcNodesOfRelation(Relation<VT, ET> r) {
        if (!relationSrcMultiDstNodesMap.containsKey(r)) {
            return new HashSet<>();
        } else {
            return relationSrcMultiDstNodesMap.get(r).keySet();
        }
    }

    public Set<Node<VT>> getDstNodesOfRelation(Relation<VT, ET> r) {
        if (!relationDstMultiSrcNodesMap.containsKey(r)) {
            return new HashSet<>();
        } else {
            return relationDstMultiSrcNodesMap.get(r).keySet();
        }
    }

    public Set<Node<VT>> outNeighborsOfRelation(Node<VT> v, Relation<VT, ET> r) {
        if (!relationSrcMultiDstNodesMap.get(r).containsKey(v)) {
            return new HashSet<>();
        } else {
            return relationSrcMultiDstNodesMap.get(r).get(v);
        }
    }

    public Set<Node<VT>> inNeighborsOfRelation(Node<VT> v, Relation<VT, ET> r) {
        if (!relationDstMultiSrcNodesMap.get(r).containsKey(v)) {
            return new HashSet<>();
        } else {
            return relationDstMultiSrcNodesMap.get(r).get(v);
        }
    }

    public Set<Relation<VT, ET>> getRelationsOfSrcLabel(VT srcLabel) {
        if (!srcLabelRelationMap.containsKey(srcLabel)) {
            return new HashSet<>();
        } else {
            return srcLabelRelationMap.get(srcLabel);
        }
    }

    public Set<Relation<VT, ET>> getRelationsOfDstLabel(VT dstLabel) {
        if (!dstLabelRelationMap.containsKey(dstLabel)) {
            return new HashSet<>();
        } else {
            return dstLabelRelationMap.get(dstLabel);
        }
    }

}