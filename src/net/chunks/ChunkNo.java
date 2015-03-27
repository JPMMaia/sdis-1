package net.chunks;

import java.security.InvalidParameterException;

/**
 * Created by João on 25/03/2015.
 */
public class ChunkNo
{
    private int m_value;

    public ChunkNo(String value) throws InvalidParameterException
    {
        int v = Integer.parseInt(value);
        ChunkNo.throwIfInvalid(v);

        m_value = v;
    }

    public ChunkNo(int value) throws InvalidParameterException
    {
        ChunkNo.throwIfInvalid(value);

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

    private static void throwIfInvalid(int value) throws InvalidParameterException
    {
        if (value < 0 || value > 1000000)
            throw new IllegalArgumentException("ChunkNo::constructor: Ilegal argument in chunkNo: " + value);
    }
}
