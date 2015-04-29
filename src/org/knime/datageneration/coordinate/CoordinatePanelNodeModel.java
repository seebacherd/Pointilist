/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by 
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME. The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ------------------------------------------------------------------------
 */
package org.knime.datageneration.coordinate;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.image.png.PNGImageCell;
import org.knime.core.data.image.png.PNGImageContent;
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
import org.knime.datageneration.coordinate.CoordinatePanelNodeDialogPane.CoordinatePanel;


/**
 * This is the model implementation of CoordinatePanel.
 * 
 * 
 * @author Daniel Seebacher
 */
public class CoordinatePanelNodeModel extends NodeModel {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(CoordinatePanelNodeModel.class);

    private CoordinatePanelNodeConfiguration m_configuration;

    /**
     * Constructor for the node model. No input, one output
     */
    protected CoordinatePanelNodeModel() {
        super(0, 2);
    }

    private DataTableSpec[] getDataTableSpec() {
        // the data table spec of the single output table,
        // the table will have three columns:
        DataColumnSpec[] dataSpec = new DataColumnSpec[3];
        dataSpec[0] = new DataColumnSpecCreator("X Coordinate", DoubleCell.TYPE).createSpec();
        dataSpec[1] = new DataColumnSpecCreator("Y Coordinate", DoubleCell.TYPE).createSpec();
        dataSpec[2] = new DataColumnSpecCreator("Class", IntCell.TYPE).createSpec();

        DataColumnSpec[] imageSpec = new DataColumnSpec[1];
        imageSpec[0] = new DataColumnSpecCreator("Image", DataType.getType(PNGImageCell.class)).createSpec();

        return new DataTableSpec[] {new DataTableSpec(dataSpec), new DataTableSpec(imageSpec) };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
            throws Exception {

        DataTableSpec dataOutputSpec = getDataTableSpec()[0];

        // the execution context will provide us with storage capacity, in this
        // case a data container to which we will add rows sequentially
        // Note, this container can also handle arbitrary big data tables, it
        // will buffer to disc if necessary.
        BufferedDataContainer dataContainer = exec.createDataContainer(dataOutputSpec);

        double[] pointsX = m_configuration.getPointsX();
        double[] pointsY = m_configuration.getPointsY();
        int[] pointsClasses = m_configuration.getPointsClasses();

        if (pointsX == null || pointsY == null || pointsClasses == null) {
            dataContainer.close();
            BufferedDataTable out = dataContainer.getTable();
            return new BufferedDataTable[] {out};
        }

        // let's add m_count rows to it
        for (int i = 0; i < pointsX.length; i++) {
            RowKey key = new RowKey("Row " + i);
            // the cells of the current row, the types of the cells must match
            // the column spec (see above)
            DataCell[] cells = new DataCell[dataOutputSpec.getNumColumns()];
            cells[0] = new DoubleCell(pointsX[i]);
            cells[1] = new DoubleCell(pointsY[i]);
            cells[2] = new IntCell(pointsClasses[i]);
            DataRow row = new DefaultRow(key, cells);
            dataContainer.addRowToTable(row);

            // check if the execution monitor was canceled
            exec.checkCanceled();
            exec.setProgress(i / (double) pointsX.length, "Adding row " + i);
        }
        // once we are done, we close the container and return its table
        dataContainer.close();
        BufferedDataTable dataOutTable = dataContainer.getTable();

        DataTableSpec imageOutputSpec = getDataTableSpec()[1];
        BufferedDataContainer imageContainer = exec.createDataContainer(imageOutputSpec);
        RowKey key = new RowKey("Image");
        byte[] imageBytes = m_configuration.getImage();
        if (imageBytes.length <= 0) {
            imageBytes = createImage();
        }
        DataRow row = new DefaultRow(key, new PNGImageContent(imageBytes).toImageCell());
        imageContainer.addRowToTable(row);
        imageContainer.close();
        BufferedDataTable imageOutTable = imageContainer.getTable();

        return new BufferedDataTable[] {dataOutTable, imageOutTable};
    }

    /**
     * This method is used if there is currently no image stored in the configuration. 
     * It creates an empty coordinate panel and creates an image of it.
     * This is necessary because otherwise the PNGImageContent would throw an exception, 
     * because it can't work with an empty byte array.
     * @return
     */
    private byte[] createImage() {
        CoordinatePanel cp = new CoordinatePanel(m_configuration.getMinY(), m_configuration.getMaxY(),
                m_configuration.getMinX(), m_configuration.getMaxX(), m_configuration.getGridY(),
                m_configuration.getGridX());
        cp.setSize(m_configuration.getImageWidth(), m_configuration.getImageHeight());
        int w = cp.getWidth();
        int h = cp.getHeight();
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();
        cp.paint(g);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(bi, "PNG", baos);
        } catch (IOException e) {
            LOGGER.error("Couldn't create an PNG Image of the Coordinate Panel", e);
        }
        return baos.toByteArray();
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
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        if (m_configuration == null) {
            m_configuration = new CoordinatePanelNodeConfiguration();
        }

        return getDataTableSpec();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        if (m_configuration != null) {
            m_configuration.saveConfiguration(settings);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        CoordinatePanelNodeConfiguration c = new CoordinatePanelNodeConfiguration();
        c.loadConfigurationInModel(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        CoordinatePanelNodeConfiguration c = new CoordinatePanelNodeConfiguration();
        c.loadConfigurationInModel(settings);
        m_configuration = c;
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
