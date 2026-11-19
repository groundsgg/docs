package example.npc;

import gg.grounds.npc.minestom.NpcHandle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicLong;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;

/** Advances one shared clip using elapsed real time. Close before removing the NPC. */
public final class NpcAnimations implements AutoCloseable {
    private final NpcHandle npc;
    private final AtomicLong epoch = new AtomicLong(System.nanoTime());
    private final Task ticking;
    private CompletionStage<Void> pendingFrame = CompletableFuture.completedFuture(null);
    private boolean advancing;
    private boolean switching;
    private boolean closed;

    public NpcAnimations(Instance instance, NpcHandle npc) {
        this.npc = npc;
        ticking = instance.scheduler().buildTask(this::tick)
                .repeat(TaskSchedule.tick(1)).schedule();
    }

    private synchronized void tick() {
        if (closed || advancing || switching) return;
        long elapsed = Math.max(0, (System.nanoTime() - epoch.get()) / 1_000_000);
        advancing = true;
        try {
            pendingFrame = npc.advanceClipTo(elapsed);
        } catch (RuntimeException error) {
            advancing = false;
            error.printStackTrace();
            return;
        }
        pendingFrame.whenComplete((unused, error) -> {
            synchronized (this) {
                advancing = false;
                if (error != null && !closed) error.printStackTrace();
            }
        });
    }

    /** Drains the old frame before starting a new clip and resetting its clock. */
    public synchronized CompletionStage<Void> play(String clip) {
        if (closed || switching) {
            return CompletableFuture.failedFuture(new IllegalStateException("Animation driver closed or switching"));
        }
        switching = true;
        return pendingFrame.handle((unused, error) -> null).thenCompose(unused -> {
            synchronized (this) {
                if (closed) return CompletableFuture.<Void>failedFuture(new IllegalStateException("Animation driver closed"));
                var reservation = npc.reserveClip(clip);
                if (reservation == null) {
                    return CompletableFuture.<Void>failedFuture(new IllegalStateException("NPC clip is busy or unavailable"));
                }
                try {
                    return reservation.start(0).thenRun(() -> epoch.set(System.nanoTime()));
                } finally {
                    reservation.close();
                }
            }
        }).whenComplete((unused, error) -> {
            synchronized (this) { switching = false; }
        });
    }

    @Override
    public synchronized void close() {
        closed = true;
        ticking.cancel();
    }
}
