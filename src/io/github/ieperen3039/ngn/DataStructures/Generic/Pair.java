package io.github.ieperen3039.ngn.DataStructures.Generic;

import java.io.Serializable;
import java.util.Objects;

/**
 * Pair class that simply holds two variables.
 *
 * @param <L> Left type
 * @param <R> Right type
 */
public record Pair<L, R>(L left, R right) implements Serializable {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o == null) || (getClass() != o.getClass())) return false;

        Pair<?, ?> other = (Pair<?, ?>) o;
        return Objects.equals(left, other.left) && Objects.equals(right, other.right);
    }

    @Override
    public String toString() {
        return "[" + left + ", " + right + "]";
    }
}
