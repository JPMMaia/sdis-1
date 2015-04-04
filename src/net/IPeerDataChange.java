package net;

import net.chunks.Chunk;
import net.chunks.BackupFile;
import net.chunks.ChunkNo;
import net.chunks.FileId;
import net.messages.Header;
import net.services.UserService;
import net.tasks.Task;

/**
 * Created by Miguel on 30-03-2015.
 */
public interface IPeerDataChange
{
    void removeUserService(UserService service);
    void removeTask(Task service);

    void sendHeaderMDB(Header header);
    void sendHeaderMDR(Header header);
    void sendHeaderMC(Header header);

    void addHomeFile(BackupFile file);
    void addHomeChunk(Chunk identifier);
    void addTemporarilyStoredChunk(Chunk chunk);
    void deleteTemporarilyStoredChunk(Chunk chunk);
    void moveTempChunkToStoredAndInc(Chunk chunk);

    void addHomeChunkIP(Chunk identifier, String address);
    void addStoredChunkIP(Chunk chunk, String address);
    void addTemporarilyStoredChunkIP(Chunk chunk, String address);

    void removeStoredChunkIP(Chunk identifier, String address);

    void deleteHomeFile(FileId fileId);

    boolean isHomeChunk(Chunk identifier);
    boolean isStoredChunk(Chunk chunk);
    boolean isTemporarilyStoredChunk(Chunk chunk);

    Chunk getStoredChunk(FileId fileId, ChunkNo chunkNo);

    int getStoredMessagesReceivedHomeOrStored(Chunk chunk);
    int getStoredMessagesReceivedTemporarily(Chunk identifier);

    long getFreeSpace();
    void decreaseFreeSpace(long value);
    void cleanUnnacessaryChunks();
    void freeSpaceByDeletingChunks(long spaceToDelete);
}
