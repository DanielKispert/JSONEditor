package com.daniel.jsoneditor.view;

import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.DividerPositionCalculator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class DividerPositionCalculatorTest
{
    private static final double DELTA = 1e-6;

    // ── Weight-based calculate() ─────────────────────────────────────────────

    /**
     * Normal operation of the weight-based {@code calculate()} method:
     * equal weights, unequal weights, three panels, minimum enforcement, and zero-weight panels.
     */
    @Test
    void weightBasedDistribution()
    {
        // Equal weights → 50/50
        double[] pos = DividerPositionCalculator.calculate(
                new double[]{100, 100}, new double[]{50, 50}, 300);
        assertEquals(0.5, pos[0], DELTA, "Equal weights should split at 50%");

        // Unequal weights → larger weight gets more extra space
        pos = DividerPositionCalculator.calculate(
                new double[]{100, 300}, new double[]{50, 50}, 400);
        assertEquals(125.0 / 400.0, pos[0], DELTA, "Panel with weight 100 should get 125 of 400");

        // Three panels: weights 100, 400, 400
        pos = DividerPositionCalculator.calculate(
                new double[]{100, 400, 400}, new double[]{50, 50, 50}, 600);
        assertEquals(2, pos.length);
        assertEquals(100.0 / 600.0, pos[0], DELTA);
        assertEquals(350.0 / 600.0, pos[1], DELTA);

        // Small weight with large minimum → minimum is guaranteed
        pos = DividerPositionCalculator.calculate(
                new double[]{10, 990}, new double[]{150, 50}, 300);
        final double panelA = 300 * pos[0];
        assertTrue(panelA >= 150, "Panel with min=150 should get at least 150, got %.1f".formatted(panelA));

        // Zero weight → gets exactly its minimum, others share the extra
        pos = DividerPositionCalculator.calculate(
                new double[]{0, 10, 20}, new double[]{100, 100, 100}, 500);
        assertEquals(100.0 / 500.0, pos[0], DELTA, "Zero-weight panel should get exactly its min");
    }

    /**
     * Edge cases for {@code calculate()}: overflow, single panel, zero available space.
     */
    @Test
    void weightBasedEdgeCases()
    {
        // Single panel → no dividers
        assertEquals(0, DividerPositionCalculator.calculate(
                new double[]{100}, new double[]{50}, 300).length);

        // Zero available → returns array without exception
        double[] pos = DividerPositionCalculator.calculate(
                new double[]{100, 100}, new double[]{50, 50}, 0);
        assertEquals(1, pos.length, "Should return one divider position even with zero space");

        // Overflow: available < sum(min) → proportional to minimums
        pos = DividerPositionCalculator.calculate(
                new double[]{100, 100}, new double[]{100, 100}, 100);
        assertEquals(0.5, pos[0], DELTA, "Overflow should distribute proportionally to mins");
    }

    // ── Preferred/minimum-based calculateFromPreferredAndMinimumSizes() ──────

    /**
     * Enough-space scenarios: all panels get at least their preferred size,
     * leftover is distributed proportionally.
     */
    @Test
    void prefBasedEnoughSpace()
    {
        // Two similar tables (5 rows + 3 rows), plenty of space → both get ≥ pref
        double[] pos = DividerPositionCalculator.calculateFromPreferredAndMinimumSizes(
                new double[]{280, 200}, new double[]{160, 160}, 800);
        assertTrue(800 * pos[0] >= 280, "5-row table should get ≥ its pref (280)");
        assertTrue(800 * (1.0 - pos[0]) >= 200, "3-row table should get ≥ its pref (200)");

        // Two equal tables (10 rows each) → even split
        pos = DividerPositionCalculator.calculateFromPreferredAndMinimumSizes(
                new double[]{480, 480}, new double[]{160, 160}, 800);
        assertEquals(0.5, pos[0], 0.01, "Equal tables should split at 50%");

        // Boundary: available == sum(pref) exactly → each gets exactly its pref
        pos = DividerPositionCalculator.calculateFromPreferredAndMinimumSizes(
                new double[]{300, 200}, new double[]{100, 100}, 500);
        assertEquals(300.0 / 500.0, pos[0], DELTA, "Exact fit: each panel gets its pref");
    }

    /**
     * Scarce-space scenarios: panels are compressed proportionally, but minimums are respected.
     */
    @Test
    void prefBasedScarceSpace()
    {
        // Main table (20 rows) + small child (2 rows): child gets close to its pref, not excessive
        double[] pos = DividerPositionCalculator.calculateFromPreferredAndMinimumSizes(
                new double[]{880, 160}, new double[]{160, 120}, 800);
        final double childSize = 800 * (1.0 - pos[0]);
        assertTrue(childSize >= 120, "2-row child should get ≥ its min (120), got %.1f".formatted(childSize));
        assertTrue(childSize <= 240, "2-row child should not get excessive space, got %.1f".formatted(childSize));

        // Main table (20 rows) + empty child (0 rows): empty child still visible
        pos = DividerPositionCalculator.calculateFromPreferredAndMinimumSizes(
                new double[]{880, 80}, new double[]{160, 40}, 800);
        final double emptySize = 800 * (1.0 - pos[0]);
        assertTrue(emptySize >= 40, "Empty child should get ≥ its min (40), got %.1f".formatted(emptySize));

        // Three panels: large table gets the most space, all respect minimums
        pos = DividerPositionCalculator.calculateFromPreferredAndMinimumSizes(
                new double[]{880, 280, 160}, new double[]{160, 160, 120}, 800);
        final double mainSize = 800 * pos[0];
        final double child1Size = 800 * (pos[1] - pos[0]);
        final double child2Size = 800 * (1.0 - pos[1]);
        assertTrue(mainSize > child1Size && mainSize > child2Size,
                "Main table should get largest share: %.0f vs %.0f vs %.0f".formatted(mainSize, child1Size, child2Size));
        assertTrue(child1Size >= 160 && child2Size >= 120, "All children should get ≥ their minimums");
    }

    /**
     * Edge cases for {@code calculateFromPreferredAndMinimumSizes}:
     * single panel, zero available, overflow, and incompressible panels.
     */
    @Test
    void prefBasedEdgeCases()
    {
        // Single panel → no dividers
        assertEquals(0, DividerPositionCalculator.calculateFromPreferredAndMinimumSizes(
                new double[]{200}, new double[]{100}, 500).length);

        // Zero available → equal fallback
        double[] pos = DividerPositionCalculator.calculateFromPreferredAndMinimumSizes(
                new double[]{200, 100}, new double[]{80, 40}, 0);
        assertEquals(0.5, pos[0], DELTA, "Zero available should fall back to equal split");

        // Overflow: available < sum(min) → proportional to minimums
        pos = DividerPositionCalculator.calculateFromPreferredAndMinimumSizes(
                new double[]{500, 300}, new double[]{200, 100}, 150);
        assertEquals(100.0 / 150.0, pos[0], DELTA, "Overflow: proportional to minimums");

        // Incompressible (pref == min): compress equally as last resort
        pos = DividerPositionCalculator.calculateFromPreferredAndMinimumSizes(
                new double[]{200, 200}, new double[]{200, 200}, 300);
        assertEquals(0.5, pos[0], DELTA, "Incompressible panels should compress equally");
    }

    // ── Horizontal: editor windows with capped preferred widths ──────────────

    /**
     * Capping preferred widths to a bounded range produces a much fairer horizontal
     * distribution than uncapped content-based widths.
     */
    @Test
    void horizontalCappedPrefsProduceFairDistribution()
    {
        final double available = 1400;
        final double[] mins = {250, 250};

        // Uncapped: left window dominates
        final double leftUncapped = available * DividerPositionCalculator.calculateFromPreferredAndMinimumSizes(
                new double[]{1200, 300}, mins, available)[0];

        // Capped to [250, 500]: much fairer
        final double leftCapped = available * DividerPositionCalculator.calculateFromPreferredAndMinimumSizes(
                new double[]{500, 300}, mins, available)[0];

        assertTrue(leftUncapped > 900, "Uncapped: left should get > 900px, got %.1f".formatted(leftUncapped));
        assertTrue(leftCapped < 850, "Capped: left should get < 850px, got %.1f".formatted(leftCapped));
        assertTrue(leftCapped > 650, "Capped: left should still get more than right, got %.1f".formatted(leftCapped));
    }
}
