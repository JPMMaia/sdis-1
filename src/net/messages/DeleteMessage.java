package net.messages;

import net.chunks.FileId;
import net.chunks.Version;

import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;

/**
 * Created by Miguel on 23-03-2015.
 */
public class DeleteMessage extends Message
{
    public static final String s_TYPE = "DELETE";
    public DeleteMessage(Version version, FileId fileId)
    {
        super(version, fileId);
    }

    @Override
    public String toString()
    {
        return s_TYPE + " " + m_version + " " + m_fileId;
    }

    @Override
    public String getType()
    {
        return s_TYPE;
    }

    public static DeleteMessage createMessage(String[] messageSplit) throws InvalidParameterException
    {
        if(messageSplit.length != 3)
            throw new InvalidParameterException();

        Version version = new Version(messageSplit[1]);
        FileId fileId = new FileId(messageSplit[2]);

        return new DeleteMessage(version, fileId);
    }
}
