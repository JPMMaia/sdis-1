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
}
