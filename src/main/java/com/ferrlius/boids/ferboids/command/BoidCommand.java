package com.ferrlius.boids.ferboids.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public class BoidCommand {
    public static void onRegisterCommands(net.minecraftforge.event.RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("summonboid")
                // <entity> argument: choose the mob by its registry ID with autocompletion.
                .then(Commands.argument("entity", ResourceLocationArgument.id())
                        .suggests((context, builder) ->
                                SharedSuggestionProvider.suggestResource(ForgeRegistries.ENTITY_TYPES.getKeys(), builder)
                        )
                        // [<pos>] argument: position for spawning. (Here, we require it.)
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                // <count> argument: how many to summon.
                                .then(Commands.argument("count", IntegerArgumentType.integer(1))
                                        .executes(context -> {
                                            // Retrieve arguments.
                                            // Inside your command execution code:
                                            ResourceLocation entityId = ResourceLocationArgument.getId(context, "entity");
                                            BlockPos pos = BlockPosArgument.getBlockPos(context, "pos");
                                            int count = IntegerArgumentType.getInteger(context, "count");
                                            Level world = context.getSource().getLevel();

                                            for (int i = 0; i < count; i++) {
                                                var entityType = ForgeRegistries.ENTITY_TYPES.getValue(entityId);
                                                if (entityType == null) {
                                                    context.getSource().sendFailure(Component.literal("Unknown entity: " + entityId));
                                                    return 0;
                                                }
                                                var mob = entityType.create(world);
                                                if (mob == null) {
                                                    context.getSource().sendFailure(Component.literal("Failed to create entity: " + entityId));
                                                    return 0;
                                                }
                                                mob.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
                                                mob.getPersistentData().putBoolean("BoidsAssisted", true);

                                                // If the spawned entity is a Mob, add the new goal to look in its velocity direction.
                                                if (mob instanceof Mob) {
                                                    ((Mob) mob).goalSelector.addGoal(6, new com.ferrlius.boids.ferboids.goals.LookInVelocityDirectionGoal((Mob) mob));
                                                }
                                                world.addFreshEntity(mob);
                                            }

                                            context.getSource().sendSuccess(() -> Component.literal("Summoned " + count + " mob(s) with boids assistance."), true);
                                            return 1;
                                        })
                                )
                        )
                )
        );
    }
}