package net.messages;

import filemanagement.Version;
import filemanagement.FileId;

/**
 * Created by Miguel on 23-03-2015.
 */
public abstract class Message
{
    protected Version m_version;
    protected FileId m_fileId;

    public Message(Version version, FileId fileId)
    {
        m_version = version;
        m_fileId = fileId;
    }

    public abstract byte[] toBytes();

    public Version getVersion()
    {
        return m_version;
    }

    public FileId getFileId()
    {
        return m_fileId;
    }

    public static String[] splitMessage(String message)
    {
        return message.split("\\s+");
    }

    public static String[] splitHeader(String header)
    {
        return header.split("(\\r\\n)+");
    }
}
