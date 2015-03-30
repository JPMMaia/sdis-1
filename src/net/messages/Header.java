package net.messages;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by João on 30/03/2015.
 */
public class Header
{
    private List<Message> m_messages;
    private byte[] m_body;

    public Header(String header, byte[] body)
    {
        m_body = body;

        String[] messages = Header.splitHeader(header);
        m_messages = new ArrayList<>(messages.length);
        for(String message : messages)
            addMessage(message);
    }

    public void addMessage(Message message)
    {
        m_messages.add(message);
    }

    public void addMessage(String message)
    {
        String[] fields = Message.splitMessage(message);

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
        }
    }

    public void removeMessage(Message message)
    {
        m_messages.remove(message);
    }

    public Message getMessage(int index)
    {
        return m_messages.get(index);
    }

    public byte[] getBody()
    {
        return m_body;
    }

    public static String[] splitHeader(String header)
    {
        return header.split("(\\r\\n)+");
    }
}
