package graphapi;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Graph - represent simple directed labeled graphs.
 * - Every node can have at most one self loop.
 * - Every pair of nodes (u, v) can have at most one edge from u to v, and at most one edge from v to u.
 * - Otherwise, there are no parallel edges.
 * <p>
 * Note:
 * - Node IDs should be immutable, since they are used as keys of underlying hash maps of the graph.
 */
public class Graph<VT, ET> {

    private int numberOfNodes;

    private int numberOfEdges;

    private Iterable<Node<VT>> nodeIter;

    private Iterable<Edge<VT, ET>> edgeIter;

    private Map<Object, Node<VT>> idNodeMap;

    private Map<Node<VT>, Map<Node<VT>, Edge<VT, ET>>> outAdjMap;

    private Map<Node<VT>, Map<Node<VT>, Edge<VT, ET>>> inAdjMap;

    private Graph() {
        numberOfNodes = 0;
        numberOfEdges = 0;
        idNodeMap = new HashMap<>();
        outAdjMap = new HashMap<>();
        inAdjMap = new HashMap<>();
        nodeIter = () -> idNodeMap.values().stream().iterator();
        edgeIter = () -> outAdjMap.values().stream().flatMap(adjMap -> adjMap.values().stream()).iterator();
    }

    @NotNull
    public static <VT, ET> Graph<VT, ET> createEmpty() {
        return new Graph<>();
    }

    public int numberOfNodes() {
        return numberOfNodes;
    }

    public int numberOfEdges() {
        return numberOfEdges;
    }

    public Iterable<Node<VT>> nodeIter() {
        return nodeIter;
    }

    public Iterable<Edge<VT, ET>> edgeIter() {
        return edgeIter;
    }

    public Collection<Node<VT>> nodeSet() {
        return idNodeMap.values();
    }

    public boolean hasId(Object id) {
        if (id == null) {
            throw new NullPointerException();
        } else {
            return idNodeMap.containsKey(id);
        }
    }

    public Node<VT> getNode(Object id) {
        if (id == null) {
            throw new NullPointerException();
        } else {
            return idNodeMap.get(id);
        }
    }

    public boolean hasNode(Node<VT> v) {
        if (v == null) {
            throw new NullPointerException();
        } else {
            return v == idNodeMap.get(v.getId());
        }
    }

    public int outDegreeOf(Node<VT> v) {
        if (!hasNode(v)) {
            throw new NoSuchElementException();
        } else {
            return outAdjMap.get(v).size();
        }
    }

    public int outDegreeOf(Object id) {
        if (!hasId(id)) {
            throw new NoSuchElementException();
        } else {
            return outDegreeOf(getNode(id));
        }
    }

    public int inDegreeOf(Node<VT> v) {
        if (!hasNode(v)) {
            throw new NoSuchElementException();
        } else {
            return inAdjMap.get(v).size();
        }
    }

    public int inDegreeOf(Object id) {
        if (!hasId(id)) {
            throw new NoSuchElementException();
        } else {
            return inDegreeOf(getNode(id));
        }
    }

    public int degreeOf(Node<VT> v) {
        if (!hasNode(v)) {
            throw new NoSuchElementException();
        } else {
            return inDegreeOf(v) + outDegreeOf(v);
        }
    }

    public int degreeOf(Object id) {
        if (!hasId(id)) {
            throw new NoSuchElementException();
        } else {
            return degreeOf(getNode(id));
        }
    }

    public Set<Node<VT>> outNeighbors(Node<VT> v) {
        if (!hasNode(v)) {
            throw new NoSuchElementException();
        } else {
            return outAdjMap.get(v).keySet();
        }
    }

    public Set<Node<VT>> outNeighbors(Object id) {
        if (!hasId(id)) {
            throw new NoSuchElementException();
        } else {
            return outNeighbors(getNode(id));
        }
    }

    public Set<Node<VT>> inNeighbors(Node<VT> v) {
        if (!hasNode(v)) {
            throw new NoSuchElementException();
        } else {
            return inAdjMap.get(v).keySet();
        }
    }

    public Set<Node<VT>> inNeighbors(Object id) {
        if (!hasId(id)) {
            throw new NoSuchElementException();
        } else {
            return inNeighbors(getNode(id));
        }
    }

    public Collection<Edge<VT, ET>> outEdges(Node<VT> v) {
        if (!hasNode(v)) {
            throw new NoSuchElementException();
        } else {
            return outAdjMap.get(v).values();
        }
    }

    public Collection<Edge<VT, ET>> outEdges(Object id) {
        if (!hasId(id)) {
            throw new NoSuchElementException();
        } else {
            return outEdges(getNode(id));
        }
    }

    public Collection<Edge<VT, ET>> inEdges(Node<VT> v) {
        if (!hasNode(v)) {
            throw new NoSuchElementException();
        } else {
            return inAdjMap.get(v).values();
        }
    }

    public Collection<Edge<VT, ET>> inEdges(Object id) {
        if (!hasId(id)) {
            throw new NoSuchElementException();
        } else {
            return inEdges(getNode(id));
        }
    }

    public Node<VT> addNode(Object id, VT label) {
        if (hasId(id)) {
            return null;
        } else {
            Node<VT> v = Node.createLabeled(id, label);
            idNodeMap.put(id, v);
            outAdjMap.put(v, new HashMap<>());
            inAdjMap.put(v, new HashMap<>());
            numberOfNodes++;
            return v;
        }
    }

    public boolean addNode(Node<VT> v) {
        if (hasId(v.getId())) {
            return false;
        } else {
            idNodeMap.put(v.getId(), v);
            outAdjMap.put(v, new HashMap<>());
            inAdjMap.put(v, new HashMap<>());
            numberOfNodes++;
            return true;
        }
    }

    public boolean delNode(Node<VT> v) {
        if (!hasNode(v)) {
            return false;
        } else {
            int degree = degreeOf(v);
            for (Node<VT> vout : outNeighbors(v)) {
                inAdjMap.get(vout).remove(v);
            }
            for (Node<VT> vin : inNeighbors(v)) {
                outAdjMap.get(vin).remove(v);
            }
            outAdjMap.remove(v);
            inAdjMap.remove(v);
            idNodeMap.remove(v.getId());
            numberOfNodes--;
            numberOfEdges -= degree;
            return true;
        }
    }

    public boolean delNode(Object id) {
        if (!hasId(id)) {
            return false;
        } else {
            return delNode(getNode(id));
        }
    }

    public Edge<VT, ET> getEdge(Node<VT> src, Node<VT> dst) {
        if (!hasNode(src)) {
            throw new NoSuchElementException();
        } else if (!hasNode(dst)) {
            throw new NoSuchElementException();
        } else {
            return outAdjMap.get(src).get(dst);
        }
    }

    public Edge<VT, ET> getEdge(Object srcId, Object dstId) {
        if (!hasId(srcId)) {
            throw new NoSuchElementException();
        } else if (!hasId(dstId)) {
            throw new NoSuchElementException();
        } else {
            return outAdjMap.get(getNode(srcId)).get(getNode(dstId));
        }
    }

    public boolean hasEdge(Node<VT> src, Node<VT> dst) {
        if (!hasNode(src)) {
            throw new NoSuchElementException();
        } else if (!hasNode(dst)) {
            throw new NoSuchElementException();
        } else {
            return outAdjMap.get(src).containsKey(dst);
        }
    }

    public boolean hasEdge(Object srcId, Object dstId) {
        if (!hasId(srcId)) {
            throw new NoSuchElementException();
        } else if (!hasId(dstId)) {
            throw new NoSuchElementException();
        } else {
            return hasEdge(getNode(srcId), getNode(dstId));
        }
    }

    public boolean hasEdge(Edge<VT, ET> e) {
        if (e == null) {
            throw new NullPointerException();
        } else {
            return e == getEdge(e.getSrcNode(), e.getDstNode());
        }
    }

    public Edge<VT, ET> addEdge(Object srcId, Object dstId, ET label) {
        Node<VT> src = getNode(srcId);
        Node<VT> dst = getNode(dstId);
        if (hasEdge(src, dst)) {
            return null;
        } else {
            Edge<VT, ET> e = Edge.createLabeled(src, dst, label);
            outAdjMap.get(src).put(dst, e);
            inAdjMap.get(dst).put(src, e);
            numberOfEdges++;
            return e;
        }
    }

    public boolean addEdge(Edge<VT, ET> e) {
        if (e == null) {
            throw new NullPointerException();
        } else if (hasEdge(e.getSrcNode(), e.getDstNode())) {
            return false;
        } else {
            outAdjMap.get(e.getSrcNode()).put(e.getDstNode(), e);
            inAdjMap.get(e.getDstNode()).put(e.getSrcNode(), e);
            numberOfEdges++;
            return true;
        }
    }

    public Edge<VT, ET> delEdge(Node<VT> src, Node<VT> dst) {
        if (!hasEdge(src, dst)) {
            return null;
        } else {
            Edge<VT, ET> e = getEdge(src, dst);
            outAdjMap.get(src).remove(dst);
            inAdjMap.get(dst).remove(src);
            numberOfEdges--;
            return e;
        }
    }

    public Edge<VT, ET> delEdge(Object srcId, Object dstId) {
        if (!hasId(srcId)) {
            throw new NoSuchElementException();
        } else if (!hasId(dstId)) {
            throw new NoSuchElementException();
        } else {
            return delEdge(getNode(srcId), getNode(dstId));
        }
    }

    public boolean delEdge(Edge<VT, ET> e) {
        if (!hasEdge(e)) {
            return false;
        } else {
            outAdjMap.get(e.getSrcNode()).remove(e.getDstNode());
            inAdjMap.get(e.getDstNode()).remove(e.getSrcNode());
            numberOfEdges--;
            return true;
        }
    }

    public Graph<VT, ET> shallowCopy() {
        Graph<VT, ET> g = Graph.createEmpty();
        for (Node<VT> v : nodeIter) {
            g.addNode(v);
        }
        for (Edge<VT, ET> e : edgeIter) {
            g.addEdge(e);
        }
        return g;
    }

    public void clear() {
        this.numberOfNodes = 0;
        this.numberOfEdges = 0;
        this.idNodeMap.clear();
        this.outAdjMap.clear();
        this.inAdjMap.clear();
    }

    public boolean isEmpty() {
        return numberOfNodes == 0;
    }

    public String toGraphString() {
        StringBuilder sb = new StringBuilder();
        sb.append("# |V| = ").append(numberOfNodes).append(" |E| = ").append(numberOfEdges).append("\n");
        sb.append("# Nodes:\n");
        for (Node<VT> node : nodeIter) {
            sb.append(node).append("\n");
        }
        sb.append("# Edges:\n");
        for (Edge<VT, ET> e : edgeIter) {
            sb.append(e.getSrcId()).append("\t").append(e.getDstId()).append("\t").append(e.getLabel()).append("\n");
        }
        return sb.toString();
    }

    public String toSizeString() {
        return "# |V| = " + numberOfNodes + " |E| = " + numberOfEdges;
    }
}
