package io.github.ieperen3039.ngn.UserInterface.Components;

import org.joml.Vector2i;
import org.joml.Vector2ic;

import io.github.ieperen3039.ngn.UserInterface.LayoutManagers.GridLayoutManager;
import io.github.ieperen3039.ngn.UserInterface.Rendering.SFrameLookAndFeel;

import static io.github.ieperen3039.ngn.UserInterface.Rendering.SFrameLookAndFeel.UIComponentType.PANEL;
import static io.github.ieperen3039.ngn.UserInterface.Rendering.SFrameLookAndFeel.UIState.ENABLED;

public class SNineWayPanel extends SContainer {
    public static final Vector2ic MIDDLE = new Vector2i(1, 1);
    public static final Vector2ic NORTH = new Vector2i(1, 0);
    public static final Vector2ic EAST = new Vector2i(2, 1);
    public static final Vector2ic SOUTH = new Vector2i(1, 2);
    public static final Vector2ic WEST = new Vector2i(0, 1);
    public static final Vector2ic NORTHEAST = new Vector2i(2, 0);
    public static final Vector2ic SOUTHEAST = new Vector2i(2, 2);
    public static final Vector2ic NORTHWEST = new Vector2i(0, 0);
    public static final Vector2ic SOUTHWEST = new Vector2i(0, 2);

    /** the same instance that the superclass uses, but with its concrete type known */
    private final GridLayoutManager grid;

    /**
     * creates a panel that uses a default GridLayout of 3x3 and with minimum size of (0, 0) and a no-growth policy.
     */
    public SNineWayPanel() {
        this(new GridLayoutManager(3, 3));
    }

    // a super(...) call cannot store its argument in a local, so we route it through a private constructor
    private SNineWayPanel(GridLayoutManager grid) {
        super(grid);
        this.grid = grid;
    }

    /**
     * Add an object to the panel, using one of {@link #NORTH}, {@link #EAST}, {@link #SOUTH}, {@link #WEST}, {@link
     * #NORTHEAST}, {@link #SOUTHEAST}, {@link #NORTHWEST}, {@link #SOUTHWEST}, {@link #MIDDLE}
     */
    public void add(SComponent comp, Vector2ic where) {
        grid.add(comp, where.x(), where.y());
        comp.setParent(this);
        invalidateLayout();
    }

    @Override
    public void add(SComponent comp) {
        throw new UnsupportedOperationException("SNineWayPanel.add requires a position");
    }

    @Override
    public void draw(SFrameLookAndFeel design, Vector2ic screenPosition) {
        assert getWidth() > 0 && getHeight() > 0 :
                String.format("Non-positive dimensions of %s: width = %d, height = %d", this, getWidth(), getHeight());

        design.draw(PANEL, ENABLED, screenPosition, getSize());
        drawChildren(design, screenPosition);
    }
}
