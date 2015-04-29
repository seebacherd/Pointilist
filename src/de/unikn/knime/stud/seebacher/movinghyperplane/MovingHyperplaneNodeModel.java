package de.unikn.knime.stud.seebacher.movinghyperplane;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.datageneration.SettingsModelDisactableLong;

/**
 * This is the model implementation of MovingHyperplane.
 * 
 * 
 * @author Daniel Seebacher
 */
public class MovingHyperplaneNodeModel extends NodeModel {

    /**
     * Key for the number of examples which will be created.
     */
    static final String CFGKEY_COUNT = "Count";

    /**
     * Key for the dimensionality of the data.
     */
    static final String CFGKEY_DIMENSIONALITY = "Dimensionality: ";

    /**
     * Key for the noise percentage of the data.
     */
    static final String CFGKEY_NOISE = "Noise Percentage: ";

    /**
     * Key for the number of dimensions which will be updated each round.
     */
    static final String CFGKEY_WEIGHTUPDATECOUNT = "Weight Update Count: ";

    /**
     * Number of the magnitude of the change each round.
     */
    static final String CFGKEY_MAGNITUDE = "Magnitude: ";

    /**
     * Seed Number to create replicable results.
     */
    static final String CFGKEY_SEED = "Seed: ";

    /**
     * Default number of examples.
     */
    static final int DEFAULT_COUNT = 5000;

    /**
     * Default number of dimensions.
     */
    static final int DEFAULT_DIMENSIONALITY = 5;

    /**
     * Default noise percentage.
     */
    static final int DEFAULT_NOISE = 5;

    /**
     * Default weight update count.
     */
    static final int DEFAULT_WEIGHTUPDATECOUNT = 5;

    /**
     * Default magnitude.
     */
    static final double DEFAULT_MAGNITUDE = 0.01;

    /**
     * Default seed (a new seed).
     */
    static final long DEFAULT_SEED = new Random().nextLong();

    private final SettingsModelIntegerBounded m_count = new SettingsModelIntegerBounded(CFGKEY_COUNT, DEFAULT_COUNT,
            Integer.MIN_VALUE, Integer.MAX_VALUE);
    private final SettingsModelIntegerBounded m_dimensionality = new SettingsModelIntegerBounded(CFGKEY_DIMENSIONALITY,
            DEFAULT_DIMENSIONALITY, 3, Integer.MAX_VALUE);
    private final SettingsModelIntegerBounded m_noise = new SettingsModelIntegerBounded(CFGKEY_NOISE, DEFAULT_NOISE, 0,
            100);
    private final SettingsModelIntegerBounded m_weightUpdateCount = new SettingsModelIntegerBounded(
            CFGKEY_WEIGHTUPDATECOUNT, DEFAULT_WEIGHTUPDATECOUNT, 0, Integer.MAX_VALUE);
    private final SettingsModelDoubleBounded m_magnitude = new SettingsModelDoubleBounded(CFGKEY_MAGNITUDE,
            DEFAULT_MAGNITUDE, 0, Double.MAX_VALUE);
    private final SettingsModelDisactableLong m_seed = 
            new SettingsModelDisactableLong(CFGKEY_SEED, DEFAULT_SEED, false);

    /**
     * Constructor for the node model.
     */
    protected MovingHyperplaneNodeModel() {
        super(0, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
            throws Exception {

        long seed = m_seed.getSeedOrRandom(); // seed used for random generator creation
        int numberOfExamples = m_count.getIntValue(); // number of examples which shall be created
        int dimensionality = m_dimensionality.getIntValue(); // number of dimensions which shall be used for the
                                                             // hyperplane
        double noisePercentage = (m_noise.getIntValue() / 100d); // noise percentage, switches
        // the label for p examples
        int weightUpdateCount = m_weightUpdateCount.getIntValue(); // number of weights which
                                                                   // shall be updated after an
                                                                   // example was created

        double magnitudeOfChange = m_magnitude.getDoubleValue(); // magnitude of
                                                                 // change for
                                                                 // the weights

        DataTableSpec outputSpec = createOutputSpec();
        BufferedDataContainer container = exec.createDataContainer(outputSpec);

        // create moving hyperplane data generator
        MovingHyperPlane movingHyperPlane = new MovingHyperPlane(seed, numberOfExamples, dimensionality,
                noisePercentage, weightUpdateCount, magnitudeOfChange);

        // add examples to buffereddatacontainer, one example at a time
        for (int i = 0; i < numberOfExamples; i++) {
            container.addRowToTable(movingHyperPlane.nextExample(i));
        }

        // once we are done, we close the container and return its table
        container.close();
        BufferedDataTable out = container.getTable();
        return new BufferedDataTable[] {out};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // nothing to reset
    }

    private DataTableSpec createOutputSpec() {
        int dimensions = m_dimensionality.getIntValue();
        // the data table spec of the single output table,
        DataColumnSpec[] allColSpecs = new DataColumnSpec[dimensions + 1];
        for (int i = 0; i < dimensions + 1; i++) {
            if (i == 0) {
                allColSpecs[i] = new DataColumnSpecCreator("Label", StringCell.TYPE).createSpec();
            } else {
                allColSpecs[i] = new DataColumnSpecCreator("Column " + (i), DoubleCell.TYPE).createSpec();
            }
        }

        DataTableSpec outputSpec = new DataTableSpec(allColSpecs);
        return outputSpec;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        return new DataTableSpec[] {createOutputSpec()};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_count.saveSettingsTo(settings);
        m_dimensionality.saveSettingsTo(settings);
        m_noise.saveSettingsTo(settings);
        m_weightUpdateCount.saveSettingsTo(settings);
        m_magnitude.saveSettingsTo(settings);
        m_seed.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_count.loadSettingsFrom(settings);
        m_dimensionality.loadSettingsFrom(settings);
        m_noise.loadSettingsFrom(settings);
        m_weightUpdateCount.loadSettingsFrom(settings);
        m_magnitude.loadSettingsFrom(settings);
        m_seed.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_count.validateSettings(settings);
        m_dimensionality.validateSettings(settings);
        m_noise.validateSettings(settings);
        m_weightUpdateCount.validateSettings(settings);
        m_magnitude.validateSettings(settings);
        m_seed.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir, final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // nothing to load
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir, final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // nothing to save
    }

}
