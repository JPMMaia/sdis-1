package net;

import net.chunks.Chunk;
import net.chunks.BackupFile;
import net.messages.Header;

/**
 * Created by Miguel on 30-03-2015.
 */
public interface IPeerDataChange
{
    void sendHeaderMDB(Header header);
    void sendHeaderMDR(Header header);
    void sendHeaderMC(Header header);

    void addHomeFile(BackupFile file);
    void addHomeChunk(String identifier);
    void addStoredChunk(Chunk chunk);
    void addHomeChunkIP(String identifier, String address);
    void addStoredChunkIP(Chunk chunk, String address);

    boolean isHomeChunk(String identifier);
    boolean isStoredChunk(Chunk chunk);

    long getFreeSpace();
    int getRealReplicationDeg(String identifier);
}
