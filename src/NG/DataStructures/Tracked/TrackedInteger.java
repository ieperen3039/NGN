package NG.DataStructures.Tracked;

/**
 * @author Geert van Ieperen created on 5-11-2017.
 */
public class TrackedInteger {
    private int current;
    private int previous;

    public TrackedInteger(int initial) {
        this.current = initial;
        this.previous = initial;
    }

    public int current() {
        return current;
    }

    public int previous() {
        return previous;
    }

    @Override
    public String toString() {
        return "[" + previous + " -> " + current + "]";
    }

    /**
     * sets the old current value to the new previous field, and the new current field to the new element
     * @param newElement the new current value
     */
    public void update(int newElement) {
        previous = current;
        current = newElement;
    }

    /**
     * updates the value by adding the parameter to the current value
     * @param addition the value that is added to the current. actual results may vary
     */
    public void addUpdate(Integer addition) {
        update(current + addition);
    }

    public Integer difference() {
        return current() - previous();
    }
}
