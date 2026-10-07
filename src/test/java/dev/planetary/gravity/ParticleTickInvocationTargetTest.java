package dev.planetary.gravity;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression for the startup-blocking Mixin INVOKE mismatch.
 *
 * <p>Inherited Particle.move is invoked from each concrete tick method with
 * the concrete tick class as the bytecode invocation owner. This must agree
 * with the six exact @At(target=...) descriptors in the bridge mixins.
 * Compilation of a mixin alone does not verify the target bytecode.</p>
 */
final class ParticleTickInvocationTargetTest {
    private static final List<String> CUSTOM_TICK_OWNERS =
            List.of(
                    "DripParticle",
                    "WaterDropParticle",
                    "BubblePopParticle",
                    "WakeParticle",
                    "CampfireSmokeParticle",
                    "BubbleParticle"
            );

    @Test
    void customTicksInvokeInheritedMoveWithTheirConcreteBytecodeOwner()
            throws IOException {
        for (String particleClass : CUSTOM_TICK_OWNERS) {
            String owner =
                    "net/minecraft/client/particle/" + particleClass;
            ArrayList<String> moveCallOwners =
                    new ArrayList<>();
            boolean[] tickFound = {false};

            try (InputStream bytes =
                         Thread.currentThread()
                                 .getContextClassLoader()
                                 .getResourceAsStream(owner + ".class")) {
                assertTrue(
                        bytes != null,
                        "Missing Minecraft 1.21.1 class " + owner
                );

                new ClassReader(bytes).accept(
                        new ClassVisitor(Opcodes.ASM9) {
                            @Override
                            public MethodVisitor visitMethod(
                                    int access,
                                    String name,
                                    String descriptor,
                                    String signature,
                                    String[] exceptions
                            ) {
                                if (!"tick".equals(name)
                                        || !"()V".equals(descriptor)) {
                                    return null;
                                }

                                tickFound[0] = true;
                                return new MethodVisitor(Opcodes.ASM9) {
                                    @Override
                                    public void visitMethodInsn(
                                            int opcode,
                                            String invokeOwner,
                                            String invokeName,
                                            String invokeDescriptor,
                                            boolean isInterface
                                    ) {
                                        if (opcode == Opcodes.INVOKEVIRTUAL
                                                && "move".equals(invokeName)
                                                && "(DDD)V".equals(
                                                        invokeDescriptor
                                                )) {
                                            moveCallOwners.add(invokeOwner);
                                        }
                                    }
                                };
                            }
                        },
                        ClassReader.SKIP_DEBUG
                                | ClassReader.SKIP_FRAMES
                );
            }

            assertTrue(tickFound[0], particleClass + " lacks tick()");
            assertEquals(
                    List.of(owner),
                    moveCallOwners,
                    particleClass + " changed its tick move invocation; "
                            + "review the matching Mixin @At target"
            );
        }
    }

    @Test
    void allSixBridgesRegisteredAndOldMultiTargetRemoved()
            throws IOException {
        try (InputStream bytes =
                     getClass().getClassLoader().getResourceAsStream(
                             "planetary.mixins.json"
                     )) {
            assertTrue(bytes != null, "Missing mixin registration JSON");
            String json = new String(
                    bytes.readAllBytes(),
                    java.nio.charset.StandardCharsets.UTF_8
            );
            assertFalse(json.contains(
                    "\"SemanticTickDeltaParticleMixin\""
            ));
            for (String name : CUSTOM_TICK_OWNERS) {
                assertTrue(
                        json.contains("\"" + name + "TickDeltaMixin\""),
                        "Missing registered bridge " + name
                );
            }
        }
    }
}
