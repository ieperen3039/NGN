package io.github.ieperen3039.ngn.GUIMenu.Components;

import io.github.ieperen3039.ngn.GUIMenu.Rendering.NGFont;
import io.github.ieperen3039.ngn.GUIMenu.Rendering.SFrameLookAndFeel;
import io.github.ieperen3039.ngn.GUIMenu.SComponentProperties;

/**
 * a simple panel with text.
 * @author Geert van Ieperen. Created on 22-9-2018.
 */
public class STextArea extends STextComponent {
    public STextArea(String text, SComponentProperties props) {
        super(text, props);
    }

    public STextArea(
            String text, int minHeight, int minWidth, boolean doGrowInWidth, NGFont.TextType textType,
            SFrameLookAndFeel.Alignment alignment
    ) {
        super(text, textType, alignment, minWidth, minHeight);
        setGrowthPolicy(doGrowInWidth, false);
    }

    public STextArea(String text, int minHeight) {
        this(text, minHeight, 0, true, NGFont.TextType.REGULAR, SFrameLookAndFeel.Alignment.LEFT_MIDDLE);
    }

    @Override
    public String toString() {
        String text = getText();
        String substring = text.length() > 25 ? text.substring(0, 20) + "..." : text;
        return this.getClass().getSimpleName() + " (" + substring + ")";
    }
}
