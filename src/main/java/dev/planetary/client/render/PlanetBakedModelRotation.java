package dev.planetary.client.render;

import com.mojang.math.Transformation;
import dev.planetary.topology.PlanetFace;
import dev.planetary.topology.PlanetGravityFrame;
import dev.planetary.topology.PlanetVector;
import dev.planetary.world.PlanetVanillaDirection;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.IQuadTransformer;
import net.neoforged.neoforge.client.model.QuadTransformers;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Cached physical views over canonical-local baked block models.
 *
 * <p>The original BakedModel remains authoritative. The wrapper only converts
 * the requested PHYSICAL cull side to the model's LOCAL side and transforms
 * returned quad geometry/normals back into PHYSICAL world axes.</p>
 */
public final class PlanetBakedModelRotation {
    private static final Map<BakedModel, EnumMap<PlanetFace, BakedModel>>
            MODEL_CACHE = new WeakHashMap<>();

    private static final Map<BakedQuad, EnumMap<PlanetFace, BakedQuad>>
            QUAD_CACHE = new WeakHashMap<>();

    private static final EnumMap<PlanetFace, IQuadTransformer>
            TRANSFORMERS = new EnumMap<>(PlanetFace.class);

    static {
        for (PlanetFace face : PlanetFace.values()) {
            TRANSFORMERS.put(
                    face,
                    QuadTransformers.applying(
                            transformation(face)
                    )
            );
        }
    }

    private PlanetBakedModelRotation() {
    }

    public static BakedModel orient(
            BakedModel model,
            PlanetFace face
    ) {
        if (face == PlanetFace.POS_Y) {
            return model;
        }

        synchronized (MODEL_CACHE) {
            return MODEL_CACHE
                    .computeIfAbsent(
                            model,
                            ignored ->
                                    new EnumMap<>(
                                            PlanetFace.class
                                    )
                    )
                    .computeIfAbsent(
                            face,
                            ignored ->
                                    new OrientedModel(
                                            model,
                                            face
                                    )
                    );
        }
    }

    static BakedQuad rotateQuad(
            BakedQuad quad,
            PlanetFace face
    ) {
        if (face == PlanetFace.POS_Y) {
            return quad;
        }

        synchronized (QUAD_CACHE) {
            return QUAD_CACHE
                    .computeIfAbsent(
                            quad,
                            ignored ->
                                    new EnumMap<>(
                                            PlanetFace.class
                                    )
                    )
                    .computeIfAbsent(
                            face,
                            ignored ->
                                    transformQuad(
                                            quad,
                                            face
                                    )
                    );
        }
    }

    private static List<BakedQuad> rotateQuads(
            List<BakedQuad> localQuads,
            PlanetFace face
    ) {
        if (face == PlanetFace.POS_Y
                || localQuads.isEmpty()) {
            return localQuads;
        }

        List<BakedQuad> result =
                new ArrayList<>(
                        localQuads.size()
                );

        for (BakedQuad quad : localQuads) {
            result.add(
                    rotateQuad(
                            quad,
                            face
                    )
            );
        }

        return List.copyOf(result);
    }

    private static BakedQuad transformQuad(
            BakedQuad original,
            PlanetFace face
    ) {
        BakedQuad transformed =
                TRANSFORMERS.get(face)
                        .process(original);

        PlanetGravityFrame frame =
                new PlanetGravityFrame(face);
        Direction physicalDirection =
                PlanetVanillaDirection.localToWorld(
                        frame,
                        original.getDirection()
                );

        return new BakedQuad(
                transformed.getVertices(),
                transformed.getTintIndex(),
                physicalDirection,
                transformed.getSprite(),
                transformed.isShade(),
                transformed.hasAmbientOcclusion()
        );
    }

    private static Transformation transformation(
            PlanetFace face
    ) {
        PlanetGravityFrame frame =
                new PlanetGravityFrame(face);

        PlanetVector east =
                frame.worldEast();
        PlanetVector up =
                frame.worldUp();
        PlanetVector south =
                frame.worldSouth();

        Matrix4f rotation =
                new Matrix4f();

        /*
         * JOML stores basis vectors in matrix columns for position
         * transformation: world = EAST*x + UP*y + SOUTH*z.
         */
        rotation.m00(east.x());
        rotation.m01(east.y());
        rotation.m02(east.z());

        rotation.m10(up.x());
        rotation.m11(up.y());
        rotation.m12(up.z());

        rotation.m20(south.x());
        rotation.m21(south.y());
        rotation.m22(south.z());

        Matrix4f aroundCenter =
                new Matrix4f()
                        .translation(
                                0.5F,
                                0.5F,
                                0.5F
                        )
                        .mul(rotation)
                        .translate(
                                -0.5F,
                                -0.5F,
                                -0.5F
                        );

        return new Transformation(
                aroundCenter
        );
    }

    private static final class OrientedModel
            extends BakedModelWrapper<BakedModel> {
        private final PlanetFace face;
        private final PlanetGravityFrame frame;

        private OrientedModel(
                BakedModel originalModel,
                PlanetFace face
        ) {
            super(originalModel);
            this.face = face;
            this.frame =
                    new PlanetGravityFrame(face);
        }

        @Override
        public List<BakedQuad> getQuads(
                @Nullable BlockState state,
                @Nullable Direction physicalSide,
                RandomSource rand
        ) {
            Direction localSide =
                    physicalSide == null
                            ? null
                            : PlanetVanillaDirection
                                    .worldToLocal(
                                            frame,
                                            physicalSide
                                    );

            return rotateQuads(
                    originalModel.getQuads(
                            state,
                            localSide,
                            rand
                    ),
                    face
            );
        }

        @Override
        public List<BakedQuad> getQuads(
                @Nullable BlockState state,
                @Nullable Direction physicalSide,
                RandomSource rand,
                ModelData extraData,
                @Nullable RenderType renderType
        ) {
            Direction localSide =
                    physicalSide == null
                            ? null
                            : PlanetVanillaDirection
                                    .worldToLocal(
                                            frame,
                                            physicalSide
                                    );

            return rotateQuads(
                    originalModel.getQuads(
                            state,
                            localSide,
                            rand,
                            extraData,
                            renderType
                    ),
                    face
            );
        }
    }
}
