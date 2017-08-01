package edu.cmu.tartan;

import java.util.Objects;
import java.util.function.Predicate;

public final class TartanUtils {
    public static final Predicate<String> IS_EMPTY = (s -> Objects.isNull(s) || s.isEmpty());
}
