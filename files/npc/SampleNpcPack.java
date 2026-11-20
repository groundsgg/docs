package example.npc;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.player.PlayerSpawnEvent;

/** Offers the local file's exact hash at your pack URL. Install once before players connect. */
public final class SampleNpcPack {
    private SampleNpcPack() {}

    public static void install(URI url, Path file) throws IOException {
        var pack = ResourcePackInfo.resourcePackInfo(
                UUID.fromString("e4bde800-f04a-4d62-9a8f-b956fdb6b35d"), url, sha1(file));
        var request = ResourcePackRequest.resourcePackRequest().packs(pack).replace(false).required(true).build();
        MinecraftServer.getGlobalEventHandler().addListener(PlayerSpawnEvent.class, event -> {
            if (event.isFirstSpawn()) event.getPlayer().sendResourcePacks(request);
        });
    }

    private static String sha1(Path file) throws IOException {
        try {
            var digest = MessageDigest.getInstance("SHA-1");
            try (var input = new DigestInputStream(Files.newInputStream(file), digest)) {
                input.transferTo(java.io.OutputStream.nullOutputStream());
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("Java runtime does not provide SHA-1", impossible);
        }
    }
}
