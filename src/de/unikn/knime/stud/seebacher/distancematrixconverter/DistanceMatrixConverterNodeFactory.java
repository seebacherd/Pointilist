package de.unikn.knime.stud.seebacher.distancematrixconverter;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "DistanceMatrixConverter" Node. Creates a distance matrix from an input data table.
 * 
 * @author Daniel Seebacher
 */
public class DistanceMatrixConverterNodeFactory extends NodeFactory<DistanceMatrixConverterNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public DistanceMatrixConverterNodeModel createNodeModel() {
        return new DistanceMatrixConverterNodeModel();
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
    public NodeView<DistanceMatrixConverterNodeModel> createNodeView(final int viewIndex,
            final DistanceMatrixConverterNodeModel nodeModel) {
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
        return new DistanceMatrixConverterNodeDialog();
    }

}
