package de.terranova.nations.utils.terraRenderer.refactor;

import org.bukkit.Location;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class DisplayMath {

    private DisplayMath() {}

    public static Quaternionf eulerToQuaternion(Vector3f eulerDeg) {
        float pitchRad = (float) Math.toRadians(eulerDeg.x());
        float yawRad   = (float) Math.toRadians(eulerDeg.y());
        float rollRad  = (float) Math.toRadians(eulerDeg.z());
        // YXZ order: yaw(Y), pitch(X), roll(Z) – like before
        return new Quaternionf().rotateYXZ(yawRad, pitchRad, rollRad);
    }

    public static Vector3f quaternionToEulerYXZDeg(Quaternionf q) {
        Vector3f anglesRad = new Vector3f();
        q.getEulerAnglesYXZ(anglesRad);

        return new Vector3f(
                (float) Math.toDegrees(anglesRad.x),
                (float) Math.toDegrees(anglesRad.y),
                (float) Math.toDegrees(anglesRad.z)
        );
    }

    public static Location roundGrid(Location loc) {
        Location clone = loc.clone();
        clone.setX(Math.floor(clone.getX()) + 0.5);
        clone.setY(Math.floor(clone.getY()) + 0.5);
        clone.setZ(Math.floor(clone.getZ()) + 0.5);
        return clone;
    }
}
