package dev.planetary.client.render;

import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetFrameVector;
import dev.planetary.topology.PlanetGravityFrame;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.IQuadTransformer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

final class PlanetBakedModelRotationTest {
    private static final double EPS = 1.0E-6D;

    @Test
    void quadGeometryAndFacingRotateOnAllSixFaces() {
        TextureAtlasSprite sprite =
                mock(TextureAtlasSprite.class);

        float[][] localVertices = {
                {0.1F, 0.2F, 0.3F},
                {0.9F, 0.2F, 0.3F},
                {0.9F, 0.8F, 0.3F},
                {0.1F, 0.8F, 0.3F}
        };

        BakedQuad original =
                quad(
                        localVertices,
                        Direction.SOUTH,
                        sprite
                );

        for (PlanetFace face : PlanetFace.values()) {
            BakedQuad rotated =
                    PlanetBakedModelRotation.rotateQuad(
                            original,
                            face
                    );

            PlanetGravityFrame frame =
                    new PlanetGravityFrame(face);

            assertEquals(
                    frame.face() == PlanetFace.POS_Y
                            ? original.getDirection()
                            : dev.planetary.world.PlanetVanillaDirection
                                    .localToWorld(
                                            frame,
                                            original.getDirection()
                                    ),
                    rotated.getDirection(),
                    face.toString()
            );

            int[] actual =
                    rotated.getVertices();
            for (int i = 0; i < 4; i++) {
                PlanetFrameVector localFromCenter =
                        new PlanetFrameVector(
                                localVertices[i][0] - 0.5D,
                                localVertices[i][1] - 0.5D,
                                localVertices[i][2] - 0.5D
                        );
                PlanetFrameVector worldFromCenter =
                        frame.localToWorld(
                                localFromCenter
                        );

                assertEquals(
                        worldFromCenter.x() + 0.5D,
                        coordinate(actual, i, 0),
                        EPS,
                        face + " vertex " + i + " x"
                );
                assertEquals(
                        worldFromCenter.y() + 0.5D,
                        coordinate(actual, i, 1),
                        EPS,
                        face + " vertex " + i + " y"
                );
                assertEquals(
                        worldFromCenter.z() + 0.5D,
                        coordinate(actual, i, 2),
                        EPS,
                        face + " vertex " + i + " z"
                );
            }

            if (face == PlanetFace.POS_Y) {
                assertSame(original, rotated);
            } else {
                assertNotSame(original, rotated);
                assertSame(
                        rotated,
                        PlanetBakedModelRotation.rotateQuad(
                                original,
                                face
                        )
                );
            }
        }
    }

    private static BakedQuad quad(
            float[][] vertices,
            Direction direction,
            TextureAtlasSprite sprite
    ) {
        int[] data =
                new int[
                        IQuadTransformer.STRIDE * 4
                ];

        for (int i = 0; i < 4; i++) {
            int offset =
                    i * IQuadTransformer.STRIDE
                            + IQuadTransformer.POSITION;

            data[offset] =
                    Float.floatToRawIntBits(
                            vertices[i][0]
                    );
            data[offset + 1] =
                    Float.floatToRawIntBits(
                            vertices[i][1]
                    );
            data[offset + 2] =
                    Float.floatToRawIntBits(
                            vertices[i][2]
                    );
        }

        return new BakedQuad(
                data,
                -1,
                direction,
                sprite,
                true,
                true
        );
    }

    private static double coordinate(
            int[] vertices,
            int vertex,
            int axisOffset
    ) {
        int offset =
                vertex * IQuadTransformer.STRIDE
                        + IQuadTransformer.POSITION
                        + axisOffset;

        return Float.intBitsToFloat(
                vertices[offset]
        );
    }
}
