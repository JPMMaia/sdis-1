package net;

import net.chunks.Chunk;
import net.chunks.BackupFile;
import net.messages.Header;
import net.services.UserService;

/**
 * Created by Miguel on 30-03-2015.
 */
public interface IPeerDataChange
{
    void removeUserService(UserService service);

    void sendHeaderMDB(Header header);
    void sendHeaderMDR(Header header);
    void sendHeaderMC(Header header);

    void addHomeFile(BackupFile file);
    void addHomeChunk(Chunk identifier);
    void addStoredChunk(Chunk chunk);
    void addHomeChunkIP(Chunk identifier, String address);
    void addStoredChunkIP(Chunk chunk, String address);

    boolean isHomeChunk(Chunk identifier);
    boolean isStoredChunk(Chunk chunk);

    long getFreeSpace();
    int getRealReplicationDeg(Chunk identifier);
}
