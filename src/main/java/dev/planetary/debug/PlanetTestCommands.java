package dev.planetary.debug;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.planetary.PlanetaryMod;
import dev.planetary.topology.PlanetFace;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Locale;

/**
 * Explicit in-game control plane for test fixtures. No fixture command works
 * in non-Planet dimensions. Natural-placement lanes remain empty.
 */
@EventBusSubscriber(modid = PlanetaryMod.MOD_ID)
public final class PlanetTestCommands {
    private PlanetTestCommands() {
    }

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> root =
                Commands.literal("planetary")
                        .requires(source -> source.hasPermission(2));

        LiteralArgumentBuilder<CommandSourceStack> test =
                Commands.literal("test");

        test.then(Commands.literal("build")
                .executes(ctx -> buildAll(ctx.getSource())));
        test.then(Commands.literal("legend")
                .executes(ctx -> legend(ctx.getSource())));

        LiteralArgumentBuilder<CommandSourceStack> go =
                Commands.literal("go");
        LiteralArgumentBuilder<CommandSourceStack> rebuild =
                Commands.literal("rebuild");

        for (PlanetFace face : PlanetFace.values()) {
            String faceName = PlanetTestFixtures.faceName(face);
            go.then(Commands.literal(faceName)
                    .executes(ctx -> visit(ctx.getSource(), face)));
            rebuild.then(Commands.literal(faceName)
                    .executes(ctx -> rebuild(ctx.getSource(), face)));
        }

        test.then(go);
        test.then(rebuild);
        root.then(test);
        event.getDispatcher().register(root);
    }

    private static int buildAll(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        if (!PlanetTestFixtures.isPlanet(level)) {
            source.sendFailure(Component.literal(
                    "Planetary test fixtures require the dedicated Planet world."
            ));
            return 0;
        }

        PlanetTestFixtures.BuildStats result =
                PlanetTestFixtures.buildAll(level, false);

        source.sendSuccess(() -> Component.literal(
                "[Planetary Test] Built " + result.built()
                        + "/6 face labs; existing " + result.alreadyPresent()
                        + "; occupied (preserved) " + result.occupied()
                        + ". Use /planetary test go pos_y and "
                        + "/planetary test legend."
        ), false);
        return result.built();
    }

    private static int rebuild(
            CommandSourceStack source,
            PlanetFace face
    ) {
        ServerLevel level = source.getLevel();
        if (!PlanetTestFixtures.isPlanet(level)) {
            source.sendFailure(Component.literal(
                    "Test rebuilding is only allowed in the dedicated Planet world."
            ));
            return 0;
        }

        PlanetTestFixtures.buildFace(level, face, true);
        source.sendSuccess(() -> Component.literal(
                "[Planetary Test] Destructively rebuilt face "
                        + PlanetTestFixtures.faceName(face)
                        + " inside its reserved 41x33 lab footprint. "
                        + "Anything the player built inside that area was "
                        + "replaced; the rest of the world is unchanged."
        ), false);
        return 1;
    }

    private static int visit(
            CommandSourceStack source,
            PlanetFace face
    ) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = player.serverLevel();

        if (!PlanetTestFixtures.isPlanet(level)) {
            source.sendFailure(Component.literal(
                    "Teleport is only available in the dedicated Planet world."
            ));
            return 0;
        }

        // On the very first visit, create ONLY in free reserved space.
        PlanetTestFixtures.BuildStats result =
                PlanetTestFixtures.buildFace(level, face, false);
        if (result.occupied() != 0) {
            source.sendFailure(Component.literal(
                    "[Planetary Test] This face lab area is occupied and was "
                            + "not overwritten. Move existing builds away, "
                            + "or explicitly use /planetary test rebuild "
                            + PlanetTestFixtures.faceName(face)
            ));
            return 0;
        }

        BlockPos target = PlanetTestFixtures.arrival(face);
        player.teleportTo(
                level,
                target.getX() + 0.5D,
                target.getY() + 0.5D,
                target.getZ() + 0.5D,
                player.getYRot(),
                player.getXRot()
        );

        player.sendSystemMessage(Component.literal(
                "[Planetary Test] Face " + PlanetTestFixtures.faceName(face)
                        + ". CYAN = prebuilt reference, "
                        + "LIME = empty natural placement cell."
        ));
        player.sendSystemMessage(Component.literal(
                PlanetTestFixtures.gridLegend()
        ));
        return 1;
    }

    private static int legend(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal(
                PlanetTestFixtures.gridLegend()
        ), false);
        return 1;
    }
}
