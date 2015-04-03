package net.messages;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by João on 30/03/2015.
 */
public class Header
{
    public static final String s_CRLF = "\r\n";
    public static final Charset s_STANDARD_CHARSET = StandardCharsets.US_ASCII;
    private List<IHeaderLine> m_messages = new ArrayList<>();
    private byte[] m_body = new byte[0];

    public Header()
    {
    }

    public Header(byte[] header, int length) throws InvalidParameterException
    {
        // Split head from body:
        byte[][] split = splitHeader(header, length);

        // Convert head to string:
        String messages = new String(split[0], Header.s_STANDARD_CHARSET);

        // Split messages:
        String[] messagesSplit = Header.splitMessages(messages);

        // Add messages to list:
        m_messages = new ArrayList<>(messagesSplit.length);
        for(String message : messagesSplit)
            addMessage(message);

        // Set body
        m_body = split[1];
    }

    public byte[] toBytes()
    {
        byte[] head = toString().getBytes(s_STANDARD_CHARSET);

        byte[] output = new byte[head.length + m_body.length];

        // Adding head:
        System.arraycopy(head, 0, output, 0, head.length);

        // Adding body:
        System.arraycopy(m_body, 0, output, head.length, m_body.length);

        return output;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();

        for (IHeaderLine message : m_messages)
        {
            builder.append(message);
            builder.append(Header.s_CRLF);
        }

        builder.append(Header.s_CRLF);

        return builder.toString();
    }

    public void addMessage(IHeaderLine message)
    {
        m_messages.add(message);
    }

    public void addMessage(String message) throws InvalidParameterException
    {
        String[] fields = Message.splitMessage(message);

        // Ignoring unknown message:
        if(fields.length == 0)
            return;

        String messageType = fields[0];
        switch (messageType)
        {
            case ChunkMessage.s_TYPE:
                addMessage(ChunkMessage.createMessage(fields));
                break;

            case DeleteMessage.s_TYPE:
                addMessage(DeleteMessage.createMessage(fields));
                break;

            case GetChunkMessage.s_TYPE:
                addMessage(GetChunkMessage.createMessage(fields));
                break;

            case PutChunkMessage.s_TYPE:
                addMessage(PutChunkMessage.createMessage(fields));
                break;

            case RemovedMessage.s_TYPE:
                addMessage(RemovedMessage.createMessage(fields));
                break;

            case StoredMessage.s_TYPE:
                addMessage(StoredMessage.createMessage(fields));
                break;

            case TcpAvailableMessage.s_TYPE:
                addMessage(TcpAvailableMessage.createMessage(fields));
                break;

            default:
                // Ignore unknown message
                break;
        }
    }

    public void removeMessage(Message message)
    {
        m_messages.remove(message);
    }

    public IHeaderLine getMessage(int index)
    {
        return m_messages.get(index);
    }

    public int getMessageNumber()
    {
        return m_messages.size();
    }

    public byte[] getBody()
    {
        return m_body;
    }

    public void setBody(byte[] body)
    {
        m_body = body;
    }

    public boolean isBodyEmpty()
    {
        return m_body.length == 0;
    }

    public static String[] splitMessages(String header)
    {
        return header.split("(\\r\\n)+");
    }

    public static byte[][] splitHeader(byte[] header, int length) throws InvalidParameterException
    {
        boolean found = false;

        int delimeterPosition = 0;
        for(int i = 0; i < length - 3; i++)
        {
            if(header[i] == Header.s_CRLF.charAt(0)
                    && header[i + 1] == Header.s_CRLF.charAt(1)
                    && header[i + 2] == Header.s_CRLF.charAt(0)
                    && header[i + 3] == Header.s_CRLF.charAt(1))
            {
                found = true;
                break;
            }

            delimeterPosition++;
        }

        if(!found)
            throw new InvalidParameterException("Header::splitHeader: <CRLF><CRLF> not found!");

        byte[][] output = new byte[2][];
        output[0] = new byte[delimeterPosition];
        System.arraycopy(header, 0, output[0], 0, output[0].length);
        delimeterPosition += 4;

        output[1] = new byte[length - delimeterPosition];
        System.arraycopy(header, delimeterPosition, output[1], 0, output[1].length);

        return output;
    }
}
