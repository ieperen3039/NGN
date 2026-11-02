package io.github.ieperen3039.ngn.InputHandling.MouseTools;

/**
 * Something under the cursor. Games define their own, e.g. a record MapTarget(Vector3fc worldPos).
 * @author Geert van Ieperen created on 2-11-2026.
 */
public interface MouseTarget {
    /** nothing picked; the default tool gives it to the camera */
    MouseTarget NONE = new MouseTarget() {
        @Override
        public String toString() {
            return "NONE";
        }
    };
}
