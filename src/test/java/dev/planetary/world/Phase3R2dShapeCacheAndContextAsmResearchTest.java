package dev.planetary.world;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 3R-2d RESEARCH-ONLY: source-vs-real-NeoForge 21.1.215 3A
 * cache/collision/support/occlusion call-site manifest. Does NOT change
 * game/Mixin behavior, certify loaded mods or prove Planet gameplay.
 */
final class Phase3R2dShapeCacheAndContextAsmResearchTest {
    private static final String P = "net/minecraft/";
    private static final String BASE = P + "world/level/block/state/BlockBehaviour$BlockStateBase";
    private static final String CACHE = BASE + "$Cache";
    private static final String SUPPORT = P + "world/level/block/SupportType";
    private static final String GETTER = P + "world/level/BlockGetter";
    private static final String COLLISIONS = P + "world/level/BlockCollisions";
    private static final String CULL = "dev/planetary/client/render/PlanetBlockRenderCulling";
    private static final String SHAPE_RUNTIME = "dev/planetary/world/PlanetBlockShapeRuntime";
    private static final String REPORT = "phase3-neo1211-shape-cache-context-callsite.tsv";

    private record Invoke(String owner, String name, String descriptor) {
    }

    private record Member(String owner, String name, String descriptor,
            List<Invoke> calls, List<Invoke> fields, List<String> typeUses) {
    }

    private static List<Member> scan(String owner) throws IOException {
        List<Member> result = new ArrayList<>();
        try (InputStream input = Thread.currentThread()
                .getContextClassLoader().getResourceAsStream(owner + ".class")) {
            assertNotNull(input, "Missing compiled NeoForge class " + owner);
            new ClassReader(input).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public MethodVisitor visitMethod(int access, String name,
                        String descriptor, String signature, String[] exceptions) {
                    List<Invoke> calls = new ArrayList<>();
                    List<Invoke> fields = new ArrayList<>();
                    List<String> types = new ArrayList<>();
                    result.add(new Member(owner, name, descriptor, calls, fields, types));
                    return new MethodVisitor(Opcodes.ASM9) {
                        @Override
                        public void visitMethodInsn(int opcode, String target,
                                String method, String desc, boolean isInterface) {
                            calls.add(new Invoke(target, method, desc));
                        }

                        @Override
                        public void visitFieldInsn(int opcode, String target,
                                String field, String desc) {
                            fields.add(new Invoke(target, field, desc));
                        }

                        @Override
                        public void visitTypeInsn(int opcode, String type) {
                            types.add(type);
                        }
                    };
                }
            }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        }
        return result;
    }

    private static Member one(List<Member> methods, String name, String desc) {
        return methods.stream().filter(m -> m.name().equals(name)
                && (desc == null || m.descriptor().equals(desc))).findFirst()
                .orElseThrow(() -> new AssertionError("Missing original ABI "
                        + name + (desc == null ? "" : desc)));
    }

    private static boolean called(Member m, String name) {
        return m.calls().stream().anyMatch(c -> c.name().equals(name));
    }

    private static boolean field(Member m, String name) {
        return m.fields().stream().anyMatch(f -> f.name().equals(name));
    }

    @Test
    void originalCompiledStateCacheAndSupportCallsitesAreDistinct()
            throws IOException {
        List<Member> base = scan(BASE);
        List<Member> cache = scan(CACHE);
        List<Member> support = scan(SUPPORT);
        String getterPos = "(Lnet/minecraft/world/level/BlockGetter;"
                + "Lnet/minecraft/core/BlockPos;";
        String voxel = ")Lnet/minecraft/world/phys/shapes/VoxelShape;";

        Member initCache = one(base, "initCache", "()V");
        assertTrue(called(initCache, "hasDynamicShape"));
        assertTrue(called(initCache, "<init>"), "Cache constructor owner must be present");

        Member face = one(base, "getFaceOcclusionShape",
                getterPos + "Lnet/minecraft/core/Direction;" + voxel);
        assertTrue(field(face, "cache"));
        assertTrue(called(face, "getOcclusionShape"));
        assertTrue(called(face, "getFaceShape"));

        Member simpleCollision = one(base, "getCollisionShape", getterPos + voxel);
        assertTrue(field(simpleCollision, "cache"));
        assertTrue(called(simpleCollision, "getCollisionShape"));

        Member sturdy = one(base, "isFaceSturdy",
                getterPos + "Lnet/minecraft/core/Direction;"
                + "Lnet/minecraft/world/level/block/SupportType;)Z");
        assertTrue(field(sturdy, "cache"));
        assertTrue(called(sturdy, "isSupporting"));
        assertTrue(called(sturdy, "isFaceSturdy"));

        Member ctor = one(cache, "<init>", null);
        assertTrue(field(ctor, "INSTANCE"), "EmptyBlockGetter.INSTANCE provenance");
        assertTrue(field(ctor, "ZERO"), "BlockPos.ZERO provenance");
        assertTrue(called(ctor, "getOcclusionShape"));
        assertTrue(called(ctor, "getCollisionShape"));
        assertTrue(called(ctor, "isSupporting"));
        assertTrue(called(ctor, "getFaceShape"));

        Member supportMethod = one(support, "values", null);
        assertNotNull(supportMethod);

        List<Member> physical = scan(COLLISIONS);
        assertTrue(physical.stream().anyMatch(m -> called(m, "getCollisionShape")));
        assertTrue(physical.stream().anyMatch(m -> called(m, "joinIsNotEmpty")));

        List<Member> getter = scan(GETTER);
        assertTrue(getter.stream().anyMatch(m -> called(m, "getInteractionShape")));
        assertTrue(getter.stream().anyMatch(m -> called(m, "getBlockShape")));

        List<Member> planetCull = scan(CULL);
        Member shouldRender = one(planetCull, "shouldRenderFace", null);
        assertTrue(called(shouldRender, "getFaceOcclusionShape"));
        assertTrue(called(shouldRender, "worldToLocal"));
        assertTrue(field(shouldRender, "OCCLUSION_CACHE"));

        Member finish = one(scan(SHAPE_RUNTIME), "finishPhysical", null);
        assertTrue(finish.typeUses().contains(P + "world/level/Level"),
                "Planet shape transformation currently requires real Level");

        // A finite, human-auditable manifest, not a bulk bytecode diff.
        List<String> lines = new ArrayList<>();
        lines.add("declaring_class\tmethod\tdescriptor\tmethod_calls\tfield_uses\ttype_references"
                + "\tevidence_scope");
        for (Member m : List.of(initCache, face, simpleCollision, sturdy, ctor,
                shouldRender, finish)) {
            lines.add(String.join("\t", m.owner(), m.name(), m.descriptor(),
                    m.calls().stream().map(c -> c.owner() + "#" + c.name())
                            .distinct().sorted().reduce((a, b) -> a + ";" + b).orElse("-"),
                    m.fields().stream().map(f -> f.owner() + "#" + f.name())
                            .distinct().sorted().reduce((a, b) -> a + ";" + b).orElse("-"),
                    m.typeUses().stream().distinct().sorted()
                            .reduce((a, b) -> a + ";" + b).orElse("-"),
                    "TARGET_NEOFORGE_21_1_215_SELECTED_COMPILED_ABI"));
        }
        Path dest = Path.of(System.getProperty("planetary.phase2.censusReportDir",
                "build/reports/planetary")).resolve(REPORT);
        Files.createDirectories(dest.getParent());
        Files.write(dest, lines, StandardCharsets.UTF_8);
        assertEquals(8, lines.size());
    }
}
