package io.github.ieperen3039.ngn.UserInterface.LayoutManagers;

import org.joml.Vector2ic;

import io.github.ieperen3039.ngn.UserInterface.Components.SComponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Places components in a single row or column, in the order in which they were added. The property class is Void
 * (always null). Along the main axis, components get their minimum size plus a share of the spare space; along the
 * other axis every component gets the full size of the layout.
 * @see GridLayoutManager
 */
public class ListLayoutManager implements SLayoutManager {
    private final boolean horizontal;
    private final List<SComponent> elements = new ArrayList<>();

    /** minimum sizes along the main axis, one per element */
    private int[] minSizes = new int[0];
    private boolean[] wantGrow = new boolean[0];
    private int nOfGrows = 0;

    private int minCrossSize = 0;
    private boolean crossWantGrow = false;

    /**
     * @param horizontal if true, components are placed in a row from left to right. If false, components are placed in
     *                   a column from top to bottom.
     */
    public ListLayoutManager(boolean horizontal) {
        this.horizontal = horizontal;
    }

    /**
     * appends a component to the end of the list
     * @param comp the component to be added
     * @param prop ignored, should be null
     */
    @Override
    public void add(SComponent comp) {
        assert comp != null;
        elements.add(comp);
    }

    @Override
    public void remove(SComponent comp) {
        elements.remove(comp);
    }

    @Override
    public void recalculateProperties() {
        int n = elements.size();
        minSizes = new int[n];
        wantGrow = new boolean[n];
        nOfGrows = 0;
        minCrossSize = 0;
        crossWantGrow = false;

        for (int i = 0; i < n; i++) {
            SComponent elt = elements.get(i);
            if (!elt.isVisible()) continue;

            int minMain = horizontal ? elt.minWidth() : elt.minHeight();
            int minCross = horizontal ? elt.minHeight() : elt.minWidth();
            boolean growMain = horizontal ? elt.wantHorizontalGrow() : elt.wantVerticalGrow();
            boolean growCross = horizontal ? elt.wantVerticalGrow() : elt.wantHorizontalGrow();

            minSizes[i] = minMain;
            minCrossSize = Math.max(minCrossSize, minCross);

            if (growMain) {
                wantGrow[i] = true;
                nOfGrows++;
            }
            if (growCross) {
                crossWantGrow = true;
            }
        }
    }

    @Override
    public void placeComponents(Vector2ic position, Vector2ic dimensions) {
        if (elements.isEmpty()) return;

        int mainSize = horizontal ? dimensions.x() : dimensions.y();
        int crossSize = horizontal ? dimensions.y() : dimensions.x();
        int[] sizes = GridLayoutManager.calculateDimensionSizes(minSizes, nOfGrows, wantGrow, mainSize);

        int mainPos = horizontal ? position.x() : position.y();
        for (int i = 0; i < sizes.length; i++) {
            SComponent elt = elements.get(i);

            if (horizontal) {
                elt.setPosition(mainPos, position.y());
                elt.setSize(sizes[i], crossSize);
            } else {
                elt.setPosition(position.x(), mainPos);
                elt.setSize(crossSize, sizes[i]);
            }

            mainPos += sizes[i];
        }
    }

    @Override
    public Collection<SComponent> getComponents() {
        return Collections.unmodifiableList(elements);
    }

    @Override
    public int getMinimumWidth() {
        return horizontal ? sum(minSizes) : minCrossSize;
    }

    @Override
    public int getMinimumHeight() {
        return horizontal ? minCrossSize : sum(minSizes);
    }

    @Override
    public void clear() {
        elements.clear();
    }

    @Override
    public boolean wantHorizontalGrow() {
        return horizontal ? nOfGrows > 0 : crossWantGrow;
    }

    @Override
    public boolean wantVerticalGrow() {
        return horizontal ? crossWantGrow : nOfGrows > 0;
    }

    private static int sum(int[] values) {
        int total = 0;
        for (int v : values) {
            total += v;
        }
        return total;
    }
}
