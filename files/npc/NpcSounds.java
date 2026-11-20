package example.npc;

import gg.grounds.npc.core.math.NpcVector3;
import gg.grounds.npc.core.model.NpcClipSound;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;

/** Delivers a shared cue to players in its radius on this instance. */
public final class NpcSounds {
    private NpcSounds() {}

    public static void near(Instance instance, NpcVector3 origin, NpcClipSound cue) {
        var position = new Vec(origin.getX(), origin.getY(), origin.getZ());
        var sound = Sound.sound(Key.key(cue.getKey()), Sound.Source.MASTER,
                (float) cue.getVolume(), (float) cue.getPitch());
        for (var player : instance.getPlayers()) {
            if (player.getPosition().distanceSquared(position) <= cue.getRadius() * cue.getRadius()) {
                player.playSound(sound, position.x(), position.y(), position.z());
            }
        }
    }
}
