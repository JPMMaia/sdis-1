package net.chunks;

import java.io.Serializable;
import java.security.InvalidParameterException;

/**
 * Created by João on 25/03/2015.
 */
public class Version implements Serializable
{
    private char m_version;
    private char m_subVersion;

    public Version(String value) throws InvalidParameterException
    {
        if(value.length() != 3)
            throw new InvalidParameterException("Version::constructor: Value must have a length of 3!");

        m_version = value.charAt(0);
        if(value.charAt(1) != '.')
            throw new InvalidParameterException("Version::constructor: Second char must be <.>!");

        m_subVersion = value.charAt(2);
    }

    public Version(char version, char subVersion)
    {
        m_version = version;
        m_subVersion = subVersion;
    }

    @Override
    public String toString()
    {
        return new String(new char[]{m_version, '.', m_subVersion});
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
