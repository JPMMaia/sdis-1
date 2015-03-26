package filemanagement;

import java.security.InvalidParameterException;

public class ReplicationDeg
{
    private int m_value;

    public ReplicationDeg(String value) throws InvalidParameterException
    {
        int v = Integer.parseInt(value);
        ReplicationDeg.throwIfInvalid(v);

        m_value = v;
    }

    public ReplicationDeg(int value)
    {
        ReplicationDeg.throwIfInvalid(value);

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
        if(value < 0 || value > 9)
            throw new InvalidParameterException("ReplicationDeg::constructor: value must be between 0 and 9!");
    }
}
