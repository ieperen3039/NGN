package io.github.ieperen3039.ngn.InputHandling.MouseTools;

import io.github.ieperen3039.ngn.UserInterface.Components.SComponent;

/**
 * A user interface component under the cursor
 * @author Geert van Ieperen created on 2-11-2026.
 */
public record UITarget(SComponent component) implements MouseTarget {
}
