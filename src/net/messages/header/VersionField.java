package net.messages.header;

/**
 * Created by João on 25/03/2015.
 */
public class VersionField
{
    private String m_value;

    public VersionField(char version, char subVersion)
    {
        setValue(version, subVersion);
    }

    @Override
    public String toString()
    {
        return m_value;
    }

    public String getValue()
    {
        return m_value;
    }

    public void setValue(char version, char subVersion)
    {
        m_value = new String(new char[]{version, '.', subVersion});
    }
}
