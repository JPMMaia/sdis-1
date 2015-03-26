package net.messages;

import filemanagement.Version;
import filemanagement.FileId;

import java.nio.charset.Charset;

/**
 * Created by Miguel on 23-03-2015.
 */
public class DeleteMessage extends Message
{
    public DeleteMessage(Version version, FileId fileId)
    {
        super(version, fileId);
    }

    @Override
    public byte[] toBytes()
    {
        String message = "DELETE " + m_version + " " + m_fileId + 0xD + 0xA;

        return message.getBytes(Charset.forName("ASCII"));
    }
}
