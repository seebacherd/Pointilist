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

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 * Configuration object for a coordinate panel. See org.knime.base.node.meta.looper.LoopEndJoinNodeConfiguration.class
 * for reference
 *
 * @author Daniel Seebacher, University of Konstanz.
 */
public class CoordinatePanelNodeConfiguration {

    private static final int MIN_XY = -10;
    private static final int MAX_XY = 10;
    private static final int GRID_XY = 1;

    private int m_minY = MIN_XY;
    private int m_maxY = MAX_XY;
    private int m_minX = MIN_XY;
    private int m_maxX = MAX_XY;
    private int m_gridY = GRID_XY;
    private int m_gridX = GRID_XY;

    private double[] m_pointsX = new double[0];
    private double[] m_pointsY = new double[0];
    private int[] m_pointsClasses = new int[0];

    private int m_imageHeight = 800;
    private int m_imageWidth = 800;
    private byte[] m_image = new byte[0];

    /**
     * Save current config.
     *
     * @param settings
     *            To save to.
     */
    void saveConfiguration(final NodeSettingsWO settings) {
        settings.addInt("minY", m_minY);
        settings.addInt("maxY", m_maxY);
        settings.addInt("minX", m_minX);
        settings.addInt("maxX", m_maxX);
        settings.addInt("gridY", m_gridY);
        settings.addInt("gridX", m_gridX);
        settings.addDoubleArray("pointsY", m_pointsY);
        settings.addDoubleArray("pointsX", m_pointsX);
        settings.addIntArray("pointsClasses", m_pointsClasses);
        settings.addInt("imageWidth", m_imageWidth);
        settings.addInt("imageHeight", m_imageHeight);
        settings.addByteArray("image", m_image);
    }

    /**
     * Load in NodeModel.
     *
     * @param settings
     *            To load from.
     * @throws InvalidSettingsException
     *             Not actually thrown.
     */
    void loadConfigurationInModel(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_minY = settings.getInt("minY", MIN_XY);
        m_maxY = settings.getInt("maxY", MAX_XY);
        m_minX = settings.getInt("minX", MIN_XY);
        m_maxX = settings.getInt("maxX", MAX_XY);
        m_gridY = settings.getInt("gridY", GRID_XY);
        m_gridX = settings.getInt("gridX", GRID_XY);
        m_pointsY = settings.getDoubleArray("pointsY", m_pointsY);
        m_pointsX = settings.getDoubleArray("pointsX", m_pointsX);
        m_pointsClasses = settings.getIntArray("pointsClasses", m_pointsClasses);
        m_imageWidth = settings.getInt("imageWidth", m_imageWidth);
        m_imageHeight = settings.getInt("imageHeight", m_imageHeight);
        m_image = settings.getByteArray("image", m_image);
    }

    /**
     * Load in Dialog.
     *
     * @param settings
     *            To load from.
     */
    void loadConfigurationInDialog(final NodeSettingsRO settings) {
        m_minY = settings.getInt("minY", MIN_XY);
        m_maxY = settings.getInt("maxY", MAX_XY);
        m_minX = settings.getInt("minX", MIN_XY);
        m_maxX = settings.getInt("maxX", MAX_XY);
        m_gridY = settings.getInt("gridY", GRID_XY);
        m_gridX = settings.getInt("gridX", GRID_XY);
        m_pointsY = settings.getDoubleArray("pointsY", m_pointsY);
        m_pointsX = settings.getDoubleArray("pointsX", m_pointsX);
        m_pointsClasses = settings.getIntArray("pointsClasses", m_pointsClasses);
        m_imageWidth = settings.getInt("imageWidth", m_imageWidth);
        m_imageHeight = settings.getInt("imageHeight", m_imageHeight);
        m_image = settings.getByteArray("image", m_image);
    }

    /**
     * @return the m_minY
     */
    int getMinY() {
        return m_minY;
    }

    /**
     * @param newMinY
     *            the m_minY to set
     */
    void setMinY(final int newMinY) {
        this.m_minY = newMinY;
    }

    /**
     * @return the m_maxY
     */
    int getMaxY() {
        return m_maxY;
    }

    /**
     * @param newMaxY
     *            the m_maxY to set
     */
    void setMaxY(final int newMaxY) {
        this.m_maxY = newMaxY;
    }

    /**
     * @return the m_minX
     */
    int getMinX() {
        return m_minX;
    }

    /**
     * @param newMinX
     *            the m_minX to set
     */
    void setMinX(final int newMinX) {
        this.m_minX = newMinX;
    }

    /**
     * @return the m_maxX
     */
    int getMaxX() {
        return m_maxX;
    }

    /**
     * @param newMaxX
     *            the m_maxX to set
     */
    void setMaxX(final int newMaxX) {
        this.m_maxX = newMaxX;
    }

    /**
     * @return the m_gridY
     */
    int getGridY() {
        return m_gridY;
    }

    /**
     * @param newGridY
     *            the m_gridY to set
     */
    void setGridY(final int newGridY) {
        this.m_gridY = newGridY;
    }

    /**
     * @return the gridX
     */
    int getGridX() {
        return m_gridX;
    }

    /**
     * @param newGridX
     *            the m_gridX to set
     */
    void setGridX(final int newGridX) {
        this.m_gridX = newGridX;
    }

    /**
     * @return the m_pointsX
     */
    double[] getPointsX() {
        return m_pointsX;
    }

    /**
     * @param newPointsX
     *            the m_pointsX to set
     */
    void setPointsX(final double[] newPointsX) {
        this.m_pointsX = newPointsX;
    }

    /**
     * @return the m_pointsY
     */
    double[] getPointsY() {
        return m_pointsY;
    }

    /**
     * @param newPointsY
     *            the m_pointsY to set
     */
    void setPointsY(final double[] newPointsY) {
        this.m_pointsY = newPointsY;
    }

    /**
     * @return the m_pointsClasses
     */
    int[] getPointsClasses() {
        return m_pointsClasses;
    }

    /**
     * @param newPointsClasses
     *            the m_pointsClasses to set
     */
    void setPointsClasses(final int[] newPointsClasses) {
        this.m_pointsClasses = newPointsClasses;
    }

    /**
     * @return byte representation of the image of the coordinate panel
     */
    byte[] getImage() {
        return m_image;
    }

    /**
     * @param newImage
     *            the m_image to set
     */
    void setImage(final byte[] newImage) {
        this.m_image = newImage;
    }

    /**
     * @return the image width
     */
    int getImageWidth() {
        return m_imageWidth;
    }

    /**
     * @param newWidth
     *            the new width of the image
     */
    void setImageWidth(final int newWidth) {
        this.m_imageWidth = newWidth;
    }

    /**
     * @return the image height
     */
    int getImageHeight() {
        return m_imageHeight;
    }

    /**
     * @param newHeight
     *            the new height of the image
     */
    void setImageHeight(final int newHeight) {
        this.m_imageHeight = newHeight;
    }

}
