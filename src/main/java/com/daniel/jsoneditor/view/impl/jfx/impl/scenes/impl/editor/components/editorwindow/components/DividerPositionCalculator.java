package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components;

import java.util.Arrays;


/**
 * Pure-logic calculator for SplitPane divider positions.
 *
 * <p>The algorithm works in two modes:</p>
 * <ul>
 *   <li><b>Normal mode</b> (available &ge; sum of minimums): every panel receives its minimum
 *       first.  The remaining extra space is distributed proportionally to each panel's
 *       {@code weight} (which is typically derived from sqrt of the row count by the caller).</li>
 *   <li><b>Overflow mode</b> (available &lt; sum of minimums): the available space is split
 *       proportionally to the minimums so every panel stays visible.</li>
 * </ul>
 *
 * <p>This class is intentionally free of any JavaFX dependency so it can be unit-tested
 * without a running toolkit.</p>
 */
public final class DividerPositionCalculator
{
    private DividerPositionCalculator()
    {
        // utility class
    }

    /**
     * Calculates divider positions for a vertical (or horizontal) SplitPane.
     *
     * @param weights   per-panel sizing weight (e.g. sqrt-scaled preferred height); must have
     *                  the same length as {@code minSizes}
     * @param minSizes  per-panel minimum pixel size; each panel is guaranteed at least this much
     *                  space (as long as the available space allows)
     * @param available total available pixel size of the SplitPane content area
     * @return array of {@code weights.length - 1} divider positions in the range [0, 1],
     *         or an empty array when there are fewer than 2 panels
     */
    public static double[] calculate(double[] weights, double[] minSizes, double available)
    {
        final int n = weights.length;
        if (n < 2)
        {
            return new double[0];
        }
        if (available <= 0)
        {
            // Size not yet known — fall back to equal distribution
            final double[] positions = new double[n - 1];
            for (int i = 0; i < positions.length; i++)
            {
                positions[i] = (double) (i + 1) / n;
            }
            return positions;
        }

        final double minTotal = Arrays.stream(minSizes).sum();
        final double[] allocated = new double[n];

        if (available >= minTotal)
        {
            // Normal mode: guarantee each panel its minimum, then distribute extra by weight
            final double extra = available - minTotal;
            final double weightTotal = Arrays.stream(weights).sum();
            for (int i = 0; i < n; i++)
            {
                final double weightShare = weightTotal > 0 ? weights[i] / weightTotal : 1.0 / n;
                allocated[i] = minSizes[i] + extra * weightShare;
            }
        }
        else
        {
            // Overflow mode: not enough space for all minimums — distribute proportionally to mins
            for (int i = 0; i < n; i++)
            {
                allocated[i] = minTotal > 0
                        ? available * (minSizes[i] / minTotal)
                        : available / n;
            }
        }

        // Convert absolute pixel sizes to cumulative divider positions in [0, 1]
        final double[] positions = new double[n - 1];
        double cumulative = 0;
        for (int i = 0; i < positions.length; i++)
        {
            cumulative += allocated[i];
            positions[i] = cumulative / available;
        }
        return positions;
    }
}
