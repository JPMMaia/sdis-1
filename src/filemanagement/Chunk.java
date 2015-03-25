package filemanagement;

import java.io.UnsupportedEncodingException;

/**
 * Created by Miguel on 23-03-2015.
 */
public class Chunk
{
    private Version m_version;
    private FileId m_fileId;
    private ChunkNo m_chunkNo;
    private byte[] m_data;

    public Chunk(Version version, FileId fileId, ChunkNo chunkNo, byte[] data) throws UnsupportedEncodingException
    {
        m_version = version;
        m_fileId = fileId;
        m_chunkNo = chunkNo;
        m_data = data;

        System.out.println("Chunk(" + data.length + "): " + new String(data, "UTF-8"));
        System.out.println(m_fileId);
    }
}
