package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components;

import java.util.Arrays;


/**
 * Pure-logic calculator for SplitPane divider positions.
 *
 * <p>The primary entry point is
 * {@link #calculateFromPreferredAndMinimumSizes(double[], double[], double)} which uses a
 * "fill to preferred size first" strategy with three modes: enough space (each panel gets
 * its preferred size), scarce space (panels are compressed proportionally), and overflow
 * (space is distributed proportionally to minimums).</p>
 *
 * <p>A lower-level {@link #calculate(double[], double[], double)} method is also available
 * for callers that supply their own pre-computed weights.</p>
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
     * Allocates space using a "fill to preferred size first" strategy.
     *
     * <ol>
     *   <li><b>Enough space</b> ({@code available &ge; sum(pref)}): each panel receives its
     *       preferred size.  Leftover space is distributed proportionally to {@code sqrt(pref)}
     *       so that larger panels benefit more from extra room.</li>
     *   <li><b>Scarce space</b> ({@code sum(min) &le; available &lt; sum(pref)}): each panel
     *       starts at its preferred size and is proportionally compressed based on how much room
     *       it has to shrink ({@code pref &minus; min}).  Panels that are already at their
     *       minimum are not compressed further.</li>
     *   <li><b>Overflow</b> ({@code available &lt; sum(min)}): space is distributed
     *       proportionally to minimum sizes so every panel stays visible.</li>
     * </ol>
     *
     * @param preferredSizes per-panel preferred pixel size
     * @param minimumSizes   per-panel minimum pixel size
     * @param available      total available pixel size
     * @return divider positions in [0, 1], or an empty array for fewer than 2 panels
     */
    public static double[] calculateFromPreferredAndMinimumSizes(double[] preferredSizes,
                                                                  double[] minimumSizes,
                                                                  double available)
    {
        final int n = preferredSizes.length;
        if (n < 2)
        {
            return new double[0];
        }
        if (available <= 0)
        {
            return equalPositions(n);
        }

        final double prefTotal = Arrays.stream(preferredSizes).sum();
        final double minTotal = Arrays.stream(minimumSizes).sum();
        final double[] allocated = new double[n];

        if (available >= prefTotal)
        {
            // Enough space: give each panel its preferred size, distribute leftover
            final double leftover = available - prefTotal;
            double weightTotal = 0;
            final double[] weights = new double[n];
            for (int i = 0; i < n; i++)
            {
                weights[i] = Math.sqrt(Math.max(preferredSizes[i], 0));
                weightTotal += weights[i];
            }
            for (int i = 0; i < n; i++)
            {
                final double share = weightTotal > 0 ? weights[i] / weightTotal : 1.0 / n;
                allocated[i] = preferredSizes[i] + leftover * share;
            }
        }
        else if (available >= minTotal)
        {
            // Scarce space: compress panels proportionally to their compressibility
            final double excess = prefTotal - available;
            double totalRoom = 0;
            final double[] room = new double[n];
            for (int i = 0; i < n; i++)
            {
                room[i] = Math.max(preferredSizes[i] - minimumSizes[i], 0);
                totalRoom += room[i];
            }
            for (int i = 0; i < n; i++)
            {
                final double reduction = totalRoom > 0
                        ? excess * room[i] / totalRoom
                        : excess / n;
                allocated[i] = preferredSizes[i] - reduction;
            }
        }
        else
        {
            allocateOverflow(allocated, minimumSizes, available, minTotal);
        }

        return toPositions(allocated, available);
    }

    /**
     * Lower-level method that calculates divider positions from pre-computed weights.
     *
     * <p>For most use cases, prefer
     * {@link #calculateFromPreferredAndMinimumSizes(double[], double[], double)} which
     * derives weights automatically from preferred and minimum sizes.</p>
     *
     * @param weights   per-panel sizing weight; must have the same length as {@code minSizes}
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
            return equalPositions(n);
        }

        final double minTotal = Arrays.stream(minSizes).sum();
        final double[] allocated = new double[n];

        if (available >= minTotal)
        {
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
            allocateOverflow(allocated, minSizes, available, minTotal);
        }

        return toPositions(allocated, available);
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    /**
     * Returns equal divider positions (1/n, 2/n, …) used as fallback when the available
     * size is not yet known.
     *
     * @param panelCount number of panels; must be &ge; 2 for meaningful results
     * @return array of {@code panelCount - 1} divider positions, or an empty array for fewer
     *         than 2 panels
     */
    private static double[] equalPositions(int panelCount)
    {
        if (panelCount < 2)
        {
            return new double[0];
        }
        final double[] positions = new double[panelCount - 1];
        for (int i = 0; i < positions.length; i++)
        {
            positions[i] = (double) (i + 1) / panelCount;
        }
        return positions;
    }

    /**
     * Distributes available space proportionally to the given sizes (overflow mode).
     */
    private static void allocateOverflow(double[] allocated, double[] sizes, double available,
                                         double sizeTotal)
    {
        final int n = allocated.length;
        for (int i = 0; i < n; i++)
        {
            allocated[i] = sizeTotal > 0
                    ? available * (sizes[i] / sizeTotal)
                    : available / n;
        }
    }

    /**
     * Converts absolute pixel allocations to cumulative divider positions in [0, 1].
     */
    private static double[] toPositions(double[] allocated, double available)
    {
        final double[] positions = new double[allocated.length - 1];
        double cumulative = 0;
        for (int i = 0; i < positions.length; i++)
        {
            cumulative += allocated[i];
            positions[i] = cumulative / available;
        }
        return positions;
    }
}
