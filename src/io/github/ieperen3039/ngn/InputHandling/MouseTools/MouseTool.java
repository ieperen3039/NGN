package io.github.ieperen3039.ngn.InputHandling.MouseTools;

/**
 * Mouse behaviour. Events arrive already resolved to a {@link MouseTarget}; all methods default to "not handled",
 * so tools override only what they need. Unhandled events fall through to the default tool (UI and camera).
 *
 * <p>Example, a station build tool: the game adds a selector layer returning a MapTarget. onHover moves a ghost and
 * returns false, so UI and camera keep working. onPress(LEFT, MapTarget) spawns a preview and returns true, onDrag
 * rotates it, onRelease places it and calls setMouseTool(null). A right press during the gesture cancels. onPress on
 * a {@link UITarget} returns false, so toolbar clicks still work.
 *
 * <p>Example, a UI reorder tool: only reacts to a UITarget whose component is reorderable. onDrag moves the item,
 * onRelease(target) drops it onto the slot under the cursor. Everything else falls through to the camera.
 * @author Geert van Ieperen created on 24-4-2020.
 */
public interface MouseTool {
    default void onActivate() {
    }

    default void onDeactivate() {
    }

    /**
     * mouse moved without a gesture in progress
     * @return true to stop fall-through
     */
    default boolean onHover(MouseTarget target, int xDelta, int yDelta, float xSc, float ySc) {
        return false;
    }

    /**
     * @return true to take ownership of this gesture (receives onDrag/onRelease until all buttons are up)
     */
    default boolean onPress(int button, MouseTarget target, int xSc, int ySc) {
        return false;
    }

    default void onDrag(int xDelta, int yDelta, float xSc, float ySc) {
    }

    /**
     * @param target what is under the cursor at release (useful for drop targets)
     */
    default void onRelease(int button, MouseTarget target, int xSc, int ySc) {
    }

    default boolean onScroll(float value, MouseTarget target, int xSc, int ySc) {
        return false;
    }
}
