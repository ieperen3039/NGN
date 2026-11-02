package io.github.ieperen3039.ngn.InputHandling.MouseTools;

/**
 * Maps a screen position to the thing under the cursor
 * @author Geert van Ieperen created on 2-11-2026.
 */
@FunctionalInterface
public interface MouseSelector {
    /**
     * @return the target at this screen position, or null if this selector has nothing there
     */
    MouseTarget pick(int xSc, int ySc);
}
