package de.terranova.nations.utils.terraRenderer.refactor.Anchor;

import org.joml.Vector3f;

public enum Anchor3D {
    CENTER(0, 0, 0),

    // corners
    MIN_MIN_MIN(-1, -1, -1),
    MIN_MIN_MAX(-1, -1,  1),
    MIN_MAX_MIN(-1,  1, -1),
    MIN_MAX_MAX(-1,  1,  1),
    MAX_MIN_MIN( 1, -1, -1),
    MAX_MIN_MAX( 1, -1,  1),
    MAX_MAX_MIN( 1,  1, -1),
    MAX_MAX_MAX( 1,  1,  1),

    // face centers (examples – extend as you like)
    CENTER_MIN_MIN(0, -1, -1),
    CENTER_MIN_MAX(0, -1,  1),
    CENTER_MAX_MIN(0,  1, -1),
    CENTER_MAX_MAX(0,  1,  1),

    MIN_CENTER_MIN(-1, 0, -1),
    MIN_CENTER_MAX(-1, 0,  1),
    MAX_CENTER_MIN( 1, 0, -1),
    MAX_CENTER_MAX( 1, 0,  1),

    MIN_MIN_CENTER(-1, -1, 0),
    MIN_MAX_CENTER(-1,  1, 0),
    MAX_MIN_CENTER( 1, -1, 0),
    MAX_MAX_CENTER( 1,  1, 0);

    private final int ax, ay, az;

    Anchor3D(int ax, int ay, int az) {
        this.ax = ax;
        this.ay = ay;
        this.az = az;
    }


    /**
     * Returns local offset from center for a box of the given size.
     */
    public Vector3f offsetForSize(Vector3f size) {
        return new Vector3f(
                size.x * ax * 0.5f,
                size.y * ay * 0.5f,
                size.z * az * 0.5f
        );
    }
}