package graphapi;

import org.jetbrains.annotations.NotNull;

/**
 * A descriptor for graph edges.
 */
public class Edge<VT, ET> {

    private final Node<VT> srcNode;

    private final Node<VT> dstNode;

    private ET label;

    private Edge(Node<VT> srcNode, Node<VT> dstNode, ET label) {
        this.srcNode = srcNode;
        this.dstNode = dstNode;
        this.label = label;
    }

    @NotNull
    public static <VT, ET> Edge<VT, ET> createLabeled(Node<VT> srcNode, Node<VT> dstNode, ET label) {
        return new Edge<>(srcNode, dstNode, label);
    }

    @NotNull
    public static <VT, ET> Edge<VT, ET> createUnlabeled(Node<VT> srcNode, Node<VT> dstNode) {
        return new Edge<>(srcNode, dstNode, null);
    }

    public Node<VT> getSrcNode() {
        return srcNode;
    }

    public Node<VT> getDstNode() {
        return dstNode;
    }

    public ET getLabel() {
        return label;
    }

    public void setLabel(ET label) {
        this.label = label;
    }

    public Object getSrcId() {
        return srcNode.getId();
    }

    public Object getDstId() {
        return dstNode.getId();
    }

    public VT getSrcLabel() {
        return srcNode.getLabel();
    }

    public VT getDstLabel() {
        return dstNode.getLabel();
    }

    public boolean equalsSrcLabel(Edge<VT, ET> e) {
        return this.srcNode.equalsLabel(e.srcNode);
    }

    public boolean equalsDstLabel(Edge<VT, ET> e) {
        return this.dstNode.equalsLabel(e.dstNode);
    }

    public boolean equalsLabel(Edge<VT, ET> e) {
        return this.label.equals(e.label);
    }

    public boolean equalsAllLabels(Edge<VT, ET> e) {
        return equalsSrcLabel(e) && equalsDstLabel(e) && equalsLabel(e);
    }

    @Override
    public String toString() {
        return srcNode + "\t" + dstNode + "\t" + label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Edge<?, ?> edge = (Edge<?, ?>) o;

        if (srcNode != null ? !srcNode.equals(edge.srcNode) : edge.srcNode != null) return false;
        if (dstNode != null ? !dstNode.equals(edge.dstNode) : edge.dstNode != null) return false;
        return label != null ? label.equals(edge.label) : edge.label == null;
    }

    @Override
    public int hashCode() {
        int result = srcNode != null ? srcNode.hashCode() : 0;
        result = 31 * result + (dstNode != null ? dstNode.hashCode() : 0);
        return result;
    }
}
