package net.messages;

import filemanagement.ReplicationDeg;
import filemanagement.Version;
import filemanagement.FileId;
import filemanagement.ChunkNo;

import java.nio.charset.Charset;

/**
 * Created by Miguel on 23-03-2015.
 */
public class PutChunkMessage extends Message
{
    private ChunkNo m_chunkNo;
    private ReplicationDeg m_replicationDeg;
    private String m_body;

    public PutChunkMessage(Version version, FileId fileId, ChunkNo chunkNo, ReplicationDeg replicationDeg, String body)
    {
        super(version, fileId);

        m_chunkNo = chunkNo;
        m_replicationDeg = replicationDeg;
        m_body = body;
    }

    @Override
    public byte[] toBytes()
    {
        String message = "PUTCHUNK " + m_version + " " + m_fileId + " " + m_chunkNo + " " + m_replicationDeg + 0xD + 0xA;

        return message.getBytes(Charset.forName("ASCII"));
    }
}
