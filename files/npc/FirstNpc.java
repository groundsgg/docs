package example.npc;

import gg.grounds.npc.core.io.content.NpcContentLoader;
import gg.grounds.npc.core.math.NpcTransform;
import gg.grounds.npc.core.math.NpcVector3;
import gg.grounds.npc.minestom.NpcHandle;
import gg.grounds.npc.minestom.NpcPrepareResult;
import gg.grounds.npc.minestom.NpcRenderer;
import gg.grounds.npc.minestom.NpcSpawnRequest;
import gg.grounds.npc.minestom.NpcStart;
import gg.grounds.npc.minestom.spi.NpcSoundSink;
import gg.grounds.npc.minestom.transform.NpcRenderTransform;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;

/** One NPC owned by your application. Load files during startup, outside the instance tick. */
public final class FirstNpc {
    private final NpcStart start;
    private final CompletableFuture<NpcHandle> ready = new CompletableFuture<>();

    private FirstNpc(NpcStart start) {
        this.start = start;
        start.ready().whenComplete((result, error) -> {
            if (error != null) {
                fail(error);
            } else if (result instanceof NpcPrepareResult.Ready prepared) {
                var npc = prepared.getHandle();
                npc.publish().whenComplete((unused, publicationError) -> {
                    if (publicationError != null) fail(publicationError);
                    else ready.complete(npc);
                });
            } else {
                fail(new IllegalStateException("NPC preparation rejected: " + result));
            }
        });
    }

    public static FirstNpc spawn(Instance instance, Path directory, Point position) throws IOException {
        return spawn(instance, directory, position, (world, origin, cue) -> {});
    }

    public static FirstNpc spawn(
            Instance instance, Path directory, Point position, NpcSoundSink sounds) throws IOException {
        var content = NpcContentLoader.load(directory.resolve("server-content.zip"));
        content.verifyResourcePack(directory.resolve("resourcepack.zip"));
        var model = Objects.requireNonNull(
                content.getModels().get("npc_demo:npc/bot"), "Example bot model missing");
        var transform = new NpcRenderTransform(
                new NpcTransform(new NpcVector3(position.x(), position.y(), position.z())), null);
        var request = NpcSpawnRequest.builder(instance, model, transform).sounds(sounds).build();
        return new FirstNpc(NpcRenderer.prepare(request));
    }

    /** Completes after preparation and publication; failures include completed cleanup. */
    public CompletionStage<NpcHandle> ready() {
        return ready.minimalCompletionStage();
    }

    /** Cancel even unfinished preparation. Await this before stopping the instance. */
    public CompletionStage<Void> closeAsync() {
        return start.cancel();
    }

    private void fail(Throwable error) {
        start.cancel().whenComplete((unused, cleanupError) -> {
            if (cleanupError != null && cleanupError != error) error.addSuppressed(cleanupError);
            ready.completeExceptionally(error);
        });
    }
}
