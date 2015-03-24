package filemanagement;

/**
 * Created by Miguel on 23-03-2015.
 */
public class Chunk
{
    private String m_fileId;
    private int m_chunkNo;

    private byte[] m_data;

    public Chunk(String fileId, int chunkNo)
    {
        if (chunkNo < 0 || chunkNo > 1000000)
            throw new IllegalArgumentException("Ilegal argument in chunkNo: " + chunkNo);

        m_fileId = fileId;
        m_chunkNo = chunkNo;
    }
}
