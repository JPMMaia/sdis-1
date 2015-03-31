package net.chunks;

import java.security.InvalidParameterException;

/**
 * Created by João on 25/03/2015.
 */
public class ChunkNo
{
    private static final int s_MAX_VALUE = 1000000;
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
        if (value < 0 || value > s_MAX_VALUE)
            throw new InvalidParameterException("ChunkNo::constructor: Value out of range [0, " + s_MAX_VALUE + "]. Value was " + value);
    }
}
