package net.messages;

import filemanagement.ChunkNo;
import filemanagement.Version;
import filemanagement.FileId;

import java.nio.charset.Charset;

/**
 * Created by Miguel on 23-03-2015.
 */
public class StoredMessage extends Message
{
    private ChunkNo m_chunkNo;

    public StoredMessage(Version version, FileId fileId, ChunkNo chunkNo)
    {
        super(version, fileId);

        m_chunkNo = chunkNo;
    }

    @Override
    public byte[] toBytes()
    {
        String message = "STORED " + m_version + " " + m_fileId + " " + m_chunkNo + 0xD + 0xA;

        return message.getBytes(Charset.forName("ASCII"));
    }
}
