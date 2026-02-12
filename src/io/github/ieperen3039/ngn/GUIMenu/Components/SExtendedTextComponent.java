package io.github.ieperen3039.ngn.GUIMenu.Components;

import io.github.ieperen3039.ngn.GUIMenu.Rendering.NGFont;
import io.github.ieperen3039.ngn.GUIMenu.Rendering.SFrameLookAndFeel;
import io.github.ieperen3039.ngn.GUIMenu.SComponentProperties;
import io.github.ieperen3039.ngn.InputHandling.MouseClickListener;
import io.github.ieperen3039.ngn.InputHandling.MouseDragListener;
import io.github.ieperen3039.ngn.InputHandling.MouseReleaseListener;

/**
 * @author Geert van Ieperen. Created on 25-9-2018.
 */
public class SExtendedTextComponent extends STextComponent
        implements MouseClickListener, MouseReleaseListener, MouseDragListener {
    private MouseDragListener dragListener;
    private MouseClickListener clickListener;
    private MouseReleaseListener releaseListener;

    public SExtendedTextComponent(String text, SComponentProperties props) {
        super(text, props);
    }

    public SExtendedTextComponent(
            String text, int minWidth, int minHeight, boolean doGrowInWidth, NGFont.TextType textType,
            SFrameLookAndFeel.Alignment alignment
    ) {
        super(text, textType, alignment, minHeight, minWidth);
        setGrowthPolicy(doGrowInWidth, false);
    }

    public SExtendedTextComponent(STextComponent source) {
        this(source.getText(), source.minWidth, source.minHeight, source.wantHorizontalGrow(), source.textType, source.alignment);
    }

    @Override
    public void onClick(int button, int xRel, int yRel) {
        if (clickListener != null) {
            clickListener.onClick(button, xRel, yRel);
        }
    }

    @Override
    public void onMouseDrag(int xDelta, int yDelta, float xPos, float yPos) {
        if (dragListener != null) {
            dragListener.onMouseDrag(xDelta, yDelta, xPos, yPos);
        }
    }

    @Override
    public void onRelease(int button) {
        if (releaseListener != null) {
            releaseListener.onRelease(button);
        }
    }

    public SExtendedTextComponent setDragListener(MouseDragListener dragListener) {
        this.dragListener = dragListener;
        return this;
    }

    public SExtendedTextComponent setClickListener(MouseClickListener clickListener) {
        this.clickListener = clickListener;
        return this;
    }

    public SExtendedTextComponent setReleaseListener(MouseReleaseListener releaseListener) {
        this.releaseListener = releaseListener;
        return this;
    }
}
