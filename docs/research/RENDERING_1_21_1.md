# Research: static baked-model rendering and culling in Minecraft 1.21.1

Status: first static-model/culling adapter implemented; build/runtime acceptance
pending.

Target: Minecraft 1.21.1 / NeoForge 21.1.215.

## 1. Exact NeoForge render call flow

NeoForge 21.1.x BlockRenderDispatcher obtains the original BakedModel and
ModelData, then calls ModelBlockRenderer.tesselateBlock with:
- BlockAndTintGetter;
- BakedModel;
- BlockState;
- physical BlockPos;
- PoseStack / VertexConsumer;
- checkSides;
- RandomSource;
- seed / overlay;
- ModelData;
- RenderType.

ModelBlockRenderer then chooses AO/non-AO and iterates ordinary PHYSICAL
Direction values.

For every physical side it:
1. asks Block.shouldRenderFace using the physical neighbor BlockPos;
2. calls BakedModel.getQuads(state, side, random, modelData, renderType);
3. renders returned BakedQuad objects;
4. AO/light calculations use BakedQuad.getDirection.

Therefore rotating only PoseStack is insufficient:
- getQuads would still be queried with the wrong semantic side;
- culling would compare the wrong state side;
- BakedQuad.direction/normals would disagree with transformed vertices;
- AO would sample as if the quad still faced its canonical local direction.

## 2. Stable model boundary

Planet intercepts only the BakedModel ARGUMENT at entry to the extended
ModelBlockRenderer.tesselateBlock overload.

This is intentionally after BlockRenderDispatcher has obtained the model and
ModelData. The ordinary NeoForge renderer continues to own:
- ModelData;
- RenderType;
- ambient-occlusion selection;
- random seeds;
- tint/color;
- VertexConsumer;
- chunk compilation;
- breaking overlay path.

The replacement is a cached oriented VIEW over the original model, never a
mutation of the registered baked model.

## 3. NeoForge BakedModelWrapper compatibility

NeoForge 1.21.1 BakedModelWrapper delegates:
- ordinary and ModelData-aware getQuads;
- useAmbientOcclusion/TriState;
- particle icon;
- transforms/overrides;
- getModelData;
- block render types;
- item render passes/types.

Planet OrientedModel subclasses BakedModelWrapper and overrides only both
getQuads forms.

This preserves extension behavior for ordinary modded BakedModel
implementations that participate in the standard NeoForge model pipeline.

## 4. Physical side -> local model side

ModelBlockRenderer iterates a PHYSICAL side.

For an oriented model:
    physical requested side
        -> canonical target frame worldToLocal
        -> originalModel.getQuads(local side)

The returned quads represent canonical-local model geometry.

For side=null, the original model is queried with null and all unculled quads
are transformed.

## 5. Quad local -> physical transform

NeoForge QuadTransformers.applying(Transformation) is used rather than manually
rewriting vertex arrays.

The 1.21.1 implementation transforms:
- vertex POSITION;
- packed NORMAL.

UV, tint, lightmap, shade and ambient-occlusion metadata remain intact.

Important: QuadTransformers intentionally does not replace
BakedQuad.direction. Planet therefore:
1. makes the NeoForge transformed copy;
2. computes physical direction from the original local quad direction;
3. creates the final BakedQuad using transformed vertices/normals and the
   physical Direction.

This keeps geometry, packed normal and BakedQuad.direction consistent for AO.

The transformation is derived from PlanetGravityFrame basis:
    world = EAST * localX + UP * localY + SOUTH * localZ
about block center (0.5, 0.5, 0.5).

No second face-specific rotation table is introduced.

## 6. Rendering caches

Two weak-key caches are used:
- original BakedModel -> EnumMap<PlanetFace, oriented BakedModel>;
- original BakedQuad -> EnumMap<PlanetFace, physical BakedQuad>.

POS_Y is identity and returns the original object.

Weak keys allow model reloads/dynamic temporary quad objects to become
collectable. The caches avoid rebuilding the same six orientation views during
chunk rebuilds.

## 7. Why vanilla Block.shouldRenderFace cache is insufficient

Vanilla/NeoForge culling receives:
- source BlockState;
- physical source BlockPos;
- PHYSICAL Direction to neighbor;
- physical neighbor BlockPos.

But Planet state semantics are canonical LOCAL.

At a gravity seam the source side and target side are especially important:
the target local side facing the source is not necessarily
sourceLocalSide.opposite().

Vanilla OCCLUSION_CACHE is keyed by:
    source state + neighbor state + one Direction

It contains no source/target gravity frame. Reusing it with physical Direction
would mix results from different Planet faces.

## 8. Frame-aware culling

PlanetBlockRenderCulling keeps physical neighbor geometry unchanged.

It resolves:
    physical Direction -> source canonical local side
    opposite physical Direction -> target canonical local side

Then it reproduces NeoForge shouldRenderFace semantics:
1. sourceState.skipRendering(neighborState, SOURCE LOCAL side);
2. NeoForge external-face hiding with TARGET LOCAL side;
3. neighbor.canOcclude;
4. source canonical face-occlusion shape using SOURCE LOCAL side;
5. target canonical face-occlusion shape using TARGET LOCAL side;
6. Shapes.joinIsNotEmpty(... ONLY_FIRST).

The existing canonical getOcclusionShape policy is therefore preserved; we do
not rotate cached occlusion shapes into physical state-cache space.

A thread-local LRU cache (2048 entries) is keyed by:
- source BlockState;
- neighbor BlockState;
- source local side;
- target local side.

This is the minimum frame-complete equivalent of vanilla's cache key.

## 9. Render-region frame resolution

Chunk compilation commonly passes a render-region wrapper rather than the
ClientLevel itself.

PlanetClientRenderFrame:
- uses the supplied Level directly when possible;
- otherwise resolves through Minecraft.getInstance().level, which is already
  bound/unbound by PlanetaryClientEvents.

If no active Planet field exists, all model/culling adapters fall back to
ordinary vanilla/NeoForge behavior.

## 10. Tests

PlanetBakedModelRotationTest:
- one asymmetric quad;
- all six PlanetFace frames;
- local vertex -> physical vertex transform around block center;
- local BakedQuad.direction -> physical direction;
- POS_Y identity;
- rotated quad cache identity.

PlanetBlockRenderCullingTest:
- exact POS_X/POS_Y seam;
- physical WEST from source maps to source local DOWN;
- physical EAST toward source maps to target local EAST;
- verifies skipRendering/external hiding/face occlusion receive those distinct
  canonical local sides.

## 11. Deliberate limitations

Not solved by this patch:
- BlockEntityRenderer transforms;
- Flywheel/other custom accelerated render engines that bypass
  ModelBlockRenderer;
- model/state random offset vectors (state.getOffset) whose axes may need an
  explicit local-vs-physical policy;
- fluids/LiquidBlockRenderer;
- particle models;
- item/inventory rendering (there is no world BlockPos/frame);
- environment lighting policy such as whether directional shade should follow
  physical skylight/world Y or local gravity UP;
- custom mod render code that manipulates raw vertices after/beside the standard
  BakedModel pipeline.

## 12. Manual acceptance

Primary:
- bottom slab model must occupy the same physical half-volume as its already
  verified outline/collision on +/-X and +/-Z;
- local-Y log texture/axis must physically point along local UP/DOWN;
- standing torch and wall torch model must match their physical outline/support.

Culling:
- adjacent full cubes must have no holes/flickering internal faces;
- inspect exact gravity edges where neighboring blocks have different canonical
  frames;
- breaking overlay must remain aligned because it uses the same extended
  tesselateBlock path.

Regression:
- POS_Y looks vanilla;
- selection/collision remain correct;
- torch/ladder/lever support removal remains correct;
- movement/edge traversal remains unchanged.


## 13. Grass/mycelium shared-edge surface rendering

Manual acceptance exposed a deterministic green side-overlay rim:
- +/-Z showed it on every face boundary;
- -Y showed it only against +/-X.

This exactly matches the canonical BlockState tie order X -> Y -> Z.

Example:
- a POS_Z/POS_X shared edge block is canonically POS_X;
- its POS_X outward side gets the local-UP grass top;
- its POS_Z outward side is interpreted as a canonical side and therefore gets
  grass_block_side + its green overlay.

Changing canonical tie priority cannot solve this: it only moves the artifact to
the other adjacent face and would destabilize BlockState semantics.

A shared cube-edge surface cell physically has two outward surface normals (and
a cube-corner cell has three), while a normal grass/mycelium model has one
canonical local UP.

Render-only rule:
- canonical BlockState/collision/support frame stays unchanged;
- for SpreadingSnowyDirtBlock only, when candidateFaces.size > 1:
  - if the PHYSICAL side being requested equals local UP of any candidate face,
    query the original model's LOCAL UP quads;
  - rotate those UP quads using that candidate face;
  - all other sides continue through the canonical frame.

Therefore a grass edge has green top on both outward cube surfaces and a grass
corner on all three outward surfaces, without giving slabs/logs/fences or modded
directional blocks multiple semantic orientations.

The seam wrapper is cached by original BakedModel + canonical face + candidate
face mask, with weak values so resource/model reloads remain collectible.

This is intentionally a surface-material rendering rule, not a new generic
BlockState frame policy.
