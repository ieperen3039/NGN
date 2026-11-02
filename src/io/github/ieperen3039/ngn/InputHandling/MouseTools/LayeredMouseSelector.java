package io.github.ieperen3039.ngn.InputHandling.MouseTools;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Ordered layers of selectors; the first non-null pick wins, else {@link MouseTarget#NONE}.
 * @author Geert van Ieperen created on 2-11-2026.
 */
public class LayeredMouseSelector implements MouseSelector {
    private final List<MouseSelector> layers = new CopyOnWriteArrayList<>();

    public void addLayer(MouseSelector layer) {
        layers.add(layer);
    }

    public void addLayer(int index, MouseSelector layer) {
        layers.add(index, layer);
    }

    public void removeLayer(MouseSelector layer) {
        layers.remove(layer);
    }

    @Override
    public MouseTarget pick(int xSc, int ySc) {
        for (MouseSelector layer : layers) {
            MouseTarget target = layer.pick(xSc, ySc);
            if (target != null) return target;
        }
        return MouseTarget.NONE;
    }
}
