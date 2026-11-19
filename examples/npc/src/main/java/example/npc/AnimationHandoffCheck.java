package example.npc;

import gg.grounds.npc.minestom.NpcClipReservation;
import gg.grounds.npc.minestom.NpcHandle;
import java.lang.reflect.Proxy;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;

/** Delays actual command completion to exercise a clock switch across instance ticks. */
public final class AnimationHandoffCheck {
    public static void main(String[] args) throws Exception {
        var server = MinecraftServer.init(new Auth.Offline());
        try {
            var instance = MinecraftServer.getInstanceManager().createInstanceContainer();
            server.start("127.0.0.1", 0);
            check(instance);
        } finally { MinecraftServer.stopCleanly(); }
    }

    public static void check(Instance instance) throws Exception {
        var frames = new CopyOnWriteArrayList<CompletableFuture<Void>>();
        var times = new CopyOnWriteArrayList<Long>();
        var firstFrame = new CompletableFuture<Void>();
        var nextFrame = new CompletableFuture<Void>();
        var startRequested = new CompletableFuture<Void>();
        var startCompleted = new CompletableFuture<Void>();
        var starts = new AtomicInteger();
        var handle = (NpcHandle) Proxy.newProxyInstance(NpcHandle.class.getClassLoader(), new Class<?>[]{NpcHandle.class},
                (proxy, method, arguments) -> {
                    if (method.getName().equals("advanceClipTo")) {
                        var pending = new CompletableFuture<Void>();
                        frames.add(pending);
                        times.add((Long) arguments[0]);
                        firstFrame.complete(null);
                        if (frames.size() > 1) nextFrame.complete(null);
                        return pending;
                    }
                    if (method.getName().equals("reserveClip")) {
                        return new NpcClipReservation() {
                            public java.util.concurrent.CompletionStage<Void> start(long elapsed) {
                                starts.incrementAndGet();
                                startRequested.complete(null);
                                return startCompleted;
                            }
                            public void close() {}
                        };
                    }
                    throw new AssertionError(method.getName());
                });
        try (var clock = new NpcAnimations(instance, handle)) {
            // Age the old idle clock; behavior assertions below are on issued commands.
            var field = NpcAnimations.class.getDeclaredField("epoch");
            field.setAccessible(true);
            ((AtomicLong) field.get(clock)).set(System.nanoTime() - TimeUnit.SECONDS.toNanos(10));
            firstFrame.get(5, TimeUnit.SECONDS);
            var switched = clock.play("wave");
            if (starts.get() != 0) throw new AssertionError("Clip start overtook an unfinished old frame");
            frames.getFirst().complete(null);
            startRequested.get(5, TimeUnit.SECONDS);
            var ticks = new CompletableFuture<Void>();
            instance.scheduler().buildTask(() -> ticks.complete(null)).delay(TaskSchedule.tick(3)).schedule();
            ticks.get(5, TimeUnit.SECONDS);
            if (frames.size() != 1) throw new AssertionError("Old-age frames queued while a new start was pending");
            startCompleted.complete(null);
            switched.toCompletableFuture().get(5, TimeUnit.SECONDS);
            nextFrame.get(5, TimeUnit.SECONDS);
            if (times.get(1) >= 1000) throw new AssertionError("Wave inherited the old idle clock");
        } finally { frames.forEach(frame -> frame.complete(null)); }
        System.out.println("NPC animation handoff OK");
    }
}
