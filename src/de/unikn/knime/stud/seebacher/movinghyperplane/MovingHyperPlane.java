package de.unikn.knime.stud.seebacher.movinghyperplane;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;

/**
 * This class is used to generate synthetic streaming data using the algorithm described in the paper "Mining
 * Concept-Drifting Data Streams using Ensemble Classifiers" (see http://dl.acm.org/citation.cfm?id=956778) under the
 * section "6.2 Streaming Data".
 * 
 * @author Daniel Seebacher
 * 
 */
public class MovingHyperPlane {

    private static final double FIFTY_PERCENT = 0.5;
    private static final double TEN_PERCENT = 0.1;
    private final Random m_weightGenerator;
    private final Random m_exampleGenerator;
    private final Random m_noiseGenerator;
    private final Random m_directionsGenerator;
    private final Random m_weightUpdateIndexGenerator;

    private final int m_numberOfExamples;
    private final int m_dimensions;
    private final double m_noisePercentage;
    private final int m_updateWeightsCount;
    private final double m_magnitude;

    private int[] m_directions;
    private double[] m_hyperplaneWeights;
    private double m_hyperplane;

    /**
     * 
     * @param seed
     *            A seed which is used to initialize the random generators
     * @param numberOfExamples
     *            the number of examples which will be created
     * @param dimensions
     *            the dimensionality of the created examples
     * @param noisePercentage
     *            the noise percentage
     * @param updateWeightsCount
     *            the number of weights which will be updated after an example was created
     * @param magnitude
     *            the magnitude of change for the weights of the hyperplane
     */
    public MovingHyperPlane(final long seed, final int numberOfExamples, final int dimensions,
            final double noisePercentage, final int updateWeightsCount, final double magnitude) {
        // is used to create other random generators
        Random seedGenerator = new Random(seed);
        // is used initialize and update the weights of the hyperplane
        m_weightGenerator = new Random(seedGenerator.nextLong());
        // is used to create random data points
        m_exampleGenerator = new Random(seedGenerator.nextLong());
        // is used to switch labels of random data points
        m_noiseGenerator = new Random(seedGenerator.nextLong());
        // is used to initialize and update the directions
        m_directionsGenerator = new Random(seedGenerator.nextLong());
        // is used to determine which dimensions shall be updated
        m_weightUpdateIndexGenerator = new Random(seedGenerator.nextLong());

        this.m_numberOfExamples = numberOfExamples;
        this.m_dimensions = dimensions;
        this.m_noisePercentage = noisePercentage;
        this.m_updateWeightsCount = updateWeightsCount;
        this.m_magnitude = magnitude;

        m_directions = initDirections(dimensions, m_directionsGenerator);
        m_hyperplaneWeights = initHyperplaneWeights(dimensions, m_weightGenerator);
        m_hyperplane = calculateHyperPlane(m_hyperplaneWeights);
    }

    /**
     * Creates a single example.
     * 
     * @param rowNumber
     *            the rowNumber of the example
     * 
     * @return An array of DataRows containing a label for a positive or a negative example and the n-dimensional data.
     */
    public DataRow nextExample(final int rowNumber) {

        // 1. generate a random point
        double[] point = generateRandomPoint(m_dimensions, m_exampleGenerator);

        // 2. check if the point is a positive or a negative example
        boolean label = isPositiveExample(m_hyperplane, m_hyperplaneWeights, 
                point, m_noisePercentage, m_noiseGenerator);

        // 3. save the data in a row
        DataRow generatedExample = createDataRow(rowNumber, label, point);

        // 4. check which dimensions shall be updated
        int[] indices = indexOfWeightsToBeUpdated(m_dimensions, m_updateWeightsCount, m_weightUpdateIndexGenerator);

        // 5. update the weights of the hyperplane
        m_hyperplaneWeights = updateHyperplaneWeights(m_hyperplaneWeights, m_directions, indices, m_numberOfExamples,
                m_magnitude);

        // 6. update the directions
        m_directions = updateDirections(m_directions, indices, m_directionsGenerator);

        // 7. recalculate the hyperplane
        m_hyperplane = calculateHyperPlane(m_hyperplaneWeights);

        return generatedExample;
    }

    /**
     * Initializes the directions of the change of the hyperplane weights.
     * 
     * @param dimensions
     *            The dimensionality of the examples
     * @param directionsGenerator
     *            A random generator
     * @return An int array containing 1 or -1 which indicate the direction of the change
     */
    private int[] initDirections(final int dimensions, final Random directionsGenerator) {
        // each dimension of the hyperplane gets a direction in which it will be
        // moved
        int[] s = new int[dimensions];
        for (int i = 0; i < dimensions; i++) {
            s[i] = (directionsGenerator.nextDouble() < FIFTY_PERCENT) ? -1 : 1;
        }

        return s;
    }

    /**
     * Initializes the weights for each dimension of the hyperplane.
     * 
     * @param dimensions
     *            The dimensionality of the examples
     * @param weightGenerator
     *            A random generator
     * @return A double array containing the weights of each dimension of the hyperplane
     */
    private double[] initHyperplaneWeights(final int dimensions, final Random weightGenerator) {
        // initialize weights of every dimension
        double[] weights = new double[dimensions];
        for (int i = 0; i < dimensions; i++) {
            weights[i] = weightGenerator.nextDouble();
        }

        return weights;
    }

    /**
     * Calculates "the middle" of a hyperplane (used to check if a point is a positive or a negative example).
     * 
     * @param weights
     *            The weights of the dimensions of a hyperplane
     * @return
     */
    private double calculateHyperPlane(final double[] weights) {
        double hyperplane = 0;
        for (double weight : weights) {
            hyperplane += weight;
        }
        hyperplane /= 2;

        return hyperplane;
    }

    /**
     * Generates a random n-dimensional point.
     * 
     * @param dimensions
     *            The desired dimensionality of the data
     * @param exampleGenerator
     *            A random generator
     * @return A new point with a random location in n-dimensional space
     */
    private double[] generateRandomPoint(final int dimensions, final Random exampleGenerator) {
        double[] x = new double[dimensions];
        for (int j = 0; j < dimensions; j++) {
            x[j] = exampleGenerator.nextDouble();
        }

        return x;
    }

    /**
     * Checks on which side of the hyperplane a point is lying.
     * 
     * @param hyperplane
     *            The "middle" of the hyperplane
     * @param hyperplaneWeights
     *            The weights of the hyperplane
     * @param pointwWeights
     *            The weights of a point
     * @param noisePercentage
     *            The noise percentage (flips the labels of n% of the examples)
     * @param noiseGenerator
     *            A random generator
     * @return Returns true if point >= hyperplane is satisfied, otherwise false
     */
    private boolean isPositiveExample(final double hyperplane, final double[] hyperplaneWeights,
            final double[] pointwWeights, final double noisePercentage, final Random noiseGenerator) {
        // assign label to point by checking on which side of the hyper
        // plane it lies
        double point = 0;
        for (int j = 0; j < pointwWeights.length; j++) {
            point += pointwWeights[j] * hyperplaneWeights[j];
        }
        boolean label = (point >= hyperplane) ? true : false;

        // add noise by switching some labels
        if (noiseGenerator.nextDouble() <= noisePercentage) {
            label = (label) ? false : true;
        }

        return label;
    }

    /**
     * Returns the indices of the dimensions which shall be updated after each example is created. If toBeUpdated >=
     * dimensions, then all dimensions will be updated. If toBeupdated < dimensions, only some dimensions will get
     * updated.
     * 
     * @param dimensions
     *            The dimensionality of the data
     * @param toBeUpdated
     *            The number of dimensions which shall be updated
     * @param weightUpdateIndexGenerator
     *            A random generator
     * @return The indicies of dimensions which shall be updated.
     */
    private int[] indexOfWeightsToBeUpdated(final int dimensions, final int toBeUpdated,
            final Random weightUpdateIndexGenerator) {

        if (toBeUpdated >= dimensions) {
            int[] indices = new int[dimensions];
            for (int i = 0; i < dimensions; i++) {
                indices[i] = i;
            }
            return indices;
        } else {
            Set<Integer> weightUpdateIndices = new HashSet<Integer>();
            while (weightUpdateIndices.size() < toBeUpdated) {
                weightUpdateIndices.add(weightUpdateIndexGenerator.nextInt(dimensions));
            }

            int[] indices = new int[dimensions];
            for (int i = 0; i < dimensions; i++) {
                indices[i] = i;
            }
            return indices;
        }
    }

    /**
     * Changes the directions of the change for the dimensions of the hyperplane. Currently there is a 10% chance that a
     * directions get reversed.
     * 
     * @param directions
     *            The old directions
     * @param indices
     *            The indicies of the directions which shall be changed
     * @param directionsGenerator
     *            A random generator
     * @return The new directions of the change for the dimensions of the hyperplane
     */
    private int[] updateDirections(final int[] directions, final int[] indices, final Random directionsGenerator) {
        int[] newDirections = Arrays.copyOf(directions, directions.length);

        for (int index : indices) {
            newDirections[index] *= (directionsGenerator.nextDouble() <= TEN_PERCENT) ? -1 : 1;
        }

        return newDirections;
    }

    /**
     * Updates the weights of the dimensions of the hyperplane to simulate a drift.
     * 
     * @param hyperplaneWeights
     *            The old weights of the dimensions
     * @param directions
     *            The directions in which dimensions shall be changed
     * @param indices
     *            The indices of the dimensions which shall be changed
     * @param n
     *            The number of examples
     * @param magnitude
     *            The magnitude of change
     * @return The new
     */
    private double[] updateHyperplaneWeights(final double[] hyperplaneWeights, final int[] directions,
            final int[] indices, final int n, final double magnitude) {

        double[] newHyperplaneWeights = Arrays.copyOf(hyperplaneWeights, hyperplaneWeights.length);

        for (int index : indices) {
            newHyperplaneWeights[index] += directions[index] * magnitude / n;
        }

        return newHyperplaneWeights;
    }

    /**
     * @param number
     *            The row number
     * @param label
     *            A positive or a negative label
     * @param point
     *            The coordinates of the point
     * @return A new DataRow
     */
    private DataRow createDataRow(final int number, final boolean label, final double[] point) {

        RowKey key = new RowKey("Row " + number);

        DataCell[] cells = new DataCell[point.length + 1];
        cells[0] = new StringCell((label) ? "Positive" : "Negative");
        for (int i = 1; i <= point.length; i++) {
            cells[i] = new DoubleCell(point[i - 1]);
        }

        return new DefaultRow(key, cells);
    }
}
