package net.messages;

import net.chunks.FileId;
import net.chunks.Version;

import java.nio.charset.StandardCharsets;

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

    @Override
    public abstract String toString();

    public byte[] toBytes()
    {
        return toString().getBytes(StandardCharsets.US_ASCII);
    }

    public Version getVersion()
    {
        return m_version;
    }

    public FileId getFileId()
    {
        return m_fileId;
    }

    public abstract String getType();

    public static String[] splitMessage(String message)
    {
        return message.trim().split("\\s+");
    }
}
