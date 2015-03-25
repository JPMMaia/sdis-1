package net.messages;

import filemanagement.Version;
import filemanagement.FileId;

/**
 * Created by Miguel on 23-03-2015.
 */
public class StoredMessage extends Message
{
    public StoredMessage(Version version, FileId fileId)
    {
        super(version, fileId);
    }
}
