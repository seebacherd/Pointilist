package de.unikn.knime.stud.seebacher.movinghyperplane;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "MovingHyperplane" Node.
 * 
 * 
 * @author Daniel Seebacher
 */
public class MovingHyperplaneNodeFactory extends NodeFactory<MovingHyperplaneNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public MovingHyperplaneNodeModel createNodeModel() {
        return new MovingHyperplaneNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<MovingHyperplaneNodeModel> createNodeView(final int viewIndex,
            final MovingHyperplaneNodeModel nodeModel) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDialog() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new MovingHyperplaneNodeDialog();
    }

}
