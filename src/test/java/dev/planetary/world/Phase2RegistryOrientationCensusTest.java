package dev.planetary.world;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Phase-2 research artifact, NOT a "all placements passed" test.
 *
 * <p>Enumerates actual bootstrapped NeoForge 1.21.1 block/item registries.
 * For every registered ID it records the runtime class, inherited
 * implementation owners of orientation-sensitive API methods, and declared
 * BlockState properties/domains. The data is saved as build reports and
 * attached by GitHub CI; candidate rows intentionally remain REVIEW_PENDING
 * until atlas family owners are individually audited.</p>
 *
 * <p>Never loads any class in the reserved dev.planetary.mixin namespace.</p>
 */
final class Phase2RegistryOrientationCensusTest {
    private static final Path REPORT_DIR =
            Path.of(System.getProperty(
                    "planetary.phase2.censusReportDir",
                    "build/reports/planetary"
            ));

    private static final Set<String> ORIENTATION_PROPERTY_NAMES = Set.of(
            "facing", "axis", "horizontal_facing", "orientation",
            "rotation", "attach_face", "face", "half", "hinge",
            "part", "type", "shape", "hanging", "north", "south",
            "east", "west", "up", "down", "vertical_direction",
            "tip_direction", "chest_type", "bed_part", "rail_shape"
    );

    private static final List<String> BLOCK_METHOD_NAMES = List.of(
            "getStateForPlacement",
            "canSurvive",
            "updateShape",
            "neighborChanged",
            "setPlacedBy",
            "onPlace",
            "canBeReplaced",
            "rotate",
            "mirror",
            "useItemOn",
            "useWithoutItem",
            "getShape",
            "getCollisionShape",
            "getFluidState",
            "randomTick",
            "tick"
    );

    private static final List<String> ITEM_METHOD_NAMES = List.of(
            "useOn",
            "use",
            "place",
            "updatePlacementContext",
            "getPlacementState",
            "placeBlock",
            "canPlace",
            "registerBlocks"
    );

    @Test
    void captureActualNeoForgeBlocksItemsAndOrientationOwners()
            throws IOException {
        Bootstrap.bootStrap();

        Files.createDirectories(REPORT_DIR);
        List<String> blocks = new ArrayList<>();
        List<String> properties = new ArrayList<>();
        List<String> items = new ArrayList<>();

        blocks.add("registry_id\tjava_class\thas_orientation_candidate"
                + "\tdeclared_property_names\tmethod_owners"
                + "\taudit_disposition");
        properties.add("registry_id\tproperty_name\tproperty_class"
                + "\tlegal_values\torientation_candidate"
                + "\taudit_disposition");
        items.add("registry_id\tjava_class\tblock_item\tplaced_block"
                + "\tmethod_owners\taudit_disposition");

        int blockCount = 0;
        int itemCount = 0;
        int orientationCandidates = 0;
        Set<String> distinctBlockImplementations = new HashSet<>();
        Set<String> distinctItemImplementations = new HashSet<>();

        for (Block block : BuiltInRegistries.BLOCK) {
            String id = BuiltInRegistries.BLOCK.getKey(block).toString();
            String type = block.getClass().getName();
            distinctBlockImplementations.add(type);
            blockCount++;

            List<String> propertyNames = new ArrayList<>();
            boolean candidate = false;

            for (Property<?> property :
                    block.getStateDefinition().getProperties()) {
                String name = property.getName();
                boolean oriented =
                        ORIENTATION_PROPERTY_NAMES.contains(name);
                candidate |= oriented;
                propertyNames.add(name);
                List<String> values = property.getPossibleValues()
                        .stream().map(Object::toString).sorted().toList();
                properties.add(tsv(id, name,
                        property.getClass().getName(),
                        String.join(",", values),
                        Boolean.toString(oriented),
                        oriented ? "REVIEW_PENDING"
                                : "NOT_CLASSIFIED_AS_ORIENTATION"));
            }

            if (candidate) {
                orientationCandidates++;
            }
            Collections.sort(propertyNames);
            blocks.add(tsv(id, type, Boolean.toString(candidate),
                    String.join(",", propertyNames),
                    owners(block.getClass(), BLOCK_METHOD_NAMES),
                    candidate ? "REVIEW_PENDING"
                            : "NON_PROPERTY_PATH_REVIEW_PENDING"));
        }

        for (Item item : BuiltInRegistries.ITEM) {
            String id = BuiltInRegistries.ITEM.getKey(item).toString();
            String type = item.getClass().getName();
            distinctItemImplementations.add(type);
            itemCount++;
            boolean blockItem = item instanceof BlockItem;
            String blockId = blockItem
                    ? BuiltInRegistries.BLOCK.getKey(
                            ((BlockItem) item).getBlock()).toString()
                    : "-";

            items.add(tsv(id, type, Boolean.toString(blockItem),
                    blockId,
                    owners(item.getClass(), ITEM_METHOD_NAMES),
                    blockItem ? "BLOCKITEM_CREATION_REVIEW_PENDING"
                            : "ITEM_USE_INTERACTION_REVIEW_PENDING"));
        }

        blocks.subList(1, blocks.size()).sort(String::compareTo);
        properties.subList(1, properties.size()).sort(String::compareTo);
        items.subList(1, items.size()).sort(String::compareTo);

        Files.write(REPORT_DIR.resolve(
                "phase2-neo1211-block-registry.tsv"),
                blocks, StandardCharsets.UTF_8);
        Files.write(REPORT_DIR.resolve(
                "phase2-neo1211-state-properties.tsv"),
                properties, StandardCharsets.UTF_8);
        Files.write(REPORT_DIR.resolve(
                "phase2-neo1211-item-registry.tsv"),
                items, StandardCharsets.UTF_8);

        String summary = "Phase2 census: blocks=" + blockCount
                + ", distinctBlockClasses="
                + distinctBlockImplementations.size()
                + ", orientationCandidateBlockIds="
                + orientationCandidates + ", properties="
                + (properties.size() - 1)
                + ", items=" + itemCount
                + ", distinctItemClasses="
                + distinctItemImplementations.size()
                + ". ALL owners REVIEW_PENDING until semantic audit.";

        Files.writeString(REPORT_DIR.resolve(
                "phase2-neo1211-summary.txt"),
                summary + System.lineSeparator(), StandardCharsets.UTF_8);

        assertTrue(blockCount >= 500,
                "Minecraft block registry not initialized: "
                        + blockCount);
        assertTrue(itemCount >= 500,
                "Minecraft item registry not initialized: "
                        + itemCount);
        assertTrue(orientationCandidates >= 100,
                "Orientation census unexpectedly small: "
                        + orientationCandidates);
        assertTrue(distinctBlockImplementations.size() > 50,
                "Superclass/owner census did not see diverse block types");
    }

    private static String owners(Class<?> actualClass,
                                 List<String> methodNames) {
        List<String> result = new ArrayList<>();
        for (String name : methodNames) {
            List<String> descriptions = new ArrayList<>();
            for (Class<?> type = actualClass;
                    type != null && type != Object.class;
                    type = type.getSuperclass()) {
                for (Method method : type.getDeclaredMethods()) {
                    if (method.getName().equals(name)
                            && !method.isSynthetic()
                            && !method.isBridge()) {
                        String signature = Arrays.stream(
                                method.getParameterTypes())
                                .map(Class::getSimpleName)
                                .reduce((a, b) -> a + "," + b)
                                .orElse("");
                        descriptions.add(
                                type.getSimpleName() + "(" + signature
                                        + ")"
                                        + (Modifier.isStatic(
                                                method.getModifiers())
                                                ? ":static" : ""));
                    }
                }
            }
            descriptions.sort(String::compareTo);
            if (!descriptions.isEmpty()) {
                result.add(name + "="
                        + String.join(";", descriptions));
            }
        }
        return String.join("|", result);
    }

    private static String tsv(String... cells) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < cells.length; i++) {
            if (i != 0) {
                line.append('\t');
            }
            line.append(cells[i].replace('\t', ' ')
                    .replace('\n', ' ').replace('\r', ' '));
        }
        return line.toString();
    }
}
