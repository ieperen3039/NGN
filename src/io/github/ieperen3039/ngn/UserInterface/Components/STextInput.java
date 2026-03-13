package io.github.ieperen3039.ngn.UserInterface.Components;

import io.github.ieperen3039.ngn.InputHandling.KeyPressListener;
import io.github.ieperen3039.ngn.InputHandling.MouseClickListener;
import io.github.ieperen3039.ngn.UserInterface.Rendering.NGFont;
import io.github.ieperen3039.ngn.UserInterface.Rendering.SFrameLookAndFeel;
import io.github.ieperen3039.ngn.UserInterface.Rendering.SFrameLookAndFeel.UIState;

import org.joml.Vector2ic;

/**
 * @author Geert van Ieperen. Created on 5-10-2018.
 */
public class STextInput extends STextComponent implements KeyPressListener, MouseClickListener {
    public STextInput(
            String text, int minHeight, int minWidth, boolean doGrowInWidth, NGFont.TextType textType,
            SFrameLookAndFeel.Alignment alignment
    ) {
        super(text, textType, alignment, minWidth, minHeight);
        setGrowthPolicy(doGrowInWidth, false);
    }

    @Override
    public void draw(SFrameLookAndFeel design, Vector2ic screenPosition) {
        design.draw(SFrameLookAndFeel.UIComponentType.INPUT_FIELD, UIState.ENABLED, screenPosition, getSize());
        super.draw(design, screenPosition);
    }

    @Override
    public void keyPressed(int keyCode) {

    }

    @Override
    public void onClick(int button, int xSc, int ySc) {

    }
}
