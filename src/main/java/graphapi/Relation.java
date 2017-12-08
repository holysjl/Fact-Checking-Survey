package graphapi;

import org.jetbrains.annotations.NotNull;

/**
 * Relation: a relation in a graph is a triple of labels: (srcLabel, dstLabel, edgeLabel)
 */
public class Relation<VT, ET> {

    private final VT srcLabel;

    private final VT dstLabel;

    private final ET edgeLabel;

    private Relation(VT srcLabel, VT dstLabel, ET edgeLabel) {
        this.srcLabel = srcLabel;
        this.dstLabel = dstLabel;
        this.edgeLabel = edgeLabel;
    }

    @NotNull
    public static <VT, ET> Relation<VT, ET> create(VT srcLabel, VT dstLabel, ET edgeLabel) {
        return new Relation<>(srcLabel, dstLabel, edgeLabel);
    }

    @NotNull
    public static <VT, ET> Relation<VT, ET> fromEdge(Edge<VT, ET> e) {
        return new Relation<>(e.getSrcLabel(), e.getDstLabel(), e.getLabel());
    }

    public VT getSrcLabel() {
        return srcLabel;
    }

    public VT getDstLabel() {
        return dstLabel;
    }

    public ET getEdgeLabel() {
        return edgeLabel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Relation<?, ?> r = (Relation<?, ?>) o;

        return (srcLabel != null ? srcLabel.equals(r.srcLabel) : r.srcLabel == null)
                && (dstLabel != null ? dstLabel.equals(r.dstLabel) : r.dstLabel == null)
                && (edgeLabel != null ? edgeLabel.equals(r.edgeLabel) : r.edgeLabel == null);
    }

    @Override
    public int hashCode() {
        int result = srcLabel != null ? srcLabel.hashCode() : 0;
        result = 31 * result + (dstLabel != null ? dstLabel.hashCode() : 0);
        result = 31 * result + (edgeLabel != null ? edgeLabel.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return srcLabel + "\t" + dstLabel + "\t" + edgeLabel;
    }
}
