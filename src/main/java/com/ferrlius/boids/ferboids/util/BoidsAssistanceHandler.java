package com.ferrlius.boids.ferboids.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = "ferboids")
public class BoidsAssistanceHandler {

    // Adjusted parameters:
    private static final double NEIGHBOR_RADIUS = 5.0;
    // Increase threshold so mobs start repelling when closer than ~1.2 blocks.
    private static final double SEPARATION_THRESHOLD = 2.0;
    // Increase separation weight to push mobs apart more strongly.
    private static final double SEPARATION_WEIGHT = 1.5;
    private static final double ALIGNMENT_WEIGHT = 0.04;
    private static final double COHESION_WEIGHT = 0.01;
    // Random weight for a small random horizontal component.
    private static final double RANDOM_WEIGHT = 0.02;
    // No base upward force (or set to 0 if you don’t need constant lift).
    private static final double HEIGHT_UPWARD_BASE = 0.0;
    // Height correction forces (vertical only)
    private static final double HEIGHT_CORRECTION_WEIGHT = 0.1;
    private static final double MIN_HEIGHT = 90;
    private static final double MAX_HEIGHT = 120;
    // Maximum velocity the assistance can provide.
    private static final double MAX_ASSIST_SPEED = 0.3;

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        // Only process on server side
        if (entity.level().isClientSide()) return;
        if (!(entity instanceof Mob)) return;

        Mob mob = (Mob) entity;
        // Only process mobs flagged for boids assistance
        if (!mob.getPersistentData().getBoolean("BoidsAssisted")) return;
        // If the mob is in combat, skip processing
        if (mob.getTarget() != null) return;

        // Search for neighbors in a box of radius NEIGHBOR_RADIUS
        AABB searchBox = new AABB(
                mob.getX() - NEIGHBOR_RADIUS, mob.getY() - NEIGHBOR_RADIUS, mob.getZ() - NEIGHBOR_RADIUS,
                mob.getX() + NEIGHBOR_RADIUS, mob.getY() + NEIGHBOR_RADIUS, mob.getZ() + NEIGHBOR_RADIUS
        );
        List<Mob> neighbors = mob.level().getEntitiesOfClass(
                Mob.class,
                searchBox,
                other -> other != mob && other.getPersistentData().getBoolean("BoidsAssisted")
        );

        // 1. Separation: Compute repulsion force from neighbors that are too close.
        Vec3 separation = Vec3.ZERO;
        int sepCount = 0;
        for (Mob neighbor : neighbors) {
            double distance = mob.position().distanceTo(neighbor.position());
            if (distance < SEPARATION_THRESHOLD && distance > 0) {
                // Compute vector from neighbor to this mob
                Vec3 diff = mob.position().subtract(neighbor.position());
                separation = separation.add(diff.normalize().scale(1.0 / distance));
                sepCount++;
            }
        }
        if (sepCount > 0) {
            separation = separation.scale(1.0 / sepCount).normalize().scale(SEPARATION_WEIGHT);
        }

        // 2. Alignment: Average the velocities of neighbors.
        Vec3 alignment = Vec3.ZERO;
        if (!neighbors.isEmpty()) {
            for (Mob neighbor : neighbors) {
                alignment = alignment.add(neighbor.getDeltaMovement());
            }
            alignment = alignment.scale(1.0 / neighbors.size());
            if (alignment.lengthSqr() > 0.0001) {
                alignment = alignment.normalize().scale(ALIGNMENT_WEIGHT);
            }
        }

        // 3. Cohesion: Compute pull toward the center of mass.
        Vec3 cohesion = Vec3.ZERO;
        if (!neighbors.isEmpty()) {
            double sumX = 0, sumY = 0, sumZ = 0;
            for (Mob neighbor : neighbors) {
                sumX += neighbor.getX();
                sumY += neighbor.getY();
                sumZ += neighbor.getZ();
            }
            Vec3 center = new Vec3(sumX / neighbors.size(), sumY / neighbors.size(), sumZ / neighbors.size());
            cohesion = center.subtract(mob.position());
            if (cohesion.lengthSqr() > 0.0001) {
                cohesion = cohesion.normalize().scale(COHESION_WEIGHT);
            }
        }

        // 4. Random horizontal component to break symmetry.
        double randX = (Math.random() * 2 - 1) * RANDOM_WEIGHT;
        double randZ = (Math.random() * 2 - 1) * RANDOM_WEIGHT;
        Vec3 random = new Vec3(randX, 0, randZ);

        // 5. Height correction: Correct vertical position if outside the allowed range.
        double correctionY = 0;
        double currentY = mob.getY();
        if (currentY > MAX_HEIGHT) {
            correctionY = -HEIGHT_CORRECTION_WEIGHT;
        } else if (currentY < MIN_HEIGHT) {
            correctionY = HEIGHT_CORRECTION_WEIGHT;
        }
        Vec3 heightCorrection = new Vec3(0, correctionY, 0);

        // 6. Base upward force (if needed; here we keep it 0)
        Vec3 baseUp = new Vec3(0, HEIGHT_UPWARD_BASE, 0);

        // 7. Sum all forces.
        Vec3 finalForce = separation.add(alignment).add(cohesion).add(random).add(heightCorrection).add(baseUp);

        // 8. Apply the final force to the current velocity.
        Vec3 currentVel = mob.getDeltaMovement();
        Vec3 newVel = currentVel.add(finalForce);

        // 9. Check horizontal movement: if horizontal (x and z) components are near zero, add a horizontal drift.
        Vec3 horizontalVel = new Vec3(newVel.x, 0, newVel.z);
        if (horizontalVel.lengthSqr() < 0.0025) { // if less than 0.05^2
            double driftX = (Math.random() * 2 - 1) * 0.1;
            double driftZ = (Math.random() * 2 - 1) * 0.1;
            newVel = newVel.add(driftX, 0, driftZ);
        }

        // 10. Limit the final velocity to MAX_ASSIST_SPEED.
        if (newVel.length() > MAX_ASSIST_SPEED) {
            newVel = newVel.normalize().scale(MAX_ASSIST_SPEED);
        }

        mob.setDeltaMovement(newVel);
    }
}