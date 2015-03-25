package net.messages;

import net.messages.header.FileIdField;
import net.messages.header.VersionField;

/**
 * Created by Miguel on 23-03-2015.
 */
public abstract class Message
{
    protected VersionField m_version;
    protected FileIdField m_fileId;

    public Message(VersionField version, FileIdField fileId)
    {
        m_version = version;
        m_fileId = fileId;
    }
}
