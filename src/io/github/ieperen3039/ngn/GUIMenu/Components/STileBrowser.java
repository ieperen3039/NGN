package io.github.ieperen3039.ngn.GUIMenu.Components;

import io.github.ieperen3039.ngn.GUIMenu.LayoutManagers.LimitedVisibilityLayout;
import io.github.ieperen3039.ngn.GUIMenu.Rendering.SFrameLookAndFeel;
import io.github.ieperen3039.ngn.GUIMenu.SComponentProperties;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Geert van Ieperen created on 13-5-2019.
 */
public class STileBrowser extends SContainer {
    private static final int SCROLL_BUTTON_WIDTH = 50;
    private static final SComponentProperties BUTTON_PROPERTIES = new SComponentProperties(SCROLL_BUTTON_WIDTH, 0, false, true);
    private final STextComponent buttonFurther;
    private final STextComponent buttonBack;
    private LimitedVisibilityLayout layoutManager;

    private final List<Consumer<Integer>> stateChangeListeners = new ArrayList<>();

    public STileBrowser(boolean growPolicy, int minWidth, int eltWidth, SComponent... elements) {
        this(eltWidth, growPolicy, minWidth - 2 * SCROLL_BUTTON_WIDTH, elements);
    }

    // for 1 element
    public STileBrowser(boolean growPolicy, int eltWidth, SComponent... elements) {
        this(growPolicy, new LimitedVisibilityLayout(1, eltWidth, false), elements);
    }

    private STileBrowser(int eltWidth, boolean growPolicy, int layoutWidth, SComponent... elements) {
        this(growPolicy, new LimitedVisibilityLayout(layoutWidth / eltWidth, layoutWidth, false), elements);
    }

    private STileBrowser(boolean growPolicy, LimitedVisibilityLayout layoutManager, SComponent... elements) {
        super(layoutManager, new ComponentBorder(0));
        this.layoutManager = layoutManager;

        buttonBack = new SButton("<", BUTTON_PROPERTIES, () -> inc(-1));
        buttonFurther = new SButton(">", BUTTON_PROPERTIES, () -> inc(1));

        for (SComponent elt : elements) {
            layoutManager.add(elt, null);
        }

        layoutBorder.add(buttonBack.minWidth(), buttonFurther.minWidth(), 0, 0);
        setGrowthPolicy(growPolicy, growPolicy);
    }    
    
    /**
    * @param action Upon change, this action is activated
    */
   public STileBrowser addStateChangeListener(Consumer<Integer> action) {
       stateChangeListeners.add(action);
       return this;
   }

    private void inc(int v) {
        this.layoutManager.shiftVisible(v);
        invalidateLayout();

        for (Consumer<Integer> c : stateChangeListeners) {
            c.accept(v);
        }
    }

    /**
     * appends the given element to the end of the browser
     * @param elt the element to add
     */
    public void add(SComponent elt) {
        super.add(elt, null);
    }

    @Override
    public SComponent getComponentAt(int xRel, int yRel) {
        if (buttonBack.contains(xRel, yRel)) {
            Vector2ic position = buttonBack.getPosition();
            return buttonBack.getComponentAt(xRel - position.x(), yRel - position.y());

        } else if (buttonFurther.contains(xRel, yRel)) {
            Vector2ic position = buttonFurther.getPosition();
            return buttonFurther.getComponentAt(xRel - position.x(), yRel - position.y());

        } else {
            return super.getComponentAt(xRel, yRel);
        }
    }

    @Override
    public void doValidateLayout() {
        super.doValidateLayout();

        assert !children().isEmpty();

        // use original layout border for side buttons instead
        ComponentBorder border = layoutBorder;
        int height = getHeight() - border.top - border.bottom;

        buttonFurther.setSize(0, height);
        buttonFurther.setPosition(this.getWidth() - border.right, border.top);
        buttonFurther.validateLayout();
        buttonBack.setSize(0, height);
        buttonBack.setPosition(border.left - buttonBack.getWidth(), border.top);
        buttonBack.validateLayout();
    }

    @Override
    public void draw(SFrameLookAndFeel design, Vector2ic screenPosition) {
        buttonBack.draw(design, new Vector2i(screenPosition).add(buttonBack.getPosition()));
        buttonFurther.draw(design, new Vector2i(screenPosition).add(buttonFurther.getPosition()));
        drawChildren(design, screenPosition);
    }
}
