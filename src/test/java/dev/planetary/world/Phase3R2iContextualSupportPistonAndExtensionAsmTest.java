package dev.planetary.world;

import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.SupportType;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 3R-2i RESEARCH ONLY: actual NeoForge 21.1.215 compiled signatures and
 * invocations at contextual support / piston / CollisionGetter / extension
 * boundaries. Static JVM callsites alone do NOT resolve runtime dynamic
 * dispatch, mods, gameplay, or frame provenance.
 */
final class Phase3R2iContextualSupportPistonAndExtensionAsmTest {
    private static final String N = "net/minecraft/";
    private static final String F = "net/neoforged/neoforge/";
    private static final String SUPPORT = N + "world/level/block/SupportType";
    private static final String PISTON = N + "world/level/block/piston/PistonMovingBlockEntity";
    private static final String COLLISION_GETTER = N + "world/level/CollisionGetter";
    private static final String EXTENSION = F + "common/extensions/IBlockStateExtension";

    private static final List<String> OWNER_CLASSES = List.of(
            SUPPORT, SUPPORT + "$1", SUPPORT + "$2", SUPPORT + "$3",
            PISTON,
            N + "world/level/block/piston/MovingPistonBlock",
            N + "world/level/CollisionGetter",
            N + "world/level/BlockCollisions",
            N + "world/level/border/WorldBorder",
            N + "world/level/border/WorldBorder$BorderExtent",
            EXTENSION,
            N + "world/level/block/Block",
            N + "world/level/block/state/BlockState",
            N + "world/level/block/state/BlockBehaviour$BlockStateBase",
            N + "world/level/BlockGetter",
            N + "world/phys/shapes/CollisionContext",
            N + "world/phys/shapes/EntityCollisionContext",
            N + "world/phys/shapes/Shapes"
    );

    private static final Set<String> SELECTED_METHODS = Set.of(
            "isSupporting", "isFaceSturdy", "getBlockSupportShape",
            "getFaceShape", "isFaceFull", "joinIsNotEmpty",
            "getCollisionShape", "getShape", "getVisualShape",
            "getInteractionShape", "getOcclusionShape", "getFaceOcclusionShape",
            "getBlockFloorHeight", "isUnobstructed", "noCollision",
            "getBlockState", "getBlockEntity", "getMovedState",
            "getProgress", "moveCollidedEntities", "moveStuckEntities",
            "getMovement", "getCollisionContext", "hasDynamicShape",
            "hidesNeighborFace", "supportsExternalFaceHiding",
            "getPistonPos", "getOffset", "skipRendering"
    );

    private record Method(String owner, String method, String descriptor, int access) {
        String line() {
            return String.join("\t", owner, method, descriptor,
                    Integer.toString(access));
        }
    }
    private record Invoke(String sourceOwner, String sourceMethod, String sourceDescriptor,
                          String targetOwner, String targetMethod, String targetDescriptor, int opcode) {
        String line() {
            return String.join("\t", sourceOwner, sourceMethod, sourceDescriptor,
                    targetOwner, targetMethod, targetDescriptor, Integer.toString(opcode));
        }
    }
    private record Owner(String internalName, String superName, String interfaces,
                         int access) {
        String line() {
            return String.join("\t", internalName,
                    superName == null ? "" : superName, interfaces,
                    Integer.toString(access));
        }
    }

    @Test
    void exactOriginalSupportPistonCollisionGetterAndNeoForgeExtensionBoundaries()
            throws Exception {
        Bootstrap.bootStrap();
        assertEquals(3, SupportType.values().length,
                "SupportType enum size differs from pinned NeoForge21.1.215");
        assertEquals(Set.of("FULL", "CENTER", "RIGID"),
                Set.of(SupportType.FULL.name(), SupportType.CENTER.name(), SupportType.RIGID.name()));
        List<Owner> owners = new ArrayList<>();
        List<Method> methods = new ArrayList<>();
        List<Invoke> invokes = new ArrayList<>();
        for (String owner : OWNER_CLASSES) {
            try (InputStream input = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(owner + ".class")) {
                assertNotNull(input, "Missing exact compiled class " + owner);
                new ClassReader(input).accept(new ClassVisitor(Opcodes.ASM9) {
                    @Override
                    public void visit(int version, int access, String name, String signature,
                                      String superName, String[] interfaces) {
                        owners.add(new Owner(name, superName,
                                String.join(",", interfaces == null ? new String[0] : interfaces),
                                access));
                    }
                    @Override
                    public MethodVisitor visitMethod(int access, String name, String desc,
                                                     String signature, String[] exceptions) {
                        methods.add(new Method(owner, name, desc, access));
                        return new MethodVisitor(Opcodes.ASM9) {
                            @Override
                            public void visitMethodInsn(int opcode, String target,
                                                        String called, String descriptor,
                                                        boolean isInterface) {
                                if (SELECTED_METHODS.contains(called)) {
                                    invokes.add(new Invoke(owner, name, desc,
                                            target, called, descriptor, opcode));
                                }
                            }
                        };
                    }
                }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            }
        }

        for (int i = 1; i <= 3; i++) {
            String subclass = SUPPORT + "$" + i;
            assertTrue(owners.stream().anyMatch(x ->
                    x.internalName().equals(subclass) && SUPPORT.equals(x.superName())));
            assertTrue(methods.stream().anyMatch(x ->
                    x.owner().equals(subclass) && x.method().equals("isSupporting")),
                    "Missing concrete strategy method: " + subclass);
            assertTrue(invokes.stream().anyMatch(x ->
                    x.sourceOwner().equals(subclass)
                            && x.sourceMethod().equals("isSupporting")
                            && x.targetMethod().equals("getBlockSupportShape")));
        }
        assertTrue(invokes.stream().anyMatch(x ->
                x.sourceOwner().equals(SUPPORT + "$1")
                        && x.targetMethod().equals("isFaceFull")));
        for (int i = 2; i <= 3; i++) {
            String subclass = SUPPORT + "$" + i;
            assertTrue(invokes.stream().anyMatch(x ->
                    x.sourceOwner().equals(subclass)
                            && x.targetMethod().equals("joinIsNotEmpty")));
            assertTrue(invokes.stream().anyMatch(x ->
                    x.sourceOwner().equals(subclass)
                            && x.targetMethod().equals("getFaceShape")));
        }

        assertTrue(methods.stream().anyMatch(x ->
                x.owner().equals(PISTON) && x.method().equals("getCollisionShape")));
        for (String method : List.of("getCollisionShape",
                "moveCollidedEntities", "moveStuckEntities")) {
            assertTrue(invokes.stream().anyMatch(x ->
                    x.sourceOwner().equals(PISTON)
                            && x.sourceMethod().equals(method)
                            && x.targetMethod().equals("getCollisionShape")),
                    "Moving piston must consume collision shapes: " + method);
        }
        assertTrue(invokes.stream().anyMatch(x ->
                x.sourceOwner().equals(COLLISION_GETTER)
                        && x.sourceMethod().equals("isUnobstructed")
                        && x.targetMethod().equals("getCollisionShape")));
        assertTrue(invokes.stream().anyMatch(x ->
                x.sourceOwner().equals(EXTENSION)
                        && x.sourceMethod().equals("hidesNeighborFace")
                        && x.targetMethod().equals("hidesNeighborFace")));
        assertTrue(invokes.stream().anyMatch(x ->
                x.sourceOwner().equals(EXTENSION)
                        && x.sourceMethod().equals("supportsExternalFaceHiding")
                        && x.targetMethod().equals("supportsExternalFaceHiding")));

        Comparator<Invoke> invokeOrder = Comparator.comparing(Invoke::sourceOwner)
                .thenComparing(Invoke::sourceMethod).thenComparing(Invoke::sourceDescriptor)
                .thenComparing(Invoke::targetOwner).thenComparing(Invoke::targetMethod)
                .thenComparing(Invoke::targetDescriptor).thenComparingInt(Invoke::opcode);
        invokes.sort(invokeOrder);
        methods.sort(Comparator.comparing(Method::owner)
                .thenComparing(Method::method).thenComparing(Method::descriptor));
        owners.sort(Comparator.comparing(Owner::internalName));

        Path reports = Path.of(System.getProperty(
                "planetary.phase2.censusReportDir", "build/reports/planetary"));
        Files.createDirectories(reports);
        List<String> ownerLines = new ArrayList<>(List.of(
                "owner\tsuperclass\tinterfaces\taccess"));
        owners.stream().map(Owner::line).forEach(ownerLines::add);
        Files.write(reports.resolve("phase3-r2i-context-owner-inheritance.tsv"),
                ownerLines, StandardCharsets.UTF_8);
        List<String> methodLines = new ArrayList<>(List.of(
                "owner\tmethod\tjvm_descriptor\taccess"));
        methods.stream().map(Method::line).forEach(methodLines::add);
        Files.write(reports.resolve("phase3-r2i-context-owner-methods.tsv"),
                methodLines, StandardCharsets.UTF_8);
        List<String> invokeLines = new ArrayList<>(List.of(
                "source_owner\tsource_method\tsource_descriptor\ttarget_owner"
                + "\ttarget_method\ttarget_descriptor\tinvoke_opcode"));
        invokes.stream().map(Invoke::line).forEach(invokeLines::add);
        Files.write(reports.resolve("phase3-r2i-context-owner-invokes.tsv"),
                invokeLines, StandardCharsets.UTF_8);
        long supportCalls = invokes.stream().filter(x ->
                x.sourceOwner().startsWith(SUPPORT + "$")
                        && x.sourceMethod().equals("isSupporting")).count();
        long pistonCalls = invokes.stream().filter(x ->
                x.sourceOwner().equals(PISTON)
                        && x.targetMethod().equals("getCollisionShape")).count();
        Files.writeString(reports.resolve("phase3-r2i-context-owner-summary.txt"),
                "minecraftVersion=1.21.1\nneoForgeVersion=21.1.215\n"
                + "researchOnly=true\n"
                + "supportEnumValues=3\n"
                + "ownerClassfiles=" + owners.size() + "\n"
                + "declaredMethods=" + methods.size() + "\n"
                + "selectedInvocationEdges=" + invokes.size() + "\n"
                + "nestedSupportStrategySelectedInvocations=" + supportCalls + "\n"
                + "pistonShapeInvocations=" + pistonCalls + "\n"
                + "coverage=18_selected_original_compiled_owners_not_all_dynamic_dispatch\n"
                + "explicitGaps=custom_mod_overrides,java_reflection,invokedynamic,"
                + "actual_context_provenance,client_Mixins,gameplay\n",
                StandardCharsets.UTF_8);
    }
}
