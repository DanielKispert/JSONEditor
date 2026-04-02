package com.daniel.jsoneditor.view;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Verifies that a deferred-init listener based on width/height dimensions
 * fires correctly regardless of which dimension becomes positive first.
 */
class DeferredInitListenerTest
{
    @Test
    void shouldFireInitWhenWidthArrivesBeforeHeight()
    {
        final SimpleDoubleProperty width = new SimpleDoubleProperty(0);
        final SimpleDoubleProperty height = new SimpleDoubleProperty(0);
        final boolean[] initCalled = {false};

        addDimensionListener(width, height, initCalled);

        width.set(100);
        height.set(100);
        assertTrue(initCalled[0], "init must fire once both width and height are > 0");
    }

    @Test
    void shouldFireInitWhenHeightArrivesBeforeWidth()
    {
        final SimpleDoubleProperty width = new SimpleDoubleProperty(0);
        final SimpleDoubleProperty height = new SimpleDoubleProperty(0);
        final boolean[] initCalled = {false};

        addDimensionListener(width, height, initCalled);

        height.set(100);
        width.set(100);
        assertTrue(initCalled[0], "init must fire once both width and height are > 0");
    }

    @Test
    void shouldNotFireInitWhileOnlyOneDimensionIsPositive()
    {
        final SimpleDoubleProperty width = new SimpleDoubleProperty(0);
        final SimpleDoubleProperty height = new SimpleDoubleProperty(0);
        final boolean[] initCalled = {false};

        addDimensionListener(width, height, initCalled);

        width.set(100);
        assertFalse(initCalled[0], "init must not fire when only width is > 0");
    }

    @Test
    void shouldFireExactlyOnce()
    {
        final SimpleDoubleProperty width = new SimpleDoubleProperty(0);
        final SimpleDoubleProperty height = new SimpleDoubleProperty(0);
        final int[] callCount = {0};

        final ChangeListener<Number> listener = new ChangeListener<>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> obs, Number oldVal, Number newVal)
            {
                if (width.get() > 0 && height.get() > 0)
                {
                    width.removeListener(this);
                    height.removeListener(this);
                    callCount[0]++;
                }
            }
        };
        width.addListener(listener);
        height.addListener(listener);

        width.set(50);
        height.set(50);
        width.set(200);
        height.set(200);

        assertEquals(1, callCount[0], "Listener must fire exactly once and then remove itself");
    }

    /**
     * Adds a listener on BOTH width and height that fires when both are > 0.
     * This is the correct pattern — listening on only one property misses the case
     * where that property fires first while the other is still 0.
     */
    private void addDimensionListener(SimpleDoubleProperty width, SimpleDoubleProperty height, boolean[] initCalled)
    {
        final ChangeListener<Number> listener = new ChangeListener<>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> obs, Number oldVal, Number newVal)
            {
                if (width.get() > 0 && height.get() > 0)
                {
                    width.removeListener(this);
                    height.removeListener(this);
                    initCalled[0] = true;
                }
            }
        };
        width.addListener(listener);
        height.addListener(listener);
    }
}

