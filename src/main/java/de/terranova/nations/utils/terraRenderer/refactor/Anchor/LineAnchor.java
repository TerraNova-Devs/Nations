package de.terranova.nations.utils.terraRenderer.refactor.Anchor;

public enum LineAnchor {
    START(0f),
    CENTER(0.5f),
    END(1f);

    public final float t;

    LineAnchor(float t) {
        this.t = t;
    }
}