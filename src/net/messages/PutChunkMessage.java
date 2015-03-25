package net.messages;

import net.messages.header.FileIdField;
import net.messages.header.VersionField;

/**
 * Created by Miguel on 23-03-2015.
 */
public class PutChunkMessage extends Message
{
    public PutChunkMessage(VersionField version, FileIdField fileId, String chunkNo, String replicationDeg, String body)
    {

    }
}
