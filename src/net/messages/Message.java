package net.messages;

import net.chunks.FileId;
import net.chunks.Version;

/**
 * Created by Miguel on 23-03-2015.
 */
public abstract class Message implements IHeaderLine
{
    protected Version m_version;
    protected FileId m_fileId;

    public Message(Version version, FileId fileId)
    {
        m_version = version;
        m_fileId = fileId;
    }

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
        return message.trim().split("\\s+");
    }
}
