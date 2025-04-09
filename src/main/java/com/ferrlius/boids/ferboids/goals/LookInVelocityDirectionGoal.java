package com.ferrlius.boids.ferboids.goals;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

public class LookInVelocityDirectionGoal extends Goal {
    private final Mob mob;

    public LookInVelocityDirectionGoal(Mob mob) {
        this.mob = mob;
    }

    @Override
    public boolean canUse() {
        // Always run this goal; you can add extra conditions if needed.
        return true;
    }

    @Override
    public void tick() {
        Vec3 velocity = mob.getDeltaMovement();
        // Do nothing if the mob’s velocity is nearly zero
        if (velocity.lengthSqr() < 1e-4) {
            return;
        }
        // Calculate a target position by taking the current position plus a scaled velocity vector.
        // The scaling factor (here, 3) can be adjusted to modify how quickly the mob turns.
        Vec3 lookTarget = mob.position().add(velocity.scale(3));
        mob.lookAt(EntityAnchorArgument.Anchor.EYES, lookTarget);
    }
}