package net.messages;

import filemanagement.ChunkNo;
import filemanagement.Version;
import filemanagement.FileId;

import java.nio.charset.StandardCharsets;

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
    public byte[] toBytes()
    {
        String message = "GETCHUNK " + m_version + " " + m_fileId + " " + m_chunkNo + 0xD + 0xA;

        return message.getBytes(StandardCharsets.US_ASCII);
    }

    public ChunkNo getChunkNo()
    {
        return m_chunkNo;
    }

    public static GetChunkMessage createMessage(String[] messageSplit)
    {
        Version version = new Version(messageSplit[1]);
        FileId fileId = new FileId(messageSplit[2]);
        ChunkNo chunkNo = new ChunkNo(messageSplit[3]);

        return new GetChunkMessage(version, fileId, chunkNo);
    }
}
