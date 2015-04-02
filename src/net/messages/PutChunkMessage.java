package net.messages;

import net.chunks.ChunkNo;
import net.chunks.FileId;
import net.chunks.ReplicationDeg;
import net.chunks.Version;

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
    public String toString()
    {
        return s_TYPE + " " + m_version + " " + m_fileId + " " + m_chunkNo + " " + m_replicationDeg;
    }

    public ChunkNo getChunkNo()
    {
        return m_chunkNo;
    }

    public ReplicationDeg getReplicationDeg()
    {
        return m_replicationDeg;
    }

    @Override
    public String getType()
    {
        return s_TYPE;
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
