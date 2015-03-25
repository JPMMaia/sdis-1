package net.messages;

import filemanagement.Version;
import filemanagement.FileId;

/**
 * Created by Miguel on 23-03-2015.
 */
public class RemovedMessage extends Message
{
    public RemovedMessage(Version version, FileId fileId)
    {
        super(version, fileId);
    }
}
