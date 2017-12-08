package graphapi;

import org.jetbrains.annotations.NotNull;

/**
 * A descriptor for graph nodes.
 */
public class Node<VT> {

    private final Object id;

    private VT label;

    private Node(Object id, VT label) {
        this.id = id;
        this.label = label;
    }

    @NotNull
    public static <VT> Node<VT> createLabeled(Object id, VT label) {
        return new Node<>(id, label);
    }

    @NotNull
    public static <VT> Node<VT> createUnlabeled(Object id) {
        return new Node<>(id, null);
    }

    public Object getId() {
        return id;
    }

    public VT getLabel() {
        return label;
    }

    public void setLabel(VT label) {
        this.label = label;
    }

    public boolean equalsLabel(Node<VT> v) {
        return this.label.equals(v.label);
    }

    @Override
    public String toString() {
        return id + "\t" + label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node<?> node = (Node<?>) o;

        if (id != null ? !id.equals(node.id) : node.id != null) return false;
        return label != null ? label.equals(node.label) : node.label == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
