package net.chunks;

import net.messages.Header;

import java.io.UnsupportedEncodingException;

/**
 * Created by Miguel on 23-03-2015.
 */
public class Chunk
{
    private FileId m_fileId;
    private ChunkNo m_chunkNo;
    private ReplicationDeg m_optimalReplicationDegree;
    private byte[] m_data;

    public Chunk(FileId fileId, ChunkNo chunkNo, ReplicationDeg replicationOptimal, byte[] data)
    {
        m_fileId = fileId;
        m_chunkNo = chunkNo;
        m_optimalReplicationDegree = replicationOptimal;
        m_data = data;

        System.out.println("Chunk::constructor: fileId -> " + m_fileId +  "; length -> " + data.length + "; data -> " + new String(data, Header.s_STANDARD_CHARSET));
    }

    public Chunk(FileId fileId, ChunkNo chunkNo)
    {
        m_fileId = fileId;
        m_chunkNo = chunkNo;
    }

    public FileId getFileId()
    {
        return m_fileId;
    }

    public ChunkNo getChunkNo()
    {
        return m_chunkNo;
    }

    public String getIdentifier()
    {
        return "" + m_fileId + "." + m_chunkNo;
    }

    public ReplicationDeg getOptimalReplicationDeg()
    {
        return m_optimalReplicationDegree;
    }

    public byte[] getData()
    {
        return m_data;
    }

    @Override
    public int hashCode()
    {
        return getIdentifier().hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof Chunk))
            return false;
        else
        {
            return this.getIdentifier().equals(((Chunk) obj).getIdentifier());
        }
    }

    @Override
    public String toString()
    {
        return getIdentifier();
    }
}
