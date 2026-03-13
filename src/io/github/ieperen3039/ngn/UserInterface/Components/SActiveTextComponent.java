package io.github.ieperen3039.ngn.UserInterface.Components;

import java.util.function.Supplier;

import io.github.ieperen3039.ngn.UserInterface.SComponentProperties;
import io.github.ieperen3039.ngn.UserInterface.Rendering.NGFont;
import io.github.ieperen3039.ngn.UserInterface.Rendering.SFrameLookAndFeel;

/**
 * @author Geert van Ieperen created on 22-5-2020.
 */
public class SActiveTextComponent extends STextComponent {
    private final Supplier<String> supplier;

    public SActiveTextComponent(Supplier<String> supplier, SComponentProperties props) {
        super("", props);
        this.supplier = supplier;
    }

    public SActiveTextComponent(Supplier<String> supplier, int minHeight) {
        this(supplier, NGFont.TextType.REGULAR, SFrameLookAndFeel.Alignment.LEFT_MIDDLE, 0, minHeight);
    }

    public SActiveTextComponent(
            Supplier<String> supplier, NGFont.TextType textType,
            SFrameLookAndFeel.Alignment alignment,
            int width, int height
    ) {
        super("", textType, alignment, width, height);
        this.supplier = supplier;
    }

    @Override
    public String getText() {
        return supplier.get();
    }
}
