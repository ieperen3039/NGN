package NG.GUIMenu.Components;

import org.joml.Vector2ic;

import NG.GUIMenu.LayoutManagers.SingleElementLayout;
import NG.GUIMenu.Rendering.SFrameLookAndFeel;

/**
 * Adds a margin around the given component.
 * Behaves similar to the following snippet:
 * <pre>
 * SContainer.row(
 *   new SFiller(left, 0),
 *   new SContainer.column(
 *     new SFiller(0, top),
 *     target,
 *     new SFiller(0, bottom)
 *   ),
 *   new SFiller(right, 0)
 * )
 * </pre>
 */
public class SMargin extends SContainer {
    public SMargin(int marginPixels, SComponent target) {
        super(new SingleElementLayout(), new ComponentBorder(marginPixels));
        add(target, null);
    }

    public SMargin(int xMargin, int yMargin, SComponent target) {
        super(new SingleElementLayout(), new ComponentBorder(xMargin, xMargin, yMargin, yMargin));
        add(target, null);
    }

    public SMargin(int left, int right, int top, int bottom, SComponent target) {
        super(new SingleElementLayout(), new ComponentBorder(left, right, top, bottom));
        add(target, null);
    }

    @Override
    public void draw(SFrameLookAndFeel design, Vector2ic screenPosition) {
        drawChildren(design, screenPosition);
    }

    @Override
    public SComponent getComponentAt(int xRel, int yRel) {
        SComponent found = super.getComponentAt(xRel, yRel);
        if (found == this) return null;
        return found;
    }
}
