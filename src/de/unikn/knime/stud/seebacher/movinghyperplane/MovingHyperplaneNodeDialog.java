package de.unikn.knime.stud.seebacher.movinghyperplane;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.datageneration.DialogComponentDisactableLong;
import org.knime.datageneration.SettingsModelDisactableLong;

/**
 * <code>NodeDialog</code> for the "MovingHyperplane" Node.
 * 
 * 
 * @author Daniel Seebacher
 */
public class MovingHyperplaneNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring MovingHyperplane node dialog.
     */
    protected MovingHyperplaneNodeDialog() {
        super();

        addDialogComponent(new DialogComponentNumber(new SettingsModelIntegerBounded(
                MovingHyperplaneNodeModel.CFGKEY_COUNT, MovingHyperplaneNodeModel.DEFAULT_COUNT, 0, Integer.MAX_VALUE),
                "Counter:", /* step */1, /* componentwidth */10));

        addDialogComponent(new DialogComponentNumber(new SettingsModelIntegerBounded(
                MovingHyperplaneNodeModel.CFGKEY_DIMENSIONALITY, MovingHyperplaneNodeModel.DEFAULT_DIMENSIONALITY, 2,
                Integer.MAX_VALUE), "Dimensionality:", /* step */1, /* componentwidth */
        10));

        addDialogComponent(new DialogComponentNumber(new SettingsModelIntegerBounded(
                MovingHyperplaneNodeModel.CFGKEY_NOISE, MovingHyperplaneNodeModel.DEFAULT_NOISE, 0, Integer.MAX_VALUE),
                "Noise:", /* step */1, /* componentwidth */
                10));

        addDialogComponent(new DialogComponentNumber(new SettingsModelIntegerBounded(
                MovingHyperplaneNodeModel.CFGKEY_WEIGHTUPDATECOUNT,
                MovingHyperplaneNodeModel.DEFAULT_WEIGHTUPDATECOUNT, 0, 
                Integer.MAX_VALUE), "Weight Update Count:", /* step */ 1, /* componentwidth */10));

        addDialogComponent(new DialogComponentNumber(new SettingsModelDoubleBounded(
                MovingHyperplaneNodeModel.CFGKEY_MAGNITUDE, MovingHyperplaneNodeModel.DEFAULT_MAGNITUDE, 0,
                Double.MAX_VALUE), "Magnitude:", /* step */0.01, /* componentwidth */
        10));
        addDialogComponent(new DialogComponentDisactableLong(new SettingsModelDisactableLong(
                MovingHyperplaneNodeModel.CFGKEY_SEED, MovingHyperplaneNodeModel.DEFAULT_SEED, false), "Seed:"));
    }
}
