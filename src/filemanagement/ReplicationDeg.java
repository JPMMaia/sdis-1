package filemanagement;

import java.security.InvalidParameterException;

public class ReplicationDeg
{
    private int m_value;

    public ReplicationDeg(int value)
    {
        if(value < 0 || value > 9)
            throw new InvalidParameterException("ReplicationDeg::constructor: value must be between 0 and 9!");

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
