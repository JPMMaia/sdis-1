package net.messages;

import net.chunks.ChunkNo;
import net.chunks.FileId;
import net.chunks.Version;

import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;

/**
 * Created by Miguel on 23-03-2015.
 */
public class GetChunkMessage extends Message
{
    public static final String s_TYPE = "GETCHUNK";
    private ChunkNo m_chunkNo;

    public GetChunkMessage(Version version, FileId fileId, ChunkNo chunkNo)
    {
        super(version, fileId);

        m_chunkNo = chunkNo;
    }

    @Override
    public String toString()
    {
        return s_TYPE + " " + m_version + " " + m_fileId + " " + m_chunkNo;
    }

    public ChunkNo getChunkNo()
    {
        return m_chunkNo;
    }

    @Override
    public String getType()
    {
        return s_TYPE;
    }

    public static GetChunkMessage createMessage(String[] messageSplit) throws InvalidParameterException
    {
        if(messageSplit.length != 4)
            throw new InvalidParameterException();

        Version version = new Version(messageSplit[1]);
        FileId fileId = new FileId(messageSplit[2]);
        ChunkNo chunkNo = new ChunkNo(messageSplit[3]);

        return new GetChunkMessage(version, fileId, chunkNo);
    }
}
