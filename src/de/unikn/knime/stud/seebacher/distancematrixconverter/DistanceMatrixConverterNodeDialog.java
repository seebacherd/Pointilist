package de.unikn.knime.stud.seebacher.distancematrixconverter;

import org.knime.core.data.IntValue;
import org.knime.core.data.DoubleValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "DistanceMatrixConverter" Node. Creates a distance matrix from an input data table.
 * 
 * @author Daniel Seebacher
 */
public class DistanceMatrixConverterNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring DistanceMatrixConverter node dialog. This is just a suggestion to demonstrate possible
     * default dialog components.
     */
    @SuppressWarnings("unchecked")
    protected DistanceMatrixConverterNodeDialog() {
        super();

        addDialogComponent(new DialogComponentColumnNameSelection(new SettingsModelString(
                DistanceMatrixConverterNodeModel.CFG_X_COLUMN, null), "X Column:", 0, IntValue.class));

        addDialogComponent(new DialogComponentColumnNameSelection(new SettingsModelString(
                DistanceMatrixConverterNodeModel.CFG_Y_COLUMN, null), "Y Column:", 0, IntValue.class));

        addDialogComponent(new DialogComponentColumnNameSelection(new SettingsModelString(
                DistanceMatrixConverterNodeModel.CFG_DISTANCE_COLUMN, null), "Distance Column:", 0, DoubleValue.class));
    }
}
