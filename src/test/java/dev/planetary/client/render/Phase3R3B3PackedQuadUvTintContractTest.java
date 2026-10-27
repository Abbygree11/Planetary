package dev.planetary.client.render;

import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.world.PlanetVanillaDirection;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.IQuadTransformer;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 3R-3B-3: test-only representative packed quad invariants.
 * Static JVM/test fixture != applied client model/render/UV game acceptance.
 */
final class Phase3R3B3PackedQuadUvTintContractTest {
    @Test
    void sixGravityFacesPreserveUvTintPackedLightColorAndQuadFlags()
            throws Exception {
        TextureAtlasSprite sprite = mock(TextureAtlasSprite.class);
        int[] packed = new int[IQuadTransformer.STRIDE * 4];
        for (int vertex = 0; vertex < 4; vertex++) {
            int off = vertex * IQuadTransformer.STRIDE;
            packed[off + IQuadTransformer.POSITION] = Float.floatToRawIntBits(
                    (vertex & 1) == 0 ? 0.125f : 0.875f);
            packed[off + IQuadTransformer.POSITION + 1] =
                    Float.floatToRawIntBits(0.75f);
            packed[off + IQuadTransformer.POSITION + 2] = Float.floatToRawIntBits(
                    vertex < 2 ? 0.25f : 0.75f);
            packed[off + IQuadTransformer.UV0] =
                    Float.floatToRawIntBits(0.0625f + vertex * 0.125f);
            packed[off + IQuadTransformer.UV0 + 1] =
                    Float.floatToRawIntBits(0.1875f + vertex * 0.0625f);
            packed[off + IQuadTransformer.COLOR] = 0xA1345678 + vertex;
            packed[off + IQuadTransformer.UV2] = 0x00F00040 + vertex;
            packed[off + IQuadTransformer.NORMAL] = 0x00007f00;
        }
        BakedQuad original = new BakedQuad(packed, 3, Direction.UP,
                sprite, true, false);

        for (PlanetFace face : PlanetFace.values()) {
            BakedQuad rotated = PlanetBakedModelRotation.rotateQuad(original, face);
            if (face == PlanetFace.POS_Y) {
                assertSame(original, rotated, "+Y must not alter canonical model");
            } else {
                assertNotSame(original, rotated);
            }
            Direction expected = PlanetVanillaDirection.localToWorld(
                    new PlanetGravityFrame(face), Direction.UP);
            assertEquals(expected, rotated.getDirection(), "outward quad " + face);
            assertEquals(3, rotated.getTintIndex(), "mod tint index " + face);
            assertSame(sprite, rotated.getSprite(), "sprite identity " + face);
            assertTrue(rotated.isShade(), "shade flag " + face);
            assertFalse(rotated.hasAmbientOcclusion(), "AO flag " + face);

            for (int vertex = 0; vertex < 4; vertex++) {
                int off = vertex * IQuadTransformer.STRIDE;
                int[] raw = rotated.getVertices();
                assertEquals(packed[off + IQuadTransformer.UV0],
                        raw[off + IQuadTransformer.UV0], "u " + face);
                assertEquals(packed[off + IQuadTransformer.UV0 + 1],
                        raw[off + IQuadTransformer.UV0 + 1], "v " + face);
                assertEquals(packed[off + IQuadTransformer.COLOR],
                        raw[off + IQuadTransformer.COLOR], "vertex color " + face);
                assertEquals(packed[off + IQuadTransformer.UV2],
                        raw[off + IQuadTransformer.UV2], "packed light " + face);
                int normal = raw[off + IQuadTransformer.NORMAL];
                double nx = ((byte) normal) / 127.0;
                double ny = ((byte) (normal >>> 8)) / 127.0;
                double nz = ((byte) (normal >>> 16)) / 127.0;
                double dot = nx * expected.getStepX() +
                        ny * expected.getStepY() + nz * expected.getStepZ();
                assertTrue(dot > 0.96, "packed normal " + face +
                        " vertex " + vertex + " dot=" + dot);
            }
        }
    }

    @Test
    void forwardsModelDataRenderTypeAndPhysicalCullSideToCanonicalModel() {
        BakedModel original = mock(BakedModel.class);
        TextureAtlasSprite sprite = mock(TextureAtlasSprite.class);
        BlockState state = Blocks.OAK_PLANKS.defaultBlockState();
        RandomSource random = RandomSource.create(3579L);
        ModelData modelData = ModelData.EMPTY;
        PlanetFace face = PlanetFace.POS_X;
        PlanetGravityFrame frame = new PlanetGravityFrame(face);
        Direction physical = PlanetVanillaDirection.localToWorld(frame, Direction.UP);

        int[] data = new int[IQuadTransformer.STRIDE * 4];
        for (int i = 0; i < 4; i++) {
            int off = i * IQuadTransformer.STRIDE;
            data[off + IQuadTransformer.NORMAL] = 0x00007f00;
        }
        BakedQuad originalQuad = new BakedQuad(
                data, -1, Direction.UP, sprite, true, true);
        when(original.getQuads(state, Direction.UP, random, modelData, null))
                .thenReturn(List.of(originalQuad));

        BakedModel rotated = PlanetBakedModelRotation.orientForBlock(
                original, state, face, Set.of(face));
        List<BakedQuad> quads = rotated.getQuads(
                state, physical, random, modelData, null);
        verify(original, times(1)).getQuads(
                state, Direction.UP, random, modelData, null);
        assertEquals(1, quads.size());
        assertEquals(physical, quads.getFirst().getDirection());
        assertNotSame(originalQuad, quads.getFirst());
    }
}
