package docscheck;

import example.npc.FirstNpc;
import example.npc.AnimationHandoffCheck;
import example.npc.NpcAnimations;
import example.npc.NpcSounds;
import example.npc.SampleNpcPack;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipFile;
import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.block.Block;

/** Runs the downloaded quickstart against real published dependencies, without Grounds services. */
public final class DocSmoke {
    public static void main(String[] args) throws Exception {
        Path work = Files.createTempDirectory("npc-docs-");
        boolean initialized = false;
        try {
            try (var zip = new ZipFile(args[0])) {
                var names = zip.stream().map(java.util.zip.ZipEntry::getName).toList();
                var unexpected = names.stream().filter(name -> !name.startsWith("npc/") && !name.startsWith("src/main/java/example/npc/")).toList();
                var required = java.util.Set.of("FirstNpc.java", "SampleNpcPack.java", "NpcAnimations.java", "NpcSounds.java");
                var missing = required.stream().filter(name -> !names.contains("src/main/java/example/npc/" + name)).toList();
                if (!unexpected.isEmpty() || !missing.isEmpty()) {
                    throw new AssertionError("Unsafe/incomplete quickstart: unexpected=" + unexpected + "; missing=" + missing);
                }
                var entries = zip.entries();
                while (entries.hasMoreElements()) {
                    var entry = entries.nextElement();
                    if (entry.isDirectory()) continue;
                    Path target = work.resolve(entry.getName()).normalize();
                    if (!target.startsWith(work)) throw new AssertionError("Unsafe example entry");
                    Files.createDirectories(target.getParent());
                    try (var input = zip.getInputStream(entry)) { Files.copy(input, target); }
                }
            }
            for (String file : new String[]{"FirstNpc.java", "SampleNpcPack.java", "NpcAnimations.java", "NpcSounds.java"}) {
                Path repository = Path.of("../../files/npc").resolve(file);
                Path download = work.resolve("src/main/java/example/npc").resolve(file);
                if (!java.util.Arrays.equals(Files.readAllBytes(repository), Files.readAllBytes(download))) {
                    throw new AssertionError("Downloaded source differs: " + file);
                }
            }
            var server = MinecraftServer.init(new Auth.Offline());
            initialized = true;
            var instance = MinecraftServer.getInstanceManager().createInstanceContainer();
            instance.setGenerator(unit -> unit.modifier().fillHeight(0, 40, Block.GRASS_BLOCK));
            SampleNpcPack.install(URI.create("http://127.0.0.1:8080/resourcepack.zip"), work.resolve("npc/resourcepack.zip"));
            server.start("127.0.0.1", 0);
            AnimationHandoffCheck.check(instance);
            var first = FirstNpc.spawn(instance, work.resolve("npc"), new Pos(0, 40, 0), NpcSounds::near);
            try {
                var npc = first.ready().toCompletableFuture().get(10, TimeUnit.SECONDS);
                long displays = instance.getEntities().stream().filter(e -> e.getEntityType() == EntityType.ITEM_DISPLAY).count();
                if (displays != 2) throw new AssertionError("Expected body/head displays: " + displays);
                try (var animation = new NpcAnimations(instance, npc)) {
                    animation.play("wave").toCompletableFuture().get(5, TimeUnit.SECONDS);
                    npc.applyHeadLook(20, -10).toCompletableFuture().get(5, TimeUnit.SECONDS);
                    npc.stopClip().toCompletableFuture().get(5, TimeUnit.SECONDS);
                }
            } finally {
                first.closeAsync().toCompletableFuture().get(5, TimeUnit.SECONDS);
            }
            if (!instance.getEntities().isEmpty()) throw new AssertionError("NPC entities survived removal");
            var cancelled = FirstNpc.spawn(instance, work.resolve("npc"), new Pos(0, 40, 0));
            cancelled.closeAsync().toCompletableFuture().get(5, TimeUnit.SECONDS);
            if (!instance.getEntities().isEmpty()) throw new AssertionError("Pending NPC survived cancellation");
            System.out.println("NPC docs quickstart OK: published model, animation, cleanup and pending cancellation");
        } finally {
            if (initialized) MinecraftServer.stopCleanly();
            try (var paths = Files.walk(work)) {
                for (var path : paths.sorted(Comparator.reverseOrder()).toList()) Files.delete(path);
            }
        }
    }
}
