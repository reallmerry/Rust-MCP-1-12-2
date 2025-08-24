package io.reallmerry.rstudio.townycenturies;
public enum Age {
    PRIMITIVE,
    STONE,
    IRON,
    EARLY_MEDIEVAL,
    CLASSIC_MEDIEVAL,
    LATE_MEDIEVAL,
    RENAISSANCE,
    MODERN,
    ATOMIC;

    public static Age nextOf(Age a) {
        int i = a.ordinal() + 1;
        return (i < values().length) ? values()[i] : a;
    }

    public static Age fromName(String s) {
        for (Age a : values()) {
            if (a.name().equalsIgnoreCase(s)) return a;
        }
        throw new IllegalArgumentException("Unknown age: " + s);
    }
}