package io.github.ieperen3039.ngn.GUIMenu.FrameManagers;

import io.github.ieperen3039.ngn.GUIMenu.Components.SComponent;
import io.github.ieperen3039.ngn.GUIMenu.Components.SFrame;
import io.github.ieperen3039.ngn.GUIMenu.Rendering.SFrameLookAndFeel;
import io.github.ieperen3039.ngn.Rendering.GLFWWindow;
import io.github.ieperen3039.ngn.Tools.Logger;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * 
 * Objects of this class can manage a window system that is behaviourally
 * similar to classes in the {@link javax.swing} package. New {@link SFrame}
 * objects can be added using
 * {@link #addFrame(SFrame)}.
 * 
 * @author Geert van Ieperen. Created on 20-9-2018.
 */
public class FrameManagerImpl extends SimpleUIManager {
    private final Deque<SFrame> frames; // the first element in this list has focus

    public FrameManagerImpl() {
        this.frames = new ArrayDeque<>();
    }

    @Override
    protected void drawInternal(SFrameLookAndFeel lookAndFeel) {
        frames.removeIf(SFrame::isDisposed);

        Iterator<SFrame> itr = frames.descendingIterator();
        while (itr.hasNext()) {
            final SFrame f = itr.next();

            if (f.isVisible()) {
                // if anything caused invalidation of the layout (e.g. text size information)
                // then redraw this frame
                do {
                    f.validateLayout();
                    f.draw(lookAndFeel, f.getPosition());
                } while (!f.layoutIsValid());
            }
        }

        if (modalComponent != null) {
            modalComponent.validateLayout();
            modalComponent.draw(lookAndFeel, modalComponent.getScreenPosition());
        }
    }

    public void addFrame(SFrame frame) {
        frame.validateLayout();

        int x = 50;
        int y = 200;

        SComponent component = getComponentAt(x, y);
        while (component != null) {
            x += component.getWidth();

            component = getComponentAt(x, y);
        }

        addFrame(frame, x, y);
    }

    public void addFrameCenter(SFrame frame, GLFWWindow window) {
        frame.validateLayout();
        int x = window.getWidth() / 2 - frame.getWidth() / 2;
        int y = window.getHeight() / 2 - frame.getHeight() / 2;
        addFrame(frame, x, y);
    }

    /**
     * adds a fame on the given position, and focusses it.
     * 
     * @param frame the frame to be added.
     * @param x     screen x coordinate in pixels from left
     * @param y     screen y coordinate in pixels from top
     */
    public void addFrame(SFrame frame, int x, int y) {
        // if the frame was already visible, still add it to make it focused.
        frames.remove(frame);

        boolean success = frames.offerFirst(frame);
        if (!success) {
            Logger.DEBUG.print("Too much subframes opened, removing the last one");
            frames.removeLast().dispose();
            frames.addFirst(frame);
        }

        frame.setPosition(x, y);
    }

    /**
     * brings the given from to the front-most position
     * 
     * @param frame a frame that has been added to this manager
     * @throws java.util.NoSuchElementException if the given frame has not been
     *                                          added or has been disposed.
     */
    public void focus(SFrame frame) {
        if (frame.isDisposed()) {
            throw new NoSuchElementException(frame + " is disposed");
        }

        // even if the frame was not opened, show it
        frame.setVisible(true);

        // no further action when already focused
        if (frame.equals(frames.peekFirst()))
            return;

        boolean success = frames.remove(frame);
        if (!success) {
            throw new NoSuchElementException(frame + " was not part of the window");
        }

        frames.addFirst(frame);
    }

    /**
     * Removes all frames
     * each of them
     */
    public void clear() {
        frames.forEach(SFrame::dispose);
        frames.clear();
    }

    @Override
    public boolean covers(int xSc, int ySc) {
        for (SFrame frame : frames) {
            if (frame.isVisible() && frame.contains(xSc, ySc)) {
                return true;
            }
        }

        return super.covers(xSc, ySc);
    }

    @Override
    public SComponent getComponentAt(int xSc, int ySc) {
        SFrame frame = getFrame(xSc, ySc);
        if (frame != null) {
            int xr = xSc - frame.getX();
            int yr = ySc - frame.getY();
            return frame.getComponentAt(xr, yr);

        } else {
            return super.getComponentAt(xSc, ySc);
        }
    }

    @Override
    protected void processParentClick(int button, SComponent component) {
        if (component instanceof SFrame frame) {
            focus(frame);
        }

        super.processParentClick(button, component);
    }

    private SFrame getFrame(int xSc, int ySc) {
        // check all frames, starting from the front-most frame
        for (SFrame frame : frames) {
            if (frame.isVisible() && frame.contains(xSc, ySc)) {
                return frame;
            }
        }
        return null;
    }

    public void addElement(SComponent component) {
        if (!(component instanceof SFrame)) {
            component = new SFrame(component.toString(), component);
        }

        addFrame((SFrame) component);
    }
}
