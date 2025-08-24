package io.reallmerry.rstudio.townycenturies.logic;

public enum PolicyMode {
    GATED_ONLY,
    WHITELIST;

    public static PolicyMode fromString(String s) {
        if (s == null) return GATED_ONLY;
        switch (s.toLowerCase()) {
            case "whitelist": return WHITELIST;
            default: return GATED_ONLY;
        }
    }
}