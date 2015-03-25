package net.messages;

import filemanagement.ReplicationDeg;
import filemanagement.Version;
import filemanagement.FileId;
import filemanagement.ChunkNo;

/**
 * Created by Miguel on 23-03-2015.
 */
public class PutChunkMessage extends Message
{
    public PutChunkMessage(Version version, FileId fileId, ChunkNo chunkNo, ReplicationDeg replicationDeg, String body)
    {
        super(version, fileId);
    }
}
