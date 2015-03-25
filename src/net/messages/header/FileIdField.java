package net.messages.header;

/**
 * Created by João on 25/03/2015.
 */
public class FileIdField
{
    private String m_hash;

    public FileIdField(byte[] data)
    {
        setValue(data);
    }

    public String getHash()
    {
        return m_hash;
    }

    public void setValue(byte[] data)
    {

    }
}
