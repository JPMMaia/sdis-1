package filemanagement;

/**
 * Created by João on 25/03/2015.
 */
public class Version
{
    private char m_version;
    private char m_subVersion;

    public Version(char version, char subVersion)
    {
        m_version = version;
        m_subVersion = subVersion;
    }

    public byte[] toBytes()
    {
        return new String(new char[]{m_version, '.', m_subVersion}).getBytes();
    }

    public char getVersion()
    {
        return m_version;
    }

    public char getSubVersion()
    {
        return m_subVersion;
    }

    public void setVersion(char version)
    {
        m_version = version;
    }

    public void setSubVersion(char subVersion)
    {
        m_subVersion = subVersion;
    }
}
