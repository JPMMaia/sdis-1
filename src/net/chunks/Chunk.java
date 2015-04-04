package net.chunks;

import net.messages.Header;

import java.io.*;

/**
 * Created by Miguel on 23-03-2015.
 */
public class Chunk
{
    public static final String s_CHUNK_DIRECTORY = "chunks/";
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

        // System.out.println("Chunk::constructor: fileId -> " + m_fileId +  "; length -> " + data.length + "; data -> " + new String(data, Header.s_STANDARD_CHARSET));
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

    public void storeFile()
    {
        if (m_data == null)
        {
            System.err.println("Chunk::storeFile data[] is null, cannot storeFile chunk!");
            return;
        }

        try
        {
            File chunkStore = new File(s_CHUNK_DIRECTORY + getIdentifier());
            FileOutputStream fileStream = new FileOutputStream(chunkStore);

            fileStream.write(m_data);

            fileStream.close();
        }
        catch (FileNotFoundException e)
        {
            System.err.println("Chunk::storeFile: Folder chunks not found");
            System.exit(-1);
        }
        catch (IOException e)
        {
            System.err.println("Chunk::storeFile: Error writing in storeFile chunk");
            System.exit(-1);
        }
    }

    public void deleteFile()
    {
        File file = new File(s_CHUNK_DIRECTORY + getIdentifier());

        if (!file.delete())
            System.err.println("Chunk::deleteFile - Error deleting file");
    }

    public ReplicationDeg getOptimalReplicationDeg()
    {
        return m_optimalReplicationDegree;
    }

    public void setData(byte[] data)
    {
        m_data = data;
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
