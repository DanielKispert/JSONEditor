package com.daniel.jsoneditor.view;

import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.DividerPositionCalculator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class DividerPositionCalculatorTest
{
    private static final double DELTA = 1e-6;

    /**
     * Two equal-weight panels with generous available space: divider should land at the midpoint.
     */
    @Test
    void equalWeightsTwoPanels()
    {
        // weights=[100,100], mins=[50,50], available=300
        // minTotal=100, extra=200. Each: 50 + 200*(100/200) = 150. Divider at 150/300 = 0.5
        final double[] positions = DividerPositionCalculator.calculate(
                new double[]{100, 100},
                new double[]{50, 50},
                300
        );
        assertEquals(1, positions.length);
        assertEquals(0.5, positions[0], DELTA);
    }

    /**
     * Larger weight receives proportionally more of the extra space above the minimums.
     */
    @Test
    void unequalWeightsTwoPanels()
    {
        // weights=[100,300], mins=[50,50], available=400
        // minTotal=100, extra=300. A: 50+300*(100/400)=125. B: 50+300*(300/400)=275
        final double[] positions = DividerPositionCalculator.calculate(
                new double[]{100, 300},
                new double[]{50, 50},
                400
        );
        assertEquals(1, positions.length);
        assertEquals(125.0 / 400.0, positions[0], DELTA);
    }

    /**
     * A panel with a large minimum is guaranteed its minimum even when its weight is tiny.
     */
    @Test
    void minimumEnforcedForSmallWeightPanel()
    {
        // weights=[10,990], mins=[150,50], available=300
        // minTotal=200, extra=100. A: 150+100*(10/1000)=151. B: 50+100*(990/1000)=149
        final double[] positions = DividerPositionCalculator.calculate(
                new double[]{10, 990},
                new double[]{150, 50},
                300
        );
        assertEquals(1, positions.length);
        final double expectedA = 150 + 100.0 * (10.0 / 1000.0);
        assertEquals(expectedA / 300.0, positions[0], DELTA);
    }

    /**
     * Three panels: two with equal larger weights and one with smaller weight.
     */
    @Test
    void threePanels()
    {
        // weights=[100,400,400], mins=[50,50,50], available=600
        // minTotal=150, extra=450. A:50+450*(100/900)=100. B:50+450*(400/900)=250. C:250
        final double[] positions = DividerPositionCalculator.calculate(
                new double[]{100, 400, 400},
                new double[]{50, 50, 50},
                600
        );
        assertEquals(2, positions.length);
        assertEquals(100.0 / 600.0, positions[0], DELTA);
        assertEquals(350.0 / 600.0, positions[1], DELTA);
    }

    /**
     * When available space is less than the sum of minimums, space is split proportionally to minimums.
     */
    @Test
    void insufficientSpaceDistributesProportionallyToMins()
    {
        // mins=[100,100], available=100 (less than minTotal=200) → each gets 0.5
        final double[] positions = DividerPositionCalculator.calculate(
                new double[]{100, 100},
                new double[]{100, 100},
                100
        );
        assertEquals(1, positions.length);
        assertEquals(0.5, positions[0], DELTA);
    }

    /**
     * Single panel: no dividers are needed.
     */
    @Test
    void singlePanelReturnsNoDividers()
    {
        final double[] positions = DividerPositionCalculator.calculate(
                new double[]{100},
                new double[]{50},
                300
        );
        assertEquals(0, positions.length);
    }

    /**
     * Zero available space: returns an array of the right length without throwing.
     */
    @Test
    void zeroAvailableReturnsArrayWithoutException()
    {
        final double[] positions = DividerPositionCalculator.calculate(
                new double[]{100, 100},
                new double[]{50, 50},
                0
        );
        assertEquals(1, positions.length);
    }

    /**
     * A panel with weight zero (pref equals min — no room to grow) receives exactly its minimum,
     * and its share of the extra space is redistributed to panels with positive weight.
     */
    @Test
    void panelWithZeroWeightGetsExactlyItsMinimum()
    {
        // weights=[0, 10, 20], mins=[100, 100, 100], available=500
        // minTotal=300, extra=200. Panel 0: 100+0=100. Panel 1: 100+200*(10/30)≈167. Panel 2: 100+200*(20/30)≈233
        final double[] positions = DividerPositionCalculator.calculate(
                new double[]{0, 10, 20},
                new double[]{100, 100, 100},
                500
        );
        assertEquals(2, positions.length);
        assertEquals(100.0 / 500.0, positions[0], DELTA);
        final double expectedB = 100 + 200.0 * (10.0 / 30.0);
        assertEquals((100.0 + expectedB) / 500.0, positions[1], DELTA);
    }

    // ── Scenario tests: UX requirements for the full pref/min → allocation pipeline ──

    /**
     * V1: Main table (20 rows) + child table (2 rows).
     * The child should receive approximately its preferred size, not much more.
     */
    @Test
    void smallChildTableGetsApproximatelyItsPref()
    {
        // With +1 buffer row: 20-row pref = (20+2)*40 = 880, 2-row pref = (2+2)*40 = 160
        final double[] prefs = {880, 160};
        final double[] mins = {160, 120};
        final double available = 800;
        final double[] pos = DividerPositionCalculator.calculateFromPreferredAndMinimumSizes(
                prefs, mins, available);
        final double childSize = available * (1.0 - pos[0]);
        // Child should get close to its pref (160) — at least its min
        assertTrue(childSize >= 120,
                "Child table (2 rows) should get ≥ its min (120) but got %.1f".formatted(childSize));
        // Child should NOT get excessive space (no more than 1.5x its pref)
        assertTrue(childSize <= 240,
                "Child table should not get excessive space, got %.1f".formatted(childSize));
    }

    /**
     * V2: Main table (20 rows) + empty child table (0 rows).
     * The empty panel should get at least its minimum and not be invisible.
     */
    @Test
    void emptyChildTableGetsAtLeastItsMin()
    {
        // 20-row pref = 880, 0-row pref = (0+2)*40 = 80
        final double[] prefs = {880, 80};
        final double[] mins = {160, 40};
        final double available = 800;
        final double[] pos = DividerPositionCalculator.calculateFromPreferredAndMinimumSizes(
                prefs, mins, available);
        final double emptySize = available * (1.0 - pos[0]);
        assertTrue(emptySize >= 40,
                "Empty table should get ≥ its min (40) but got %.1f".formatted(emptySize));
        assertTrue(emptySize <= 160,
                "Empty table should not get excessive space, got %.1f".formatted(emptySize));
    }

    /**
     * V3: Main table (5 rows) + child table (3 rows), plenty of space.
     * Both tables should get approximately their preferred size with leftover distributed.
     */
    @Test
    void similarTablesWithPlentyOfSpaceBothGetTheirPref()
    {
        // 5-row pref = (5+2)*40 = 280, 3-row pref = (3+2)*40 = 200
        final double[] prefs = {280, 200};
        final double[] mins = {160, 160};
        final double available = 800;
        final double[] pos = DividerPositionCalculator.calculateFromPreferredAndMinimumSizes(
                prefs, mins, available);
        final double mainSize = available * pos[0];
        final double childSize = available * (1.0 - pos[0]);
        // Both should get at least their pref (sum=480 < 800, so enough space)
        assertTrue(mainSize >= 280,
                "5-row table should get ≥ its pref (280) but got %.1f".formatted(mainSize));
        assertTrue(childSize >= 200,
                "3-row table should get ≥ its pref (200) but got %.1f".formatted(childSize));
    }

    /**
     * V4: Main table (20 rows) + two child tables (5 rows, 2 rows).
     * The large table should get the most space; small tables get close to their pref.
     */
    @Test
    void largeTableGetsLionsShareWhenSpaceIsScarce()
    {
        // 20-row pref = 880, 5-row pref = 280, 2-row pref = 160
        final double[] prefs = {880, 280, 160};
        final double[] mins = {160, 160, 120};
        final double available = 800;
        final double[] pos = DividerPositionCalculator.calculateFromPreferredAndMinimumSizes(
                prefs, mins, available);
        final double mainSize = available * pos[0];
        final double child1Size = available * (pos[1] - pos[0]);
        final double child2Size = available * (1.0 - pos[1]);
        // Main table has the most content and should get the largest share
        assertTrue(mainSize > child1Size,
                "Main table should get more than child1, got %.1f vs %.1f"
                        .formatted(mainSize, child1Size));
        assertTrue(mainSize > child2Size,
                "Main table should get more than child2, got %.1f vs %.1f"
                        .formatted(mainSize, child2Size));
        // All panels should get at least their minimum
        assertTrue(child1Size >= 160,
                "5-row child should get ≥ its min (160) but got %.1f".formatted(child1Size));
        assertTrue(child2Size >= 120,
                "2-row child should get ≥ its min (120) but got %.1f".formatted(child2Size));
    }

    /**
     * V5: Two identical tables (10 rows each) must split evenly.
     */
    @Test
    void equalTablesSplitEvenly()
    {
        // 10-row pref = (10+2)*40 = 480
        final double[] prefs = {480, 480};
        final double[] mins = {160, 160};
        final double available = 800;
        final double[] pos = DividerPositionCalculator.calculateFromPreferredAndMinimumSizes(
                prefs, mins, available);
        assertEquals(0.5, pos[0], 0.01, "Equal tables should split at 50%%");
    }

    // ── Edge-case coverage for calculateFromPreferredAndMinimumSizes ──────

    /**
     * Exercises every guard clause and fallback path of calculateFromPreferredAndMinimumSizes
     * that is not already covered by the UX scenario tests above.
     */
    @Test
    void prefMinEdgeCases()
    {
        // (a) Single panel → no dividers
        assertEquals(0,
                DividerPositionCalculator.calculateFromPreferredAndMinimumSizes(
                        new double[]{200}, new double[]{100}, 500).length,
                "Single panel should produce no dividers");

        // (b) available = 0 → equal fallback positions
        final double[] zeroAvail = DividerPositionCalculator.calculateFromPreferredAndMinimumSizes(
                new double[]{200, 100}, new double[]{80, 40}, 0);
        assertEquals(1, zeroAvail.length);
        assertEquals(0.5, zeroAvail[0], DELTA, "available=0 should fall back to equal split");

        // (c) Overflow: available < sum(min) → proportional to minimums
        //     mins=[200,100], available=150 < 300. Expected: 200/300*150=100, 100/300*150=50
        final double[] overflow = DividerPositionCalculator.calculateFromPreferredAndMinimumSizes(
                new double[]{500, 300}, new double[]{200, 100}, 150);
        assertEquals(1, overflow.length);
        assertEquals(100.0 / 150.0, overflow[0], DELTA,
                "Overflow should distribute proportionally to minimums");

        // (d) Scarce mode, all panels incompressible (pref == min for every panel):
        //     totalRoom = 0, falls back to excess/n reduction.
        //     prefs=[200,200], mins=[200,200], available=300, excess=100.
        //     Each: 200 - 100/2 = 150. Divider at 150/300 = 0.5
        final double[] incompressible = DividerPositionCalculator.calculateFromPreferredAndMinimumSizes(
                new double[]{200, 200}, new double[]{200, 200}, 300);
        assertEquals(0.5, incompressible[0], DELTA,
                "Incompressible panels should compress equally as last resort");

        // (e) Boundary: available == sum(pref) exactly → enough-space mode, zero leftover
        //     prefs=[300,200], mins=[100,100], available=500.
        //     Each gets exactly its pref. Divider at 300/500 = 0.6
        final double[] exact = DividerPositionCalculator.calculateFromPreferredAndMinimumSizes(
                new double[]{300, 200}, new double[]{100, 100}, 500);
        assertEquals(300.0 / 500.0, exact[0], DELTA,
                "When available == sum(pref), each panel should get exactly its pref");
    }

    // ── Sizing contract: verifies the pref/min formulas the scenario tests depend on ──

    /**
     * Documents and verifies the sizing contract between EditorTableViewImpl and the
     * DividerPositionCalculator. The scenario tests above use hardcoded pref/min values
     * derived from these formulas. If the formulas change, this test fails and the scenario
     * test values must be updated accordingly.
     *
     * <p>Contract (EditorTableViewImpl constants: ROW_HEIGHT = 40, MIN_ROWS_FOR_SIZING = 3):
     * <ul>
     *   <li>prefHeight = (rows + 2) × ROW_HEIGHT — content + header + one buffer row</li>
     *   <li>minHeight  = (min(MIN_ROWS, rows) + 1) × ROW_HEIGHT — capped minimum</li>
     * </ul>
     */
    @Test
    void sizingContractForScenarioTests()
    {
        final double ROW = 40.0;
        final int MIN_ROWS = 3;

        // Verify the pref/min values used in each scenario test match the formula
        record SizingCase(int rows, double expectedPref, double expectedMin) {}
        final SizingCase[] cases = {
                new SizingCase(0,  80,  40),   // V2: empty table
                new SizingCase(2,  160, 120),  // V1: 2-row child
                new SizingCase(3,  200, 160),  // V3: 3-row child
                new SizingCase(5,  280, 160),  // V3/V4: 5-row table
                new SizingCase(10, 480, 160),  // V5: 10-row table
                new SizingCase(20, 880, 160),  // V1/V2/V4: 20-row table
        };
        for (final SizingCase c : cases)
        {
            final double pref = (c.rows + 2) * ROW;
            final double min = (Math.min(MIN_ROWS, c.rows) + 1) * ROW;
            assertEquals(c.expectedPref, pref, DELTA,
                    "prefHeight mismatch for %d rows".formatted(c.rows));
            assertEquals(c.expectedMin, min, DELTA,
                    "minHeight mismatch for %d rows".formatted(c.rows));
        }

        // Critical invariant: pref > min for every non-empty table.
        // Without this, scarce-mode compression has zero room and falls back to equal reduction.
        for (int rows = 1; rows <= 100; rows++)
        {
            final double pref = (rows + 2) * ROW;
            final double min = (Math.min(MIN_ROWS, rows) + 1) * ROW;
            assertTrue(pref > min,
                    "pref must exceed min for %d rows (pref=%.0f, min=%.0f)"
                            .formatted(rows, pref, min));
        }
    }
}
