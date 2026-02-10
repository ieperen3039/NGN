package NG.GUIMenu.Components;

import NG.GUIMenu.LayoutManagers.GridLayoutManager;
import NG.GUIMenu.SComponentProperties;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * a row/column of toggle buttons, of which always exactly one is active
 * @author Geert van Ieperen created on 21-2-2020.
 */
public class SExclusiveButtonRow extends SComposite {
    private List<Consumer<Integer>> selectionListeners;
    private List<Consumer<Integer>> deselectionListeners;
    private List<Consumer<Integer>> switchListeners;

    private SToggleButton selected;
    private int selectedIndex = -1;

    /**
     * creates a row of buttons that display the given texts. There will be {@code elements.length} buttons, regardless
     * of the contents of elements. If {@code elements} contains null elements, the buttons will have no text.
     * @param horizontal if true, buttons are positioned in a row. if false, buttons are stacked in a column
     * @param elements   the names of the buttons. There will be as much buttons as names.
     */
    public SExclusiveButtonRow(boolean horizontal, String... elements) {
        this(horizontal, new SComponentProperties(0, 0, horizontal, !horizontal), elements);
    }

    /**
     * creates a row of buttons that display the given texts. There will be {@code elements.length} buttons, regardless
     * of the contents of elements. If {@code elements} contains null elements, those buttons will have no text.
     * @param horizontal       if true, buttons are positioned in a row. if false, buttons are stacked in a column
     * @param elements         the names of the buttons. There will be as much buttons as names.
     * @param buttonProperties
     */
    public SExclusiveButtonRow(boolean horizontal, SComponentProperties buttonProperties, String... elements) {
        super(new SContainer.GhostContainer(
                new GridLayoutManager(horizontal ? elements.length : 1, horizontal ? 1 : elements.length)
        ));

        selectionListeners = new ArrayList<>();
        deselectionListeners = new ArrayList<>();
        switchListeners = new ArrayList<>();

        Vector2i pos = new Vector2i(0, 0);
        Vector2ic delta = horizontal ? new Vector2i(1, 0) : new Vector2i(0, 1);

        for (int i = 0; i < elements.length; i++) {
            String label = elements[i] == null ? "" : elements[i];
            SToggleButton button = new SToggleButton(label, buttonProperties) {
                @Override // override to ignore deselection by click
                public void onClick(int button, int xSc, int ySc) {
                    if (!isActive()) super.onClick(button, xSc, ySc);
                }

                @Override // override to ignore deselection by click
                public void onRelease(int button) {
                    if (!isActive()) super.onRelease(button);
                }
            };
            int index = i;

            button.addStateChangeListener((s) -> select(s, button, index));

            add(button, pos);
            pos.add(delta);
        }
    }

    /**
     * Adds a listener that is activated when any of the button is set to active. The index passed to the listener is
     * the index of the button as given in the constructor.
     * @param listener receives the index of the selected button.
     * @return this
     */
    public SExclusiveButtonRow addSelectionListener(Consumer<Integer> listener) {
        selectionListeners.add(listener);
        if (selectedIndex >= 0)
            listener.accept(selectedIndex);
        return this;
    }

    /**
     * Adds a listener that is activated when any of the buttons deactivates. The listener receives the index of the button
     * that is deactivated, as given in the constructor.
     * @param listener receives the index of the deselected button.
     * @return this
     */
    public SExclusiveButtonRow addDeselectionListener(Consumer<Integer> listener) {
        deselectionListeners.add(listener);
        return this;
    }

    /**
     * Adds a listener that is activated when the selection is changed. The listener receives the index of the new selected button, 
     * as given in the constructor, or null if the previous selection was toggled (such that no button is selected).
     * @param listener receives the index of the selected button.
     * @return this
     */
    public SExclusiveButtonRow addSwitchListener(Consumer<Integer> listener) {
        switchListeners.add(listener);
        return this;
    }

    private void select(boolean toActive, SToggleButton elt, int index) {
        if (toActive) {
            SToggleButton previousButton = selected;
            selected = elt;
            selectedIndex = index;

            // this also recursively calls a select(false, ...) for this button
            if (previousButton != null)
                previousButton.setActive(false);

            selectionListeners.forEach(c -> c.accept(index));
            switchListeners.forEach(c -> c.accept(index));
        } else if (elt == selected) {
            selected = null;
            selectedIndex = -1;
            switchListeners.forEach(c -> c.accept(null));
            deselectionListeners.forEach(c -> c.accept(index));
        } else {
            deselectionListeners.forEach(c -> c.accept(index));
        }
    }
}
