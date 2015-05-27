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

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;

/**
 * Custom configuration dialog for the coordinate panel node.
 *
 * @author Daniel Seebacher, University of Konstanz.
 */
public final class CoordinatePanelNodeDialogPane extends NodeDialogPane {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(CoordinatePanelNodeDialogPane.class);

    private static final int MIN_XY = -10; // default negative value for axis
    private static final int MAX_XY = 10; // default positive value for axis
    private static final int GRID_XY = 1; // default value for grid width
    private static final int COLUMN_WIDTH = 11; // the column width of the jspinners

    private JPanel m_coordinatePanelSettingsPanel;
    private CoordinatePanel m_coordinatePanel;

    private JCheckBox m_snapToGridCheckbox;
    private JSpinner m_pointsClassSpinner;
    private JButton m_clearAllButton;

    private JSpinner m_minYSpinner;
    private JSpinner m_maxYSpinner;
    private JSpinner m_minXSpinner;
    private JSpinner m_maxXSpinner;
    private JSpinner m_gridYSpinner;
    private JSpinner m_gridXSpinner;

    private JPanel m_imageSettingsPanel;
    private JSpinner m_imageWidthSpinner;
    private JSpinner m_imageHeightSpinner;

    /**
     * Default constructor. Creates GUI Elements and initializes components with default values.
     */
    public CoordinatePanelNodeDialogPane() {

        // initliaze components with default values, get overriden once settings
        // are loaded
        initializeComponents();

        // Create Coordinate Panel Settings Panel
        m_coordinatePanelSettingsPanel = createCoordinatePanelSettingsPanel();
        addTab("Coordinate Panel Settings", m_coordinatePanelSettingsPanel);

        // Create Image Settings Panel
        m_imageSettingsPanel = createImageSettingsPanel();
        addTab("Image Settings", m_imageSettingsPanel);

        // Add necessary event listeners
        addListeners();
    }

    /**
     * Adds the ChangeListener to the JSpinners and a MouseListener to the coordinate panel.
     */
    private void addListeners() {

        // ActionListener for Clear button (remove all points)
        ActionListener al = new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                int n = JOptionPane.showConfirmDialog(null, "Remove all points?", "Confirmation",
                        JOptionPane.YES_NO_OPTION);

                if (n == JOptionPane.YES_OPTION) {
                    m_coordinatePanel.clearPoints();
                }
            }
        };

        m_clearAllButton.addActionListener(al);

        // ChangeListeners updates the dimensions of the coordinate panel if a
        // spinner was changed
        ChangeListener cl = new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                m_coordinatePanel.setMinY((Integer) m_minYSpinner.getValue());
                m_coordinatePanel.setMaxY((Integer) m_maxYSpinner.getValue());
                m_coordinatePanel.setMinX((Integer) m_minXSpinner.getValue());
                m_coordinatePanel.setMaxX((Integer) m_maxXSpinner.getValue());
                m_coordinatePanel.setGridY((Integer) m_gridYSpinner.getValue());
                m_coordinatePanel.setGridX((Integer) m_gridXSpinner.getValue());
            }
        };

        // add changelistener to all necessary components
        m_minYSpinner.addChangeListener(cl);
        m_maxYSpinner.addChangeListener(cl);
        m_minXSpinner.addChangeListener(cl);
        m_maxXSpinner.addChangeListener(cl);
        m_gridYSpinner.addChangeListener(cl);
        m_gridXSpinner.addChangeListener(cl);

        // MouseListener for coordinate panel, left click adds a point, right
        // click removes one
        MouseListener ml = new MouseListener() {

            @Override
            public void mouseReleased(final MouseEvent arg0) {
            }

            @Override
            public void mousePressed(final MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    m_coordinatePanel.addPoint(e.getX(), e.getY(), (Integer) m_pointsClassSpinner.getValue(),
                            m_snapToGridCheckbox.isSelected());
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    m_coordinatePanel.removePoint(e.getX(), e.getY());
                } else {
                    return;
                }
            }

            @Override
            public void mouseExited(final MouseEvent arg0) {
            }

            @Override
            public void mouseEntered(final MouseEvent arg0) {
            }

            @Override
            public void mouseClicked(final MouseEvent e) {

            }
        };

        m_coordinatePanel.addMouseListener(ml);
    }

    /**
     * Initialize components using default values.
     */
    private void initializeComponents() {
        // initialize components
        m_minYSpinner = new JSpinner();
        m_maxYSpinner = new JSpinner();
        m_minXSpinner = new JSpinner();
        m_maxXSpinner = new JSpinner();
        m_gridYSpinner = new JSpinner();
        m_gridXSpinner = new JSpinner();
        m_pointsClassSpinner = new JSpinner();
        m_snapToGridCheckbox = new JCheckBox();

        m_imageWidthSpinner = new JSpinner();
        m_imageHeightSpinner = new JSpinner();

        // set default values
        m_coordinatePanel = new CoordinatePanel(MIN_XY, MAX_XY, MIN_XY, MAX_XY, GRID_XY, GRID_XY);

        // create default layout for the JSpinners, set the max. column size to
        // 11 (max integer length + minus sign)
        m_minYSpinner.setModel(new SpinnerNumberModel(MIN_XY, Integer.MIN_VALUE, 0, 1));
        ((JSpinner.DefaultEditor) m_minYSpinner.getEditor()).getTextField().setColumns(COLUMN_WIDTH);
        m_maxYSpinner.setModel(new SpinnerNumberModel(MAX_XY, 0, Integer.MAX_VALUE, 1));
        ((JSpinner.DefaultEditor) m_maxYSpinner.getEditor()).getTextField().setColumns(COLUMN_WIDTH);
        m_minXSpinner.setModel(new SpinnerNumberModel(MIN_XY, Integer.MIN_VALUE, 0, 1));
        ((JSpinner.DefaultEditor) m_minXSpinner.getEditor()).getTextField().setColumns(COLUMN_WIDTH);
        m_maxXSpinner.setModel(new SpinnerNumberModel(MAX_XY, 0, Integer.MAX_VALUE, 1));
        ((JSpinner.DefaultEditor) m_maxXSpinner.getEditor()).getTextField().setColumns(COLUMN_WIDTH);
        m_gridYSpinner.setModel(new SpinnerNumberModel(GRID_XY, 1, Integer.MAX_VALUE, 1));
        ((JSpinner.DefaultEditor) m_gridYSpinner.getEditor()).getTextField().setColumns(COLUMN_WIDTH);
        m_gridXSpinner.setModel(new SpinnerNumberModel(GRID_XY, 1, Integer.MAX_VALUE, 1));
        ((JSpinner.DefaultEditor) m_gridXSpinner.getEditor()).getTextField().setColumns(COLUMN_WIDTH);

        // for the pointclass spinner the max. column size doesn't need to be
        // that big
        m_pointsClassSpinner.setModel(new SpinnerNumberModel(1, 1, 54, 1));
        ((JSpinner.DefaultEditor) m_pointsClassSpinner.getEditor()).getTextField().setColumns(4);

        m_clearAllButton = new JButton("Clear");

        m_imageWidthSpinner.setModel(new SpinnerNumberModel(800, 256, 16384, 1));
        ((JSpinner.DefaultEditor) m_imageWidthSpinner.getEditor()).getTextField().setColumns(COLUMN_WIDTH);

        m_imageHeightSpinner.setModel(new SpinnerNumberModel(800, 256, 16384, 1));
        ((JSpinner.DefaultEditor) m_imageHeightSpinner.getEditor()).getTextField().setColumns(COLUMN_WIDTH);
    }

    /**
     * Creates the tab which is added to the NodeDialogPane.
     *
     * @return, a JPanel containing the necessary JSpinners, Buttons and the coordinate panel
     */
    private JPanel createCoordinatePanelSettingsPanel() {

        // Create a JPanel and add the coordinate panel
        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new BorderLayout(5, 5));
        settingsPanel.add(m_coordinatePanel, BorderLayout.CENTER);

        // Create a second JPanel which holds the JSpinners, this one has a
        // BoxLayout
        JPanel secondPanel = new JPanel();
        secondPanel.setLayout(new BoxLayout(secondPanel, BoxLayout.Y_AXIS));

        // Create a Box for the Min. Y Spinner
        Box minYBox = Box.createHorizontalBox();
        minYBox.add(new JLabel("Min. Y"));
        minYBox.add(Box.createRigidArea(new Dimension(9, 15)));
        minYBox.add(m_minYSpinner);
        m_minYSpinner.setMaximumSize(new Dimension(0, 25));
        minYBox.add(Box.createHorizontalGlue());

        // Create a Box for the Max. Y Spinner
        Box maxYBox = Box.createHorizontalBox();
        maxYBox.add(new JLabel("Max. Y"));
        maxYBox.add(Box.createRigidArea(new Dimension(5, 15)));
        maxYBox.add(m_maxYSpinner);
        m_maxYSpinner.setMaximumSize(new Dimension(0, 25));
        maxYBox.add(Box.createHorizontalGlue());

        // Create a Box for the Min. X Spinner
        Box minXBox = Box.createHorizontalBox();
        minXBox.add(new JLabel("Min. X"));
        minXBox.add(Box.createRigidArea(new Dimension(9, 15)));
        minXBox.add(m_minXSpinner);
        m_minXSpinner.setMaximumSize(new Dimension(0, 25));
        minXBox.add(Box.createHorizontalGlue());

        // Create a Box for the Max. X Spinner
        Box maxXBox = Box.createHorizontalBox();
        maxXBox.add(new JLabel("Max. X"));
        maxXBox.add(Box.createRigidArea(new Dimension(5, 15)));
        maxXBox.add(m_maxXSpinner);
        m_maxXSpinner.setMaximumSize(new Dimension(0, 25));
        maxXBox.add(Box.createHorizontalGlue());

        // Create a Box for the Grid. Y Spinner
        Box gridYBox = Box.createHorizontalBox();
        gridYBox.add(new JLabel("Grid. Y"));
        gridYBox.add(Box.createRigidArea(new Dimension(6, 15)));
        gridYBox.add(m_gridYSpinner);
        m_gridYSpinner.setMaximumSize(new Dimension(0, 25));
        gridYBox.add(Box.createHorizontalGlue());

        // Create a Box for the Grid. X Spinner
        Box gridXBox = Box.createHorizontalBox();
        gridXBox.add(new JLabel("Grid. X"));
        gridXBox.add(Box.createRigidArea(new Dimension(6, 15)));
        gridXBox.add(m_gridXSpinner);
        m_gridXSpinner.setMaximumSize(new Dimension(0, 25));
        gridXBox.add(Box.createHorizontalGlue());

        // add all boxes and some spaces in between to the option panel
        secondPanel.add(minYBox);
        secondPanel.add(Box.createRigidArea(new Dimension(15, 15)));
        secondPanel.add(maxYBox);
        secondPanel.add(Box.createRigidArea(new Dimension(15, 15)));
        secondPanel.add(minXBox);
        secondPanel.add(Box.createRigidArea(new Dimension(15, 15)));
        secondPanel.add(maxXBox);
        secondPanel.add(Box.createRigidArea(new Dimension(15, 15)));
        secondPanel.add(gridYBox);
        secondPanel.add(Box.createRigidArea(new Dimension(15, 15)));
        secondPanel.add(gridXBox);

        // add the option panel to the layoutpanel
        settingsPanel.add(secondPanel, BorderLayout.WEST);

        // Create a third JPanel which holds the Clear All Button, the
        // Pointclass Spinner and the Snap to Grid Checkbox
        JPanel thirdPanel = new JPanel();
        thirdPanel.setLayout(new BoxLayout(thirdPanel, BoxLayout.X_AXIS));

        thirdPanel.add(Box.createHorizontalGlue());
        thirdPanel.add(new JLabel("Snap to Grid"));
        thirdPanel.add(Box.createRigidArea(new Dimension(5, 15)));
        thirdPanel.add(m_snapToGridCheckbox);
        thirdPanel.add(Box.createRigidArea(new Dimension(25, 15)));
        thirdPanel.add(new JLabel("Class"));
        thirdPanel.add(Box.createRigidArea(new Dimension(5, 15)));
        thirdPanel.add(m_pointsClassSpinner);
        m_pointsClassSpinner.setMaximumSize(new Dimension(100, 25));
        thirdPanel.add(Box.createRigidArea(new Dimension(25, 15)));
        thirdPanel.add(m_clearAllButton);

        // add the third panel to the layoutpanel
        settingsPanel.add(thirdPanel, BorderLayout.NORTH);

        return settingsPanel;
    }

    private JPanel createImageSettingsPanel() {
        JPanel parentPanel = new JPanel();
        parentPanel.setLayout(new BorderLayout());

        JPanel imageSettingsPanel = new JPanel();
        imageSettingsPanel.setLayout(new BoxLayout(imageSettingsPanel, BoxLayout.Y_AXIS));
        imageSettingsPanel.add(Box.createVerticalGlue());

        Box widthBox = Box.createHorizontalBox();
        widthBox.add(Box.createHorizontalGlue());
        widthBox.add(new JLabel("Width:  "));
        widthBox.add(Box.createRigidArea(new Dimension(5, 15)));
        widthBox.add(m_imageWidthSpinner);
        widthBox.add(Box.createHorizontalGlue());
        m_imageWidthSpinner.setMaximumSize(new Dimension(0, 25));
        imageSettingsPanel.add(widthBox);

        imageSettingsPanel.add(Box.createRigidArea(new Dimension(5, 15)));

        Box heightBox = Box.createHorizontalBox();
        heightBox.add(Box.createHorizontalGlue());
        heightBox.add(new JLabel("Height: "));
        heightBox.add(Box.createRigidArea(new Dimension(5, 15)));
        heightBox.add(m_imageHeightSpinner);
        heightBox.add(Box.createHorizontalGlue());
        m_imageHeightSpinner.setMaximumSize(new Dimension(0, 25));
        imageSettingsPanel.add(heightBox);

        imageSettingsPanel.add(Box.createVerticalGlue());

        return imageSettingsPanel;
    }

    /** {@inheritDoc} */
    @Override
    protected void loadSettingsFrom(final NodeSettingsRO settings, final DataTableSpec[] specs)
            throws NotConfigurableException {

        // Load Configurations
        CoordinatePanelNodeConfiguration c = new CoordinatePanelNodeConfiguration();
        c.loadConfigurationInDialog(settings);

        // Set Models for the JSpinners and set the max. column size to 11 (max
        // integer length + minus sign)
        m_minYSpinner.setModel(new SpinnerNumberModel(c.getMinY(), Integer.MIN_VALUE, 0, 1));
        ((JSpinner.DefaultEditor) m_minYSpinner.getEditor()).getTextField().setColumns(11);
        m_maxYSpinner.setModel(new SpinnerNumberModel(c.getMaxY(), 0, Integer.MAX_VALUE, 1));
        ((JSpinner.DefaultEditor) m_maxYSpinner.getEditor()).getTextField().setColumns(11);
        m_minXSpinner.setModel(new SpinnerNumberModel(c.getMinX(), Integer.MIN_VALUE, 0, 1));
        ((JSpinner.DefaultEditor) m_minXSpinner.getEditor()).getTextField().setColumns(11);
        m_maxXSpinner.setModel(new SpinnerNumberModel(c.getMaxX(), 0, Integer.MAX_VALUE, 1));
        ((JSpinner.DefaultEditor) m_maxXSpinner.getEditor()).getTextField().setColumns(11);
        m_gridYSpinner.setModel(new SpinnerNumberModel(c.getGridY(), 1, Integer.MAX_VALUE, 1));
        ((JSpinner.DefaultEditor) m_gridYSpinner.getEditor()).getTextField().setColumns(11);
        m_gridXSpinner.setModel(new SpinnerNumberModel(c.getGridX(), 1, Integer.MAX_VALUE, 1));
        ((JSpinner.DefaultEditor) m_gridXSpinner.getEditor()).getTextField().setColumns(11);

        // set the dimensions of the coordinate panel
        m_coordinatePanel.setMinY(c.getMinY());
        m_coordinatePanel.setMaxY(c.getMaxY());
        m_coordinatePanel.setMinX(c.getMinX());
        m_coordinatePanel.setMaxX(c.getMaxX());
        m_coordinatePanel.setGridY(c.getGridY());
        m_coordinatePanel.setGridX(c.getGridX());

        // load points (if there are any)
        double[] pointsX = c.getPointsX();
        double[] pointsY = c.getPointsY();
        int[] pointsClasses = c.getPointsClasses();

        if (pointsX != null && pointsY != null && pointsClasses != null) {
            List<Point> points = new ArrayList<Point>();
            for (int i = 0; i < pointsX.length; i++) {
                points.add(new Point(pointsX[i], pointsY[i], pointsClasses[i]));
            }
            m_coordinatePanel.loadPointList(points);
        }

        m_imageWidthSpinner.setModel(new SpinnerNumberModel(c.getImageWidth(), 256, 16384, 1));
        ((JSpinner.DefaultEditor) m_imageWidthSpinner.getEditor()).getTextField().setColumns(COLUMN_WIDTH);

        m_imageHeightSpinner.setModel(new SpinnerNumberModel(c.getImageHeight(), 256, 16384, 1));
        ((JSpinner.DefaultEditor) m_imageHeightSpinner.getEditor()).getTextField().setColumns(COLUMN_WIDTH);
    }

    /** {@inheritDoc} */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {

        CoordinatePanelNodeConfiguration c = new CoordinatePanelNodeConfiguration();

        // save dimensions for the coordinate panel
        c.setMinY((Integer) m_minYSpinner.getValue());
        c.setMaxY((Integer) m_maxYSpinner.getValue());
        c.setMinX((Integer) m_minXSpinner.getValue());
        c.setMaxX((Integer) m_maxXSpinner.getValue());
        c.setGridY((Integer) m_gridYSpinner.getValue());
        c.setGridX((Integer) m_gridXSpinner.getValue());

        // Transform the point list into three arrays to save them with
        // settings.addArray
        List<Point> points = m_coordinatePanel.getPoints();
        if (points != null && !points.isEmpty()) {
            double[] pointsX = new double[points.size()];
            double[] pointsY = new double[points.size()];
            int[] pointsClasses = new int[points.size()];

            for (int i = 0; i < points.size(); i++) {
                pointsX[i] = points.get(i).getX();
                pointsY[i] = points.get(i).getY();
                pointsClasses[i] = points.get(i).getPointClass();
            }

            c.setPointsX(pointsX);
            c.setPointsY(pointsY);
            c.setPointsClasses(pointsClasses);
        }

        int imageWidth = (Integer) m_imageWidthSpinner.getValue();
        int imageHeight = (Integer) m_imageHeightSpinner.getValue();
        c.setImageWidth(imageWidth);
        c.setImageHeight(imageHeight);

        // create image of the coordinate panel
        CoordinatePanel copy = new CoordinatePanel(m_coordinatePanel);
        copy.setSize(imageWidth, imageHeight);
        BufferedImage bi = createBufferedImage(copy);
        byte[] imageBytes = bufferedImageToByteArray(bi);
        c.setImage(imageBytes);

        c.saveConfiguration(settings);

    }

    private BufferedImage createBufferedImage(final CoordinatePanel cp) {
        int w = cp.getWidth();
        int h = cp.getHeight();
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();
        cp.paint(g);
        return bi;
    }

    private byte[] bufferedImageToByteArray(final BufferedImage bi) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(bi, "PNG", baos);
        } catch (IOException e) {
            LOGGER.error("Couldn't create an PNG Image of the Coordinate Panel", e);
        }
        return baos.toByteArray();
    }

    /**
     * A coordinate panel which displays points.
     *
     * @author Daniel Seebacher
     */
    static class CoordinatePanel extends JPanel {

        private static final long serialVersionUID = 1L;
        private double m_minY;
        private double m_maxY;
        private double m_minX;
        private double m_maxX;
        private double m_gridY;
        private double m_gridX;
        private static final int OFFSET = 20;

        private final List<Point> m_points = new ArrayList<Point>();

        /**
         * Default constructor for the coordinate panel class.
         *
         * @param minY
         *            the minimum y value of the y axis
         * @param maxY
         *            the maximum y value of the y axis
         * @param minX
         *            the minimum x value of the x axis
         * @param maxX
         *            the maximum x value of the x axis
         * @param gridY
         *            the distance between grid lines on the y axis
         * @param gridX
         *            the distance between grid lines on the x axis
         */
        CoordinatePanel(final double minY, final double maxY, final double minX, final double maxX,
                final double gridY, final double gridX) {
            super();

            setPreferredSize(new Dimension(500, 500));

            this.m_minY = minY;
            this.m_maxY = maxY;
            this.m_minX = minX;
            this.m_maxX = maxX;
            this.m_gridY = gridY;
            this.m_gridX = gridX;

            addMouseMotionListener(createMouseMotionListener());
        }

        /**
         * Copy Constructor.
         *
         * @param cp
         *            a CoordinatePanel
         */
        CoordinatePanel(final CoordinatePanel cp) {
            super();

            setPreferredSize(new Dimension(500, 500));

            this.m_minY = cp.m_minY;
            this.m_maxY = cp.m_maxY;
            this.m_minX = cp.m_minX;
            this.m_maxX = cp.m_maxX;
            this.m_gridY = cp.m_gridY;
            this.m_gridX = cp.m_gridX;

            addMouseMotionListener(createMouseMotionListener());

            loadPointList(cp.getPoints());
        }

        @Override
        public void paintComponent(final Graphics g) {

            if (this.getWidth() < 1 || this.getHeight() < 1) {
                return;
            }

            super.paintComponent(g);

            // use anti-aliasing
            final Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // use white as background color
            g2d.setColor(Color.white);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.setColor(Color.black);

            // get default stroke and use a bigger stroke for the axis
            final Stroke defaultStroke = g2d.getStroke();
            final Stroke axisStroke = new BasicStroke(3f);
            g2d.setStroke(axisStroke);

            // get x,y coordinates of the origin origin
            final int[] origin = transformCoordinateToScreen(0, 0);

            // draw y axis
            g2d.drawLine(origin[0], OFFSET, origin[0], getHeight() - OFFSET);
            // draw x axis
            g2d.drawLine(OFFSET, origin[1], getWidth() - OFFSET, origin[1]);

            // x axis left arrow
            if (m_minX != 0) {
                g2d.drawLine(OFFSET, origin[1], OFFSET + 5, origin[1] - 5);
                g2d.drawLine(OFFSET, origin[1], OFFSET + 5, origin[1] + 5);
            }

            // x axis right arrow
            if (m_maxX != 0) {
                g2d.drawLine(getWidth() - OFFSET, origin[1], getWidth() - (OFFSET + 5), origin[1] - 5);
                g2d.drawLine(getWidth() - OFFSET, origin[1], getWidth() - (OFFSET + 5), origin[1] + 5);
            }

            // y axis bottom arrow
            if (m_minY != 0) {
                g2d.drawLine(origin[0], getHeight() - OFFSET, origin[0] - 5, getHeight() - (OFFSET + 5));
                g2d.drawLine(origin[0], getHeight() - OFFSET, origin[0] + 5, getHeight() - (OFFSET + 5));
            }

            // y axis top arrow
            if (m_maxY != 0) {
                g2d.drawLine(origin[0], OFFSET, origin[0] - 5, OFFSET + 5);
                g2d.drawLine(origin[0], OFFSET, origin[0] + 5, OFFSET + 5);
            }

            // draw grid
            g2d.setStroke(defaultStroke);

            // calculate screen difference from one grid line to another grid
            // line
            final int[] gridPoint = transformCoordinateToScreen(m_gridX, m_gridY);
            final int xDifference = Math.abs(gridPoint[0] - origin[0]);
            final int yDifference = Math.abs(gridPoint[1] - origin[1]);

            // draw x grid lines on the negative side and grid numbering
            int count = 0;
            for (int i = origin[0]; i > OFFSET * 2; i -= xDifference) {
                g2d.drawLine(i, OFFSET, i, getHeight() - OFFSET);

                if (count != 0 && count % 2 == 0 && m_gridX > 5) {
                    g2d.drawString(String.valueOf(((int) m_gridX * count) * -1), i + 2, origin[1] - 5);
                }
                ++count;
            }

            // draw x grid lines on the positive side and grid numbering
            FontMetrics fm = g2d.getFontMetrics();
            count = 0;
            for (int i = origin[0]; i < getWidth() - OFFSET * 2; i += xDifference) {
                g2d.drawLine(i, OFFSET, i, getHeight() - OFFSET);

                if (count != 0 && count % 2 == 0 && m_gridX > 5) {
                    g2d.drawString(String.valueOf((int) m_gridX * count),
                            i - fm.stringWidth(String.valueOf((int) m_gridX * count)) - 2, origin[1] - 5);
                }
                ++count;
            }

            // draw y grid lines on the positive side
            count = 0;
            for (int i = origin[1]; i > OFFSET * 2; i -= yDifference) {
                g2d.drawLine(OFFSET, i, getWidth() - OFFSET, i);

                if (count != 0 && count % 2 == 0 && m_gridX > 5) {
                    g2d.drawString(String.valueOf((int) m_gridY * count), origin[0] + 4, i + 2 + fm.getHeight());
                }
                ++count;
            }

            // draw y grid lines on the negative side
            count = 0;
            for (int i = origin[1]; i < getHeight() - OFFSET * 2; i += yDifference) {
                g2d.drawLine(OFFSET, i, getWidth() - OFFSET, i);

                if (count != 0 && count % 2 == 0 && m_gridX > 5) {
                    g2d.drawString(String.valueOf(((int) m_gridY * count) * -1), origin[0] + 4, i - 2);
                }
                ++count;
            }

            // add points to coordinate system
            for (final Point p : m_points) {
                // calculate the position on the screen from the coordinates
                final int[] xy = transformCoordinateToScreen(p.getX(), p.getY());
                // use point color
                g2d.setColor(p.getColor());
                g2d.fillOval(xy[0] - 5, xy[1] - 5, 10, 10);
            }
        }

        /**
         * Removes all points which are currently in this coordinate panel.
         */
        private void clearPoints() {
            m_points.clear();
            repaint();
        }

        /**
         * Adds a point to the coordinate panel.
         *
         * @param x
         *            x coordinate on the screen
         * @param y
         *            y coordinate on the screen
         * @param pointClass
         *            a class which the point shall have (1-54)
         * @param snapToGrid
         *            true if the point should be added to the nearest intersection of the grid lines
         */
        private void addPoint(final int x, final int y, final int pointClass, final boolean snapToGrid) {

            // check if snap to grid is activated
            if (snapToGrid) {
                double[] xy = transformScreenToCoordinate(x, y);

                double modX = xy[0] % m_gridX;
                double modY = xy[1] % m_gridY;

                // if x is negative, just map it to the positive side
                if (xy[0] < 0) {

                    xy[0] = Math.abs(xy[0]);
                    modX = Math.abs(modX);

                    // checks on which side of the grid the point should be
                    // drawn
                    xy[0] = (modX > (m_gridX / 2)) ? xy[0] - modX + m_gridX : xy[0] - modX;

                    // reverse direction (to match negative side)
                    xy[0] *= -1d;

                } else {
                    // checks on which side of the grid the point should be
                    // drawn
                    xy[0] = (modX > (m_gridX / 2)) ? xy[0] - modX + m_gridX : xy[0] - modX;
                }

                // if y is negative, just map it to the positive side
                if (xy[1] < 0) {
                    xy[1] = Math.abs(xy[1]);
                    modY = Math.abs(modY);

                    // checks on which side of the grid the point should be
                    // drawn
                    xy[1] = (modY > (m_gridY / 2)) ? xy[1] - modY + m_gridY : xy[1] - modY;

                    // reverse direction (to match negative side)
                    xy[1] *= -1d;
                } else {
                    // checks on which side of the grid the point should be
                    // drawn
                    xy[1] = (modY > (m_gridY / 2)) ? xy[1] - modY + m_gridY : xy[1] - modY;
                }

                m_points.add(new Point(xy[0], xy[1], pointClass));
            } else {
                final double[] xy = transformScreenToCoordinate(x, y);
                m_points.add(new Point(xy[0], xy[1], pointClass));
            }

            // repaint after adding the point
            repaint();
        }

        /**
         * Removes the point nearest to the given coordinates, but only if there is a point nearby.
         *
         * @param x
         *            , x coordinate on the screen
         * @param y
         *            , y coordinate on the screen
         */
        private void removePoint(final int x, final int y) {
            // transform screen coordinates to real coordinates
            final double[] xy = transformScreenToCoordinate(x, y);

            // find the nearest neighbor
            double minDistance = Double.MAX_VALUE;
            Point nearestPoint = null;
            for (final Point p : m_points) {

                // if the distance on the screen is too big, continue;
                final int[] pointToScreen = transformCoordinateToScreen(p.getX(), p.getY());
                if (Math.abs(x - pointToScreen[0]) > 5 || Math.abs(y - pointToScreen[1]) > 5) {
                    continue;
                }

                // get the nearest point
                final double distance = Math.sqrt(Math.pow(xy[0] - p.getX(), 2) + Math.pow(xy[1] - p.getY(), 2));

                if (distance < minDistance) {
                    nearestPoint = p;
                    minDistance = distance;
                }
            }

            // if there is a point nearby, delete it
            if (nearestPoint == null) {
                return;
            } else {
                m_points.remove(nearestPoint);
            }

            // repaint coordinate panel after removing a point
            repaint();
        }

        /**
         * Transforms a point on the screen to a point on the coordinate panel.
         *
         * @param x
         *            , the x coordinate on the screen
         * @param y
         *            , the y coordinate on the screen
         * @return A double[] array containing the x,y values on the coordinate panel
         */
        private double[] transformScreenToCoordinate(final double x, final double y) {

            double reversedY = getHeight() - y;

            final double minScreenY = OFFSET;
            final double maxScreenY = getHeight() - OFFSET;
            final double minScreenX = OFFSET;
            final double maxScreenX = getWidth() - OFFSET;

            final double xCoordinate = (m_maxX - m_minX) * ((x - minScreenX) / (maxScreenX - minScreenX)) + m_minX;
            final double yCoordinate = (m_maxY - m_minY) * ((reversedY - minScreenY) / (maxScreenY - minScreenY))
                    + m_minY;

            return new double[] {xCoordinate, yCoordinate};
        }

        /**
         * Transforms a point in the coordinate panel to a point on the screen.
         *
         * @param x
         *            , the x coordinate in the coordinate panel
         * @param y
         *            , the y coordinate in the coordinate panel
         * @return A double[] array containing the x,y values on the screen
         */
        private int[] transformCoordinateToScreen(final double x, final double y) {

            final double minScreenY = OFFSET;
            final double maxScreenY = getHeight() - OFFSET;
            final double minScreenX = OFFSET;
            final double maxScreenX = getWidth() - OFFSET;

            final double xCoordinate = (maxScreenX - minScreenX) * ((x - m_minX) / (m_maxX - m_minX)) + minScreenX;
            final double yCoordinate = (maxScreenY - minScreenY) * ((y - m_minY) / (m_maxY - m_minY)) + minScreenY;

            return new int[] {(int) xCoordinate, getHeight() - (int) yCoordinate};
        }

        /**
         * @return Simple MouseMotionListener which sets the tool tip to the x,y coordinates the mouse is hovering over
         */
        private MouseMotionListener createMouseMotionListener() {
            final MouseMotionListener mml = new MouseMotionListener() {

                @Override
                public void mouseMoved(final MouseEvent e) {
                    // check if mouse is inside the coordinate system
                    if (e.getX() < OFFSET || e.getY() < OFFSET || e.getX() > getWidth() - OFFSET
                            || e.getY() > getHeight() - OFFSET) {
                        setToolTipText(null);
                    } else {
                        final double[] xy = transformScreenToCoordinate(e.getX(), e.getY());

                        Point nearestPoint = null;
                        double minDist = Double.MAX_VALUE;
                        for (final Point p : m_points) {
                            final double coordDistance = Math.sqrt(Math.pow(xy[0] - p.getX(), 2)
                                    + Math.pow(xy[1] - p.getY(), 2));

                            final int[] screenCord = transformCoordinateToScreen(p.getX(), p.getY());
                            if (Math.abs(e.getX() - screenCord[0]) > 5 || Math.abs(e.getY() - screenCord[1]) > 5) {
                                continue;
                            }

                            if (coordDistance < minDist) {
                                nearestPoint = p;
                                minDist = coordDistance;
                            }
                        }

                        DecimalFormat df = new DecimalFormat("#.##");
                        if (nearestPoint == null) {
                            setToolTipText("x = " + df.format(xy[0]) + " y = " + df.format(xy[1]));
                        } else {
                            setToolTipText("Point: x = " + df.format(nearestPoint.getX()) + " y = "
                                    + df.format(nearestPoint.getY()) + " Class = " + nearestPoint.getPointClass());
                        }
                    }
                }

                @Override
                public void mouseDragged(final MouseEvent e) {
                }
            };

            return mml;
        }

        /**
         * Saves the given List of Points, only if they're not already added.
         *
         * @param points
         *            , a List of points
         */
        private void loadPointList(final List<Point> points) {
            for (Point point : points) {
                if (!this.m_points.contains(point)) {
                    this.m_points.add(point);
                }
            }

            // repaint after adding the points
            repaint();
        }

        /**
         * @return all points which are currently in the coordinate panel.
         */
        private List<Point> getPoints() {
            return m_points;
        }

        /**
         * Sets the minimum y value of the y axis.
         * @param minY the new minimum y value.
         */
        private void setMinY(final double minY) {
            this.m_minY = minY;
            repaint();
        }

        /**
         * Sets the maximum y value of the y axis.
         * @param maxY the new maximum y value.
         */
        private void setMaxY(final double maxY) {
            this.m_maxY = maxY;
            repaint();
        }

        /**
         * Sets the minimum x value of the x axis.
         * @param minX the new minimum x value.
         */
        private void setMinX(final double minX) {
            this.m_minX = minX;
            repaint();
        }

        /**
         * Sets the maximum x value of the x axis.
         * @param maxX the new maximum x value.
         */
        private void setMaxX(final double maxX) {
            this.m_maxX = maxX;
            repaint();
        }

        /**
         * Sets the grid Y value.
         * @param gridY the new grid y value.
         */
        private void setGridY(final double gridY) {
            this.m_gridY = gridY;
            repaint();
        }

        /**
         * Sets the grid X value.
         * @param gridX the new grid x value.
         */
        private void setGridX(final double gridX) {
            this.m_gridX = gridX;
            repaint();
        }
    }

    /**
     * Representation of a Point, holds the x,y coordinates and a class value.
     *
     * @author Daniel Seebacher
     *
     */
    private static class Point {
        private final double m_x;
        private final double m_y;

        private final int m_pointClass;

        public Point(final double x, final double y, final int pointClass) {
            this.m_x = x;
            this.m_y = y;
            this.m_pointClass = pointClass;
        }

        public double getX() {
            return m_x;
        }

        public double getY() {
            return m_y;
        }

        public int getPointClass() {
            return m_pointClass;
        }

        @Override
        public String toString() {
            return m_x + " " + m_y + " " + m_pointClass;
        }

        @Override
        public boolean equals(final Object obj) {
            if (!(obj instanceof Point)) {
                return false;
            }

            final Point otherPoint = (Point) obj;

            // No exact matching, a little error is allowed (rounding mistakes
            // by double)
            double epsilon = 0.001;
            double distance = euclideanDistance(otherPoint.getX(), this.getX(), otherPoint.getY(), this.getY());

            if (distance > epsilon || otherPoint.getPointClass() != this.getPointClass()) {
                return false;
            }
            return true;
        }

        private double euclideanDistance(final double x2, final double x1, final double y2, final double y1) {

            double diffX = Math.pow(x2 - x1, 2);
            double diffY = Math.pow(y2 - y1, 2);

            return Math.sqrt(diffX + diffY);
        }

        @Override
        public int hashCode() {
            final int firstPrime = 31;
            final int secondPrime = 17;
            final int thirdPrime = 31;
            return (int) (m_x * firstPrime + m_y * secondPrime + m_pointClass * thirdPrime);
        }

        /**
         * @return The color assigned to this point, determined by its class
         */
        public Color getColor() {
            return hex2Rgb(excelColors(m_pointClass));
        }

        /**
         * Assigns each class a different color, using the excel color set see:
         * (http://dmcritchie.mvps.org/excel/colors.htm | last accessed 18.02.2013).
         *
         * @param pointClass
         *            the class of a point
         * @return
         */
        private String excelColors(final int pointClass) {
            final String[] excelHexColors = {"#FF0000", "#00FF00", "#0000FF", "#FFFF00", "#FF00FF", "#00FFFF",
                    "#800000", "#008000", "#000080", "#808000", "#800080", "#008080", "#C0C0C0", "#808080", "#9999FF",
                    "#993366", "#FFFFCC", "#CCFFFF", "#660066", "#FF8080", "#0066CC", "#CCCCFF", "#000080", "#FF00FF",
                    "#FFFF00", "#00FFFF", "#800080", "#800000", "#008080", "#0000FF", "#00CCFF", "#CCFFFF", "#CCFFCC",
                    "#FFFF99", "#99CCFF", "#FF99CC", "#CC99FF", "#FFCC99", "#3366FF", "#33CCCC", "#99CC00", "#FFCC00",
                    "#FF9900", "#FF6600", "#666699", "#969696", "#003366", "#339966", "#003300", "#333300", "#993300",
                    "#993366", "#333399", "#333333" };
            return excelHexColors[pointClass - 1];
        }

        /**
         * Turns a hex representation of an RGB color into a color.
         *
         * @param colorStr
         *            a hex representation of a color
         * @return A Color
         */
        private Color hex2Rgb(final String colorStr) {
            return new Color(Integer.valueOf(colorStr.substring(1, 3), 16),
                    Integer.valueOf(colorStr.substring(3, 5), 16),
                    Integer.valueOf(colorStr.substring(5, 7), 16));
        }
    }

}
