package dev.planetary.debug;

import dev.planetary.gravity.PlanetGravityRuntime;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityField;
import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.topology.PlanetVector;
import dev.planetary.world.PlanetBlockSupportQuery;
import dev.planetary.world.PlanetBlockSupportRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.RedstoneWallTorchBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;

import java.util.List;
import java.util.Objects;

/**
 * Runtime acceptance for canonical-local support and updateShape semantics.
 *
 * <p>The probe does not mutate the world. It evaluates hypothetical states at
 * the six interior cells two blocks outward from the core and routes an actual
 * updateShape call from each block's physical support neighbor.</p>
 */
public final class PlanetSupportDiagnostics {
    private PlanetSupportDiagnostics() {
    }

    public static Result verify(
            ServerLevel level
    ) {
        Objects.requireNonNull(level, "level");

        PlanetGravityField field =
                PlanetGravityRuntime.find(level)
                        .orElseThrow();

        int survivalChecks = 0;
        int updateChecks = 0;

        for (PlanetFace face : PlanetFace.values()) {
            BlockPos pos =
                    twoBlocksFromCore(
                            field,
                            face
                    );

            List<SupportCase> cases =
                    List.of(
                            new SupportCase(
                                    "standing torch",
                                    Blocks.TORCH.defaultBlockState(),
                                    Direction.DOWN
                            ),
                            new SupportCase(
                                    "wall torch",
                                    Blocks.WALL_TORCH
                                            .defaultBlockState()
                                            .setValue(
                                                    WallTorchBlock.FACING,
                                                    Direction.EAST
                                            ),
                                    Direction.WEST
                            ),
                            new SupportCase(
                                    "redstone wall torch",
                                    Blocks.REDSTONE_WALL_TORCH
                                            .defaultBlockState()
                                            .setValue(
                                                    RedstoneWallTorchBlock.FACING,
                                                    Direction.EAST
                                            ),
                                    Direction.WEST
                            ),
                            new SupportCase(
                                    "ladder",
                                    Blocks.LADDER
                                            .defaultBlockState()
                                            .setValue(
                                                    LadderBlock.FACING,
                                                    Direction.EAST
                                            ),
                                    Direction.WEST
                            ),
                            new SupportCase(
                                    "floor lever",
                                    Blocks.LEVER
                                            .defaultBlockState()
                                            .setValue(
                                                    FaceAttachedHorizontalDirectionalBlock.FACE,
                                                    AttachFace.FLOOR
                                            )
                                            .setValue(
                                                    FaceAttachedHorizontalDirectionalBlock.FACING,
                                                    Direction.NORTH
                                            ),
                                    Direction.DOWN
                            )
                    );

            for (SupportCase testCase : cases) {
                PlanetBlockSupportQuery query =
                        PlanetBlockSupportRuntime.query(
                                level,
                                pos,
                                testCase.localSupportDirection()
                        ).orElseThrow();

                if (!PlanetBlockSupportRuntime.isFaceSturdy(
                        level,
                        query
                )) {
                    throw new IllegalStateException(
                            "Support probe requires sturdy central stone at "
                                    + face
                                    + " / "
                                    + testCase.name()
                                    + ", support="
                                    + query.supportPos()
                    );
                }

                if (!testCase.state()
                        .canSurvive(
                                level,
                                pos
                        )) {
                    throw new IllegalStateException(
                            "canSurvive failed at "
                                    + face
                                    + " / "
                                    + testCase.name()
                    );
                }
                survivalChecks++;

                BlockState updated =
                        testCase.state()
                                .updateShape(
                                        query.physicalDirectionToSupport(),
                                        level.getBlockState(
                                                query.supportPos()
                                        ),
                                        level,
                                        pos,
                                        query.supportPos()
                                );

                if (!updated.is(
                        testCase.state().getBlock()
                )) {
                    throw new IllegalStateException(
                            "updateShape dropped supported block at "
                                    + face
                                    + " / "
                                    + testCase.name()
                    );
                }
                updateChecks++;
            }
        }

        return new Result(
                survivalChecks,
                updateChecks
        );
    }

    private static BlockPos twoBlocksFromCore(
            PlanetGravityField field,
            PlanetFace face
    ) {
        PlanetGravityFrame frame =
                new PlanetGravityFrame(face);
        PlanetVector up =
                frame.worldUp();

        return new BlockPos(
                field.core().x() + up.x() * 2,
                field.core().y() + up.y() * 2,
                field.core().z() + up.z() * 2
        );
    }

    private record SupportCase(
            String name,
            BlockState state,
            Direction localSupportDirection
    ) {
    }

    public record Result(
            int survivalChecks,
            int updateChecks
    ) {
    }
}
