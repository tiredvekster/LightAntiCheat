package me.vekster.lightanticheat.check.checks.combat;

import me.vekster.lightanticheat.check.Check;
import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.util.async.AsyncUtil;
import me.vekster.lightanticheat.version.VerUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public abstract class CombatCheck extends Check {
    public CombatCheck(CheckName name) {
        super(name);
    }

    protected static final Set<EntityType> flyingEntities = new HashSet<>();

    static {
        flyingEntities.add(VerUtil.entityTypes.get("PARROT"));
        flyingEntities.add(EntityType.BAT);
        flyingEntities.add(EntityType.BLAZE);
        flyingEntities.add(EntityType.GHAST);
        flyingEntities.add(EntityType.SQUID);
        flyingEntities.add(VerUtil.entityTypes.get("SALMON"));
        flyingEntities.add(VerUtil.entityTypes.get("COD"));
        flyingEntities.add(VerUtil.entityTypes.get("TROPICAL_FISH"));
        flyingEntities.add(VerUtil.entityTypes.get("DOLPHIN"));
    }

    public double distanceToHitbox(Player player, Entity entity) {
        if (player == null || entity == null)
            return -1;
        return AABB.from(entity).collidesD(Ray.from(player), 0, 16);
    }

    /**
     * <a href="https://pastebin.com/eHQawAme">Author</a>
     * Just a ray class I made with useful methods I needed.
     */
    public static class Ray {
        private Vector origin;
        private Vector direction;

        // Create a ray at the origin pointing in a direction.
        public Ray(Vector origin, Vector direction) {
            this.origin = origin;
            this.direction = direction;
        }

        // Create a ray based on where the player is looking.
        // Origin: Player Eye Location
        // Direction: Player-looking direction
        public static Ray from(Player player) {
            return new Ray(player.getEyeLocation().toVector(), player.getLocation().getDirection());
        }

        // (Used for rotating vectors) Creates a vector in the horizontal plane (y=0) perpendicular to a vector.
        public static Vector right(Vector vector) {
            Vector n = vector.clone();
            n = n.setY(0).normalize();
            double x = n.getX();
            n.setX(n.getZ());
            n.setZ(-x);
            return n;
        }

        // Returns a normalized version of this Ray with the Y component set to 0
        public Ray level() {
            return new Ray(origin, direction.setY(0).normalize());
        }

        public Vector getOrigin() {
            return origin;
        }

        public Vector getDirection() {
            return direction;
        }

        public double origin(int i) {
            switch (i) {
                case 0:
                    return origin.getX();
                case 1:
                    return origin.getY();
                case 2:
                    return origin.getZ();
                default:
                    return 0;
            }
        }

        public double direction(int i) {
            switch (i) {
                case 0:
                    return direction.getX();
                case 1:
                    return direction.getY();
                case 2:
                    return direction.getZ();
                default:
                    return 0;
            }
        }

        // Get a point x distance away from this ray.
        // Can be used to get a point 2 blocks in front of a player's face.
        public Vector getPoint(double distance) {
            return direction.clone().normalize().multiply(distance).add(origin);
        }

        // Same as above, but no need to construct object.
        public static Location getPoint(Player player, double distance) {
            Vector point = Ray.from(player).getPoint(distance);
            World world = AsyncUtil.getWorld(player);
            if (world == null) world = player.getWorld();
            return new Location(world, point.getX(), point.getY(), point.getZ());
        }
    }

    /**
     * <a href="https://pastebin.com/PV63ZkQy">Author</a>
     * Just an AABB class I made with some useful methods I needed.
     * Mainly for fast Ray-AABB collision detection.
     */
    public static class AABB {
        private Vector min;
        protected Vector max;

        // Create Bounding Box from min/max locations.
        public AABB(Vector min, Vector max) {
            this(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
        }

        // Main constructor for AABB
        public AABB(double x1, double y1, double z1, double x2, double y2, double z2) {
            this.min = new Vector(Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2));
            this.max = new Vector(Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2));
        }

        private AABB(Entity entity) {
            this.min = getMin(entity);
            this.max = getMax(entity);
        }

        private Vector getMin(Entity entity) {
            return entity.getLocation().toVector().add(new Vector(-0.3, 0, -0.3));
        }

        private Vector getMax(Entity entity) {
            return entity.getLocation().toVector().add(new Vector(0.3, 1.8, 0.3));
        }

        // Create an AABB based on an entity's hitbox
        public static AABB from(Entity entity) {
            return new AABB(entity);
        }

        public Vector getMin() {
            return min;
        }

        public Vector getMax() {
            return max;
        }

        // Returns minimum x, y, or z point from inputs 0, 1, or 2.
        public double min(int i) {
            switch (i) {
                case 0:
                    return min.getX();
                case 1:
                    return min.getY();
                case 2:
                    return min.getZ();
                default:
                    return 0;
            }
        }

        // Returns maximum x, y, or z point from inputs 0, 1, or 2.
        public double max(int i) {
            switch (i) {
                case 0:
                    return max.getX();
                case 1:
                    return max.getY();
                case 2:
                    return max.getZ();
                default:
                    return 0;
            }
        }

        // Check if a Ray passes through this box. tmin and tmax are the bounds.
        // Example: If you wanted to see if the Ray collides anywhere from its
        // origin to 5 units away, the values would be 0 and 5.
        public boolean collides(Ray ray, double tmin, double tmax) {
            for (int i = 0; i < 3; i++) {
                double d = 1 / ray.direction(i);
                double t0 = (min(i) - ray.origin(i)) * d;
                double t1 = (max(i) - ray.origin(i)) * d;
                if (d < 0) {
                    double t = t0;
                    t0 = t1;
                    t1 = t;
                }
                tmin = Math.max(t0, tmin);
                tmax = Math.min(t1, tmax);
                if (tmax <= tmin) return false;
            }
            return true;
        }

        // Same as other collides method, but returns the distance of the nearest
        // point of collision of the ray and box, or -1 if no collision.
        public double collidesD(Ray ray, double tmin, double tmax) {
            for (int i = 0; i < 3; i++) {
                double d = 1 / ray.direction(i);
                double t0 = (min(i) - ray.origin(i)) * d;
                double t1 = (max(i) - ray.origin(i)) * d;
                if (d < 0) {
                    double t = t0;
                    t0 = t1;
                    t1 = t;
                }
                tmin = Math.max(t0, tmin);
                tmax = Math.min(t1, tmax);
                if (tmax <= tmin) return -1;
            }
            return tmin;
        }

        // Check if the location is in this box.
        public boolean contains(Location location) {
            if (location.getX() > max.getX()) return false;
            if (location.getY() > max.getY()) return false;
            if (location.getZ() > max.getZ()) return false;
            if (location.getX() < min.getX()) return false;
            if (location.getY() < min.getY()) return false;
            if (location.getZ() < min.getZ()) return false;
            return true;
        }
    }

}
