package dev.planetary.world;

import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 3R-7 research gate integrity only: cross-check historical 151-row owner
 * ledger against explicit per-row research/source/visual disposition.
 * Does NOT prove Minecraft original JVM bytecode, target mod ABI, loaded
 * Create/Flywheel/Iris clients, game render behavior or gate R/V PASS.
 */
final class Phase3R7WholePhaseOwnerGateAuditTest {
    private static final String LEDGER =
        "docs/research/PHASE3_OWNER_COVERAGE_1_21_1.tsv";
    private static final String AUDIT =
        "docs/research/PHASE3_R7_ALL_151_OWNER_SOURCE_GATE_DISPOSITION_1_21_1.tsv";
    private static final Map<String,Integer> PHASE_COUNTS = Map.of(
        "3A",41,"3B",25,"3C",48,"3D",15,"3E",22);
    private static final Map<String,Set<String>> R_SOURCE_BLOCKERS = Map.of(
        "G01",Set.of("B05"),
        "G02",Set.of("C05"),
        "G03",Set.of("A17","A35","B11"),
        "G04",Set.of("A25"),
        "G05",Set.of("D10","D11","E20"),
        "G06",Set.of("E03","E10"));
    private static final Set<String> OPTIONAL_SHADER_ROWS = Set.of("D14","E22");
    private static final Set<String> PHASE_REPORTS = Set.of(
        "docs/phases/phase-01/results/README.md",
        "docs/phases/phase-02/results/README.md",
        "docs/phases/phase-03/results/README.md",
        "docs/phases/phase-03/results/3a-shapes.md",
        "docs/phases/phase-03/results/3b-baked-models.md",
        "docs/phases/phase-03/results/3c-block-entity-renderers.md",
        "docs/phases/phase-03/results/3d-alternate-pipelines.md",
        "docs/phases/phase-03/results/3e-client-lifecycle.md",
        "docs/research/PHASE3_FULL_OWNER_AUDIT_GATE_1_21_1.md",
        "docs/research/PHASE2_VISUAL_READINESS_GATE_1_21_1.md",
        "docs/research/PHASE3_R7_WHOLE_PHASE_OWNER_SOURCE_GATE_RECONCILIATION_1_21_1.md",
        "docs/research/PHASE3_R5_INDEPENDENT_SECTION_GEOMETRY_CREATE_FLYWHEEL_SHADER_SOURCE_1_21_1.md",
        "docs/research/PHASE3_R6_CLIENT_RESOURCE_RELOAD_WORKER_EPOCH_MOD_1_21_1.md");
    @Test void verifyResearchOwnerGateHasNoImplicitPassAndEveryOriginalRecordIsDispositioned() throws Exception {
        String path = System.getProperty("planetary.phase2.censusReportDir");
        assertNotNull(path, "Use the absolute NeoForge census report directory configured in Gradle");
        Path output = Path.of(path).toAbsolutePath().normalize();
        Path root = output.getParent().getParent().getParent();
        assertTrue(Files.isRegularFile(root.resolve(LEDGER)), "Cannot find original owner ledger in project root: "+root);
        assertTrue(Files.isRegularFile(root.resolve(AUDIT)), "Cannot find owner research gate matrix");
        List<String[]> original = tsv(root.resolve(LEDGER), 7);
        List<String[]> dispositions = tsv(root.resolve(AUDIT), 10);
        assertEquals(151,original.size(),"Historical 151-row selected source inventory must never silently shrink");
        assertEquals(151,dispositions.size(),"One full gate disposition for every historical owner row");
        Map<String,String[]> prior = new LinkedHashMap<>();
        Map<String,Integer> counts = new TreeMap<>();
        for(String[] r : original) {
            String key=r[0]+"/"+r[1];
            assertNull(prior.put(key,r),"Duplicate original owner: "+key);
            counts.merge(r[0],1,Integer::sum);
            for(String cell:r)assertFalse(cell.isBlank(),"Blank original value in "+key);
        }
        assertEquals(PHASE_COUNTS,counts,"Coverage must include all five phase 3 components");
        Set<String> seen=new HashSet<>();
        Map<String,Set<String>> blockers = new TreeMap<>();
        Set<String> shader=new HashSet<>();
        Map<String,Integer> decisions=new TreeMap<>();
        List<String> actualBlockers=new ArrayList<>();
        for(String[] r:dispositions) {
            String key=r[0]+"/"+r[1];
            assertTrue(seen.add(key),"Duplicate disposition: "+key);
            String[] old=prior.get(key);
            assertNotNull(old,"Extra owner not found in the original ledger: "+key);
            assertEquals(old[2],r[2],"Owner meaning must not be silently rewritten: "+key);
            assertEquals(old[3],r[3],"Original source-evidence status must be preserved: "+key);
            assertEquals(old[6],r[9],"Original gap must not be deleted: "+key);
            assertFalse(r[4].isBlank(),"Missing evidence grade in "+key);
            assertFalse(r[5].isBlank(),"Missing R/V disposition in "+key);
            assertFalse(r[7].isBlank(),"Missing next-stage disposition in "+key);
            assertTrue(Files.isRegularFile(root.resolve("docs/research").resolve(r[8])),
                "Selected source anchor absent for "+key+": "+r[8]);
            assertFalse(r[5].equals("R_PASS")||r[5].equals("V_PASS"),
                "Audit cannot silently approve source or live game gate: "+key);
            decisions.merge(r[5],1,Integer::sum);
            if(r[5].equals("BLOCKS_R_SOURCE_GATE")) {
                assertFalse(r[6].isBlank(),"Source blocker needs named group: "+key);
                assertEquals("3R-7F",r[7],"Blocked work must not defer to game-only gate: "+key);
                blockers.computeIfAbsent(r[6],unused->new HashSet<>()).add(r[1]);
                actualBlockers.add(r[0]+"\t"+r[1]+"\t"+r[6]+"\t"+r[2]+"\t"+r[9]);
            }
            if(r[5].equals("OPTIONAL_SHADER_SOURCE_SCOPE_UNVERIFIED")) {
                assertEquals("G07",r[6],"Optional shader source must be scoped separately");
                shader.add(r[1]);
            }
        }
        assertEquals(prior.keySet(),seen);
        assertEquals(R_SOURCE_BLOCKERS,blockers,"Six exact blocking owner clusters cannot be waived by a text-only audit");
        assertEquals(OPTIONAL_SHADER_ROWS,shader,"Iris/Sodium shader source is not a baseline game or ABI PASS");
        assertEquals(11,decisions.get("BLOCKS_R_SOURCE_GATE"));
        assertEquals(2,decisions.get("OPTIONAL_SHADER_SOURCE_SCOPE_UNVERIFIED"));
        assertEquals(18,decisions.get("SUPERSEDED_OR_STALE_LEDGER_RECONCILE"));
        assertEquals(74,decisions.get("SELECTED_SOURCE_EVIDENCE_RUNTIME_GAP"));
        assertEquals(46,decisions.get("SELECTED_SOURCE_NEEDS_FINAL_GUARD"));
        for(String report:PHASE_REPORTS) {
            assertTrue(Files.isRegularFile(root.resolve(report)),"Missing permanent research output: "+report);
        }
        Files.createDirectories(output);
        String oldHash=sha(Files.readAllBytes(root.resolve(LEDGER)));
        String newHash=sha(Files.readAllBytes(root.resolve(AUDIT)));
        StringBuilder info=new StringBuilder();
        info.append("target=PlanetaryPhase3R7ResearchIntegrityNotMinecraftOriginalJVM\n");
        info.append("status=PASS_RECORD_RECONCILIATION_ONLY\nresearchGate=R_OPEN\nvisualGate=V_OPEN\n");
        info.append("phase3Rows=").append(original.size()).append("\nauditRows=").append(dispositions.size()).append('\n');
        PHASE_COUNTS.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e->
            info.append(e.getKey()).append('=').append(e.getValue()).append('\n'));
        for(Map.Entry<String,Integer> e:decisions.entrySet()) info.append(e.getKey()).append('=').append(e.getValue()).append('\n');
        info.append("sixSourceBlockerClusters=").append(blockers.size()).append("\noptionalShaderOwnerRows=").append(shader.size()).append('\n');
        info.append("sourceLedgerSHA256=").append(oldHash).append("\nsourceDispositionSHA256=").append(newHash).append('\n');
        Files.writeString(output.resolve("phase3-r7-whole-phase-owner-gate-summary.txt"),info.toString(),StandardCharsets.UTF_8);
        List<String> lines=new ArrayList<>();
        lines.add("phase\tid\tcluster\towner_family\toriginal_missing_for_phase_complete");
        actualBlockers.sort(Comparator.naturalOrder());
        lines.addAll(actualBlockers);
        Files.write(output.resolve("phase3-r7-blocking-source-groups.tsv"),lines,StandardCharsets.UTF_8);
    }
    private static List<String[]> tsv(Path file,int columns) throws Exception {
        List<String> lines=Files.readAllLines(file,StandardCharsets.UTF_8);
        assertFalse(lines.isEmpty());
        assertEquals(columns,lines.get(0).split("\t",-1).length,"Invalid header: "+file);
        List<String[]> result=new ArrayList<>();
        for(int i=1;i<lines.size();i++){
            if(lines.get(i).isBlank())continue;
            String[] fields=lines.get(i).split("\t",-1);
            assertEquals(columns,fields.length,"Malformed TSV row "+i+" in "+file);
            result.add(fields);
        }
        return result;
    }
    private static String sha(byte[] data) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data));
    }
}
