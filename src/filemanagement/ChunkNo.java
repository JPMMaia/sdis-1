package filemanagement;

/**
 * Created by João on 25/03/2015.
 */
public class ChunkNo
{
    private int m_value;

    public ChunkNo(int value)
    {
        if (value < 0 || value > 1000000)
            throw new IllegalArgumentException("ChunkNo::constructor: Ilegal argument in chunkNo: " + value);

        m_value = value;
    }

    @Override
    public String toString()
    {
        return String.valueOf(m_value);
    }

    public int getValue()
    {
        return m_value;
    }

    public void setValue(int value)
    {
        m_value = value;
    }
}
