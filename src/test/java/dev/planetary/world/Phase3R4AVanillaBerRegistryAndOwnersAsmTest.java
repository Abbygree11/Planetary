package dev.planetary.world;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Planetary 3R-4A / 3C registry census. Enumerate exact original
 * Minecraft1.21.1 / NeoForge21.1.215 BlockEntityRenderers.<clinit> factory
 * INVOKEDYNAMIC handles and all registered original renderer classfiles.
 * Never infer registration from only class filenames or a comparator source.
 * This is static original bytecode; it is not an applied renderer/Mixin or
 * a dynamically loaded modded client.
 */
final class Phase3R4AVanillaBerRegistryAndOwnersAsmTest {
    private static final String BASE = "net/minecraft/client/renderer/blockentity/";
    private static final String BLOCK_ENTITY = "net/minecraft/world/level/block/entity/";
    private static final String REGISTRY = BASE + "BlockEntityRenderers";
    private static final String TYPE = BLOCK_ENTITY + "BlockEntityType";

    private static final List<String> EXPECTED_IDS = List.of(
            "SIGN", "HANGING_SIGN", "MOB_SPAWNER", "PISTON",
            "CHEST", "ENDER_CHEST", "TRAPPED_CHEST", "ENCHANTING_TABLE",
            "LECTERN", "END_PORTAL", "END_GATEWAY", "BEACON",
            "SKULL", "BANNER", "STRUCTURE_BLOCK", "SHULKER_BOX",
            "BED", "CONDUIT", "BELL", "CAMPFIRE",
            "BRUSHABLE_BLOCK", "DECORATED_POT", "TRIAL_SPAWNER", "VAULT"
    );
    private static final List<String> EXPECTED_RENDERERS = List.of(
            "SignRenderer", "HangingSignRenderer", "SpawnerRenderer",
            "PistonHeadRenderer", "ChestRenderer", "ChestRenderer",
            "ChestRenderer", "EnchantTableRenderer", "LecternRenderer",
            "TheEndPortalRenderer", "TheEndGatewayRenderer", "BeaconRenderer",
            "SkullBlockRenderer", "BannerRenderer", "StructureBlockRenderer",
            "ShulkerBoxRenderer", "BedRenderer", "ConduitRenderer",
            "BellRenderer", "CampfireRenderer", "BrushableBlockRenderer",
            "DecoratedPotRenderer", "TrialSpawnerRenderer", "VaultRenderer"
    );

    private record Registration(int ordinal, String type, String factoryOwner,
                                String constructorDesc, String bootstrap) {
        String tsv() {
            return ordinal + "\t" + type + "\t" + factoryOwner + "\t"
                    + constructorDesc + "\t" + bootstrap;
        }
    }
    private record Owner(String classfile, String sha, String parent,
                         String interfaces, boolean present) {
        String tsv() {
            return String.join("\t", classfile, sha, parent, interfaces,
                    Boolean.toString(present));
        }
    }
    private record Method(String owner, String name, String desc, int access) {
        String tsv() {
            return String.join("\t", owner, name, desc, Integer.toString(access));
        }
    }
    private record Field(String owner, String name, String desc, int access) {
        String tsv() {
            return String.join("\t", owner, name, desc, Integer.toString(access));
        }
    }
    private record Invoke(String owner, String method, String desc,
                          String target, String targetName, String targetDesc,
                          int opcode) {
        String tsv() {
            return String.join("\t", owner, method, desc, target, targetName,
                    targetDesc, Integer.toString(opcode));
        }
    }

    @Test
    void allOriginalRegisteredBerTypesAndRendererOwnersAreMapped() throws Exception {
        List<Registration> registration = new ArrayList<>();
        List<Owner> owners = new ArrayList<>();
        List<Method> methods = new ArrayList<>();
        List<Field> fields = new ArrayList<>();
        List<Invoke> invokes = new ArrayList<>();
        List<String> staticTypes = new ArrayList<>();
        List<String> rendererFactories = new ArrayList<>();
        List<String> factoryDescriptors = new ArrayList<>();
        List<String> bootstrapMethods = new ArrayList<>();

        byte[] registerClass = load(REGISTRY);
        new ClassReader(registerClass).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override public MethodVisitor visitMethod(int access, String method,
                    String desc, String signature, String[] exceptions) {
                if (!method.equals("<clinit>")) return null;
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override public void visitFieldInsn(int opcode, String owner,
                            String name, String desc) {
                        if (opcode == Opcodes.GETSTATIC && owner.equals(TYPE)) {
                            staticTypes.add(name);
                        }
                    }
                    @Override public void visitInvokeDynamicInsn(String name,
                            String desc, Handle bootstrap, Object... arguments) {
                        List<Handle> constructors = new ArrayList<>();
                        for (Object arg : arguments) {
                            if (arg instanceof Handle handle
                                    && handle.getName().equals("<init>")
                                    && handle.getOwner().startsWith(BASE)
                                    && handle.getTag() == Opcodes.H_NEWINVOKESPECIAL) {
                                constructors.add(handle);
                            }
                        }
                        assertEquals(1, constructors.size(),
                                "Every BlockEntityRenderer register factory must "
                                + "point to one concrete renderer constructor");
                        Handle chosen = constructors.getFirst();
                        rendererFactories.add(chosen.getOwner());
                        factoryDescriptors.add(chosen.getDesc());
                        bootstrapMethods.add(bootstrap.getOwner() + "."
                                + bootstrap.getName());
                    }
                    @Override public void visitMethodInsn(int opcode, String owner,
                            String name, String desc, boolean isInterface) {
                        if (opcode == Opcodes.INVOKESTATIC
                                && owner.equals(REGISTRY) && name.equals("register")) {
                            assertEquals(staticTypes.size(), rendererFactories.size(),
                                    "Factory chain must be one ordered block entity "
                                    + "type per original renderer constructor");
                            int index = registration.size();
                            assertEquals(index + 1, staticTypes.size(),
                                    "Original <clinit> registration must not drop any type");
                            registration.add(new Registration(index,
                                    staticTypes.get(index), rendererFactories.get(index),
                                    factoryDescriptors.get(index),
                                    bootstrapMethods.get(index)));
                        }
                    }
                };
            }
        }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

        assertEquals(24, registration.size(),
                "Actual original vanilla BER registry entries changed: re-audit");
        assertEquals(EXPECTED_IDS, registration.stream()
                .map(Registration::type).toList(),
                "Minecraft 1.21.1 expected renderer registration ordering changed");
        assertEquals(EXPECTED_RENDERERS, registration.stream()
                .map(x -> x.factoryOwner().substring(BASE.length())).toList(),
                "Registered original provider-to-renderer family map changed");
        assertEquals(22, registration.stream()
                .map(Registration::factoryOwner).distinct().count(),
                "22 unique original vanilla BER owners; three chest registrations");
        Set<String> names = new HashSet<>(registration.stream()
                .map(Registration::factoryOwner).toList());
        names.addAll(List.of(REGISTRY, BASE + "BlockEntityRenderer",
                BASE + "BlockEntityRendererProvider",
                BASE + "BlockEntityRendererProvider$Context",
                BASE + "BlockEntityRenderDispatcher",
                BASE + "SignRenderer$SignModel"));
        for (String name : names) {
            byte[] bytes = load(name);
            String sha = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(bytes));
            new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override public void visit(int version, int access, String owner,
                        String signature, String parent, String[] ifaces) {
                    owners.add(new Owner(owner, sha, parent == null ? "" : parent,
                            String.join(",", ifaces == null
                                    ? new String[0] : ifaces), true));
                }
                @Override public FieldVisitor visitField(int access, String field,
                        String desc, String signature, Object value) {
                    fields.add(new Field(name, field, desc, access));
                    return null;
                }
                @Override public MethodVisitor visitMethod(int access, String method,
                        String desc, String signature, String[] exceptions) {
                    methods.add(new Method(name, method, desc, access));
                    return new MethodVisitor(Opcodes.ASM9) {
                        @Override public void visitMethodInsn(int opcode, String target,
                                String member, String descriptor, boolean itf) {
                            invokes.add(new Invoke(name, method, desc,
                                    target, member, descriptor, opcode));
                        }
                    };
                }
            }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        }
        assertEquals(names.size(), owners.size(),
                "All exact expected original BER source classes present");
        for (String renderer : registration.stream().map(Registration::factoryOwner)
                .distinct().toList()) {
            assertTrue(methods.stream().anyMatch(x -> x.owner().equals(renderer)
                    && x.name().equals("render")),
                    "Registered original renderer lacks render method: " + renderer);
            assertTrue(methods.stream().anyMatch(x -> x.owner().equals(renderer)
                    && x.name().equals("<init>")),
                    "Registered original renderer lacks factory constructor: " + renderer);
        }
        assertTrue(invokes.stream().anyMatch(x ->
                        x.owner().equals(BASE + "SignRenderer")
                        && x.name().equals("render")
                        && x.targetName().equals("renderSignText")),
                "Original sign render must own a separate text rendering path");
        assertTrue(invokes.stream().anyMatch(x ->
                        x.owner().equals(BASE + "HangingSignRenderer")
                        && x.name().equals("render")
                        && x.targetName().equals("renderSign")),
                "Original hanging-sign render must delegate sign geometry");

        owners.sort(Comparator.comparing(Owner::classfile));
        fields.sort(Comparator.comparing(Field::owner).thenComparing(Field::name));
        methods.sort(Comparator.comparing(Method::owner).thenComparing(Method::name)
                .thenComparing(Method::desc));
        invokes.sort(Comparator.comparing(Invoke::owner)
                .thenComparing(Invoke::method).thenComparing(Invoke::desc)
                .thenComparing(Invoke::target).thenComparing(Invoke::targetName)
                .thenComparing(Invoke::targetDesc).thenComparingInt(Invoke::opcode));

        Path report = Path.of(System.getProperty("planetary.phase2.censusReportDir",
                "build/reports/planetary"));
        Files.createDirectories(report);
        List<String> rs = new ArrayList<>(List.of(
                "ordinal\tblock_entity_type\tconstructor_owner\tconstructor_desc\tbootstrap"));
        registration.stream().map(Registration::tsv).forEach(rs::add);
        Files.write(report.resolve("phase3-r4a-original-ber-registrations.tsv"),
                rs, StandardCharsets.UTF_8);
        List<String> os = new ArrayList<>(List.of(
                "owner\tclass_sha256\tsuperclass\tinterfaces\tpresent"));
        owners.stream().map(Owner::tsv).forEach(os::add);
        Files.write(report.resolve("phase3-r4a-original-ber-owners.tsv"),
                os, StandardCharsets.UTF_8);
        List<String> ms = new ArrayList<>(List.of(
                "owner\tmethod\tjvm_descriptor\taccess"));
        methods.stream().map(Method::tsv).forEach(ms::add);
        Files.write(report.resolve("phase3-r4a-original-ber-methods.tsv"),
                ms, StandardCharsets.UTF_8);
        List<String> fs = new ArrayList<>(List.of(
                "owner\tfield\tjvm_descriptor\taccess"));
        fields.stream().map(Field::tsv).forEach(fs::add);
        Files.write(report.resolve("phase3-r4a-original-ber-fields.tsv"),
                fs, StandardCharsets.UTF_8);
        List<String> is = new ArrayList<>(List.of(
                "owner\tmethod\tjvm_descriptor\ttarget_owner\ttarget_method\t"
                + "target_descriptor\topcode"));
        invokes.stream().map(Invoke::tsv).forEach(is::add);
        Files.write(report.resolve("phase3-r4a-original-ber-invokes.tsv"),
                is, StandardCharsets.UTF_8);
        Files.writeString(report.resolve("phase3-r4a-original-ber-summary.txt"),
                "target=Minecraft1.21.1-NeoForge21.1.215\n"
                + "stage=3R-4A\n"
                + "source=original_compiled_BlockEntityRenderers_clinit_constructor_factories\n"
                + "registration_count=" + registration.size() + "\n"
                + "distinct_renderer_factories=" + registration.stream()
                    .map(Registration::factoryOwner).distinct().count() + "\n"
                + "original_owner_classfiles=" + owners.size() + "\n"
                + "method_declarations=" + methods.size() + "\n"
                + "field_declarations=" + fields.size() + "\n"
                + "direct_invokes=" + invokes.size() + "\n"
                + "scope=registration_and_owner_classfile_jvm_not_loaded_mods_game\n",
                StandardCharsets.UTF_8);
    }

    private static byte[] load(String name) throws Exception {
        try (InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(name + ".class")) {
            assertNotNull(in, "Missing exact target JVM original classfile: " + name);
            return in.readAllBytes();
        }
    }
}
