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
}
