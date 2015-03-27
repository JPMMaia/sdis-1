package net.messages;

import net.chunks.ChunkNo;
import net.chunks.FileId;
import net.chunks.Version;

import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;


/**
 * Created by Miguel on 23-03-2015.
 */
public class ChunkMessage extends Message
{
    public static final String s_TYPE = "CHUNK";
    private ChunkNo m_chunkNo;

    public ChunkMessage(Version version, FileId fileId, ChunkNo chunkNo)
    {
        super(version, fileId);

        m_chunkNo = chunkNo;
    }

    @Override
    public byte[] toBytes()
    {
        String message = "CHUNK " + m_version + " " + m_fileId + " " + m_chunkNo + 0xD + 0xA;

        return message.getBytes(StandardCharsets.US_ASCII);
    }

    public ChunkNo getChunkNo()
    {
        return m_chunkNo;
    }

    public static ChunkMessage createMessage(String[] messageSplit) throws InvalidParameterException
    {
        if(messageSplit.length != 4)
            throw new InvalidParameterException();

        Version version = new Version(messageSplit[1]);
        FileId fileId = new FileId(messageSplit[2]);
        ChunkNo chunkNo = new ChunkNo(messageSplit[3]);

        return new ChunkMessage(version, fileId, chunkNo);
    }
}
