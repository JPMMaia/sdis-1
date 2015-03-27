package net.messages;

import filemanagement.ChunkNo;
import filemanagement.FileId;
import filemanagement.ReplicationDeg;
import filemanagement.Version;

import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;

/**
 * Created by Miguel on 23-03-2015.
 */
public class PutChunkMessage extends Message
{
    public static final String s_TYPE = "PUTCHUNK";
    private ChunkNo m_chunkNo;
    private ReplicationDeg m_replicationDeg;

    public PutChunkMessage(Version version, FileId fileId, ChunkNo chunkNo, ReplicationDeg replicationDeg)
    {
        super(version, fileId);

        m_chunkNo = chunkNo;
        m_replicationDeg = replicationDeg;
    }

    @Override
    public byte[] toBytes()
    {
        String message = "PUTCHUNK " + m_version + " " + m_fileId + " " + m_chunkNo + " " + m_replicationDeg + 0xD + 0xA;

        return message.getBytes(StandardCharsets.US_ASCII);
    }

    public ChunkNo getChunkNo()
    {
        return m_chunkNo;
    }

    public ReplicationDeg getReplicationDeg()
    {
        return m_replicationDeg;
    }

    public static PutChunkMessage createMessage(String[] messageSplit) throws InvalidParameterException
    {
        if(messageSplit.length != 5)
            throw new InvalidParameterException();

        Version version = new Version(messageSplit[1]);
        FileId fileId = new FileId(messageSplit[2]);
        ChunkNo chunkNo = new ChunkNo(messageSplit[3]);
        ReplicationDeg replicationDeg = new ReplicationDeg(messageSplit[4]);

        return new PutChunkMessage(version, fileId, chunkNo, replicationDeg);
    }
}
