package net.messages;

import filemanagement.Version;
import filemanagement.FileId;

/**
 * Created by Miguel on 23-03-2015.
 */
public class GetChunkMessage extends Message
{
    public GetChunkMessage(Version version, FileId fileId)
    {
        super(version, fileId);
    }
}
