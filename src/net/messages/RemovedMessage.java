package net.messages;

import filemanagement.ChunkNo;
import filemanagement.FileId;
import filemanagement.Version;

import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;

/**
 * Created by Miguel on 23-03-2015.
 */
public class RemovedMessage extends Message
{
    public static final String s_TYPE = "REMOVED";
    private ChunkNo m_chunkNo;

    public RemovedMessage(Version version, FileId fileId, ChunkNo chunkNo)
    {
        super(version, fileId);

        m_chunkNo = chunkNo;
    }

    @Override
    public byte[] toBytes()
    {
        String message = "REMOVED " + m_version + " " + m_fileId + " " + m_chunkNo + 0xD + 0xA;

        return message.getBytes(StandardCharsets.US_ASCII);
    }

    public ChunkNo getChunkNo()
    {
        return m_chunkNo;
    }

    public static RemovedMessage createMessage(String[] messageSplit) throws InvalidParameterException
    {
        if(messageSplit.length != 4)
            throw new InvalidParameterException();

        Version version = new Version(messageSplit[1]);
        FileId fileId = new FileId(messageSplit[2]);
        ChunkNo chunkNo = new ChunkNo(messageSplit[3]);

        return new RemovedMessage(version, fileId, chunkNo);
    }
}
