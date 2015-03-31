package net;

import net.chunks.Chunk;
import net.chunks.StoredFile;
import net.messages.Header;

/**
 * Created by Miguel on 30-03-2015.
 */
public interface IPeerDataChange
{
    void sendHeaderMDB(Header header);
    void addStoredFile(StoredFile file);
    int getRealReplicationDeg(Chunk chunk);
}
