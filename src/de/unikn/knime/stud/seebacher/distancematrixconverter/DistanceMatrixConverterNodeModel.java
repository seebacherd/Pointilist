package de.unikn.knime.stud.seebacher.distancematrixconverter;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.IntValue;
import org.knime.core.data.RowIterator;
import org.knime.core.data.RowKey;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.distmatrix.type.DistanceVectorDataCell;
import org.knime.distmatrix.type.DistanceVectorDataCellFactory;


/**
 * This is the model implementation of DistanceMatrixConverter. Creates a
 * distance matrix from an input data table.
 * 
 * @author Daniel Seebacher
 */
public class DistanceMatrixConverterNodeModel extends NodeModel {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(DistanceMatrixConverterNodeModel.class);
    
    /**
     * Default Value for the X Column.
     */
    public static final String CFG_X_COLUMN = "X Column";
    
    /**
     * Default Value for the Y Column.
     */
    public static final String CFG_Y_COLUMN = "Y Column";
    
    /**
     * Default Value for the Distance Column.
     */
    public static final String CFG_DISTANCE_COLUMN = "Distance Column";

    private SettingsModelString m_xColumn = new SettingsModelString(CFG_X_COLUMN, null);
    private SettingsModelString m_yColumn = new SettingsModelString(CFG_Y_COLUMN, null);
    private SettingsModelString m_distColumn = new SettingsModelString(CFG_DISTANCE_COLUMN, null);

    /**
     * Constructor for the node model.
     */
    protected DistanceMatrixConverterNodeModel() {
        super(1, 1);
    }

    /**
     * Validates if the given input DataTableSpec is equal to [Integer, Integer,
     * Double].
     * 
     * @param inSpecs
     *            the input DataTableSpec
     * @throws InvalidSettingsException
     *             Gets thrown if the input DataTableSpec isn't equal to
     *             [Integer, Integer, Double]
     * @return The output DataTableSpec
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {

        //check if there are exactly 3 input columns
        if (inSpecs[0].getNumColumns() < 3) {
            throw new InvalidSettingsException("Input data table must have three columns!");
        }
        
        DataColumnSpec spec0 = inSpecs[0].getColumnSpec(0);
        DataColumnSpec spec1 = inSpecs[0].getColumnSpec(1);
        DataColumnSpec spec2 = inSpecs[0].getColumnSpec(2);

        // guess the layout, the integer types should be x and y, the double type should be the distance
        if (spec0.getType().isCompatible(IntValue.class)
                && spec1.getType().isCompatible(IntValue.class) 
                && spec2.getType().isCompatible(DoubleValue.class)) { // case 1 [Int, Int, Double]
            
            m_xColumn.setStringValue(spec0.getName());
            m_yColumn.setStringValue(spec1.getName());
            m_distColumn.setStringValue(spec2.getName());
            
            LOGGER.info(String.format("Assuming Layout [X=%s, Y=%s, Distance=%s]", spec0.getType().toString(), 
                    spec1.getType().toString(), spec2.getType().toString()));
            
        } else if (spec0.getType().isCompatible(IntValue.class) 
                && spec1.getType().isCompatible(DoubleValue.class) 
                && spec2.getType().isCompatible(IntValue.class)) { // case 2 [Int, Double, Int]
            
            m_xColumn.setStringValue(spec0.getName());
            m_yColumn.setStringValue(spec2.getName());
            m_distColumn.setStringValue(spec1.getName());
            
            LOGGER.info(String.format("Assuming Layout [X=%s, Y=%s, Distance=%s]", spec0.getType().toString(), 
                    spec2.getType().toString(), spec1.getType().toString()));
            
        } else if (spec0.getType().isCompatible(DoubleValue.class) 
                && spec1.getType().isCompatible(IntValue.class) 
                && spec2.getType().isCompatible(IntValue.class)) { // case 3 [Double, Int, Int]
            
            m_xColumn.setStringValue(spec1.getName());
            m_yColumn.setStringValue(spec2.getName());
            m_distColumn.setStringValue(spec0.getName());
            
            LOGGER.info(String.format("Assuming Layout [X=%s, Y=%s, Distance=%s]", spec1.getType().toString(), 
                    spec2.getType().toString(), spec0.getType().toString()));
            
        } else {
//            throw new InvalidSettingsException(
//                    String.format("No valid input data table spec [%s, %s, %s]", spec0.getType().toString(), 
//                            spec1.getType().toString(), spec2.getType().toString()));
        }
        
        return new DataTableSpec[] {getDTS()};
    }

    /**
     * Method taken from Iris Adae's TableToMatrix Node.
     */
    private DataTableSpec getDTS() {
        DataColumnSpec distance = (new DataColumnSpecCreator("distance", DistanceVectorDataCell.TYPE).createSpec());
        return new DataTableSpec(distance);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        
        if (inData[0].getRowCount() == 0) {
            BufferedDataContainer out = exec.createDataContainer(getDTS());
            return new BufferedDataTable[] {out.getTable()};
        }
        
        //Rearrange input data
        DataTableSpec inSpec = inData[0].getDataTableSpec();
        ColumnRearranger c = createColumnRearranger(inSpec);
        
        BufferedDataTable workingTable = exec.createColumnRearrangeTable(inData[0], c, exec);

        final Map<Integer, double[]> distanceMatrix = createDistanceMatrix(workingTable);
        final int signature = System.identityHashCode(inData[0]) ^ DistanceVectorDataCellFactory.RANDOM.nextInt();
        
        BufferedDataContainer out = exec.createDataContainer(getDTS());

        for (int i = 0; i < distanceMatrix.size(); i++) {
            exec.checkCanceled();
            exec.setProgress((i * 1.0) / distanceMatrix.size());

            out.addRowToTable(new DefaultRow(new RowKey(String.valueOf(i)),
                    DistanceVectorDataCellFactory.createCell(distanceMatrix.get(i), signature)));
        }


        out.close();

        return new BufferedDataTable[] {out.getTable()};

    }

    
    private ColumnRearranger createColumnRearranger(final DataTableSpec inSpec) {
        ColumnRearranger c = new ColumnRearranger(inSpec);
        c.move(m_xColumn.getStringValue(), 0);
        c.move(m_yColumn.getStringValue(), 1);
        c.move(m_distColumn.getStringValue(), 2);
        return c;
    }
    
    /**
     * Creates a triangular distance matrix from the given input data.
     * 
     * @param inData the input data table
     * @return A triangular distance matrix
     */
    private Map<Integer, double[]> createDistanceMatrix(final BufferedDataTable inData) {
      
        // retrieve the highest value of the x and y column
        final int max = Math.max(
                ((IntCell) inData.getDataTableSpec().getColumnSpec(0).getDomain().getUpperBound()).getIntValue(), 
                ((IntCell) inData.getDataTableSpec().getColumnSpec(1).getDomain().getUpperBound()).getIntValue()
                );
        
        //create triangular distance matrix, row is the key
        Map<Integer, double[]> distanceMatrix = new HashMap<Integer, double[]>();
        for (int i = 0; i <= max; i++) {
            distanceMatrix.put(i, new double[i]);
        }
       
        //read in data
        RowIterator rowIt = inData.iterator();
        while (rowIt.hasNext()) {
            DataRow currentRow = rowIt.next();
            
            //get x,y and the distance between them
            final int x = ((IntCell) currentRow.getCell(0)).getIntValue();
            final int y = ((IntCell) currentRow.getCell(1)).getIntValue();
            
            double distance;
            if (currentRow.getCell(2).getType().isCompatible(IntValue.class)) {
               distance = ((IntCell) currentRow.getCell(2)).getIntValue();
            } else {
                distance = ((DoubleCell) currentRow.getCell(2)).getDoubleValue();
            }


            //if x == y we continue because the distance from x to x should be 0
            if (x == y) {
                continue;
            }
            
            // we assume the input data is symetrical
            // if we get (2,0) with distance 1 it is the same as (0,2) with distance 1
            int row = Math.max(x, y);
            int col = Math.min(x, y);
           
            distanceMatrix.get(row)[col] = distance;
        }

        return distanceMatrix;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // nothing to reset
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_xColumn.saveSettingsTo(settings);
        m_yColumn.saveSettingsTo(settings);
        m_distColumn.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_xColumn.loadSettingsFrom(settings);
        m_yColumn.loadSettingsFrom(settings);
        m_distColumn.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_xColumn.validateSettings(settings);
        m_yColumn.validateSettings(settings);
        m_distColumn.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // nothing to load
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // nothing to save
    }

}
