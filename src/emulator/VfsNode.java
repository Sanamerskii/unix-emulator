package emulator;

import java.time.Instant;
import java.util.Base64;

/** A virtual file or directory with Base64 content and basic metadata. */
public final class VfsNode {
    private final boolean directory;
    private final String base64;
    private final long size;
    private String owner = "user";
    private Instant modified;

    public VfsNode(boolean directory, byte[] content, Instant modified) {
        this.directory = directory;
        base64 = Base64.getEncoder().encodeToString(content);
        size = content.length;
        this.modified = modified;
    }

    public boolean isDirectory() { return directory; }
    public long size() { return size; }
    public String owner() { return owner; }
    public Instant modified() { return modified; }
    public byte[] content() { return Base64.getDecoder().decode(base64); }

    void changeOwner(String owner) { this.owner = owner; }
    void touch(Instant time) { modified = time; }
}
