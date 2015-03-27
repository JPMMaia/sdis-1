package net.multicast;

import net.messages.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;

/**
 * Created by Jo�o on 20/03/2015.
 */
public class MCMulticastChannel extends MulticastChannel
{
    public MCMulticastChannel(String address, int port) throws IOException
    {
        super(address, port);
    }

    public void sendStoredMessage(StoredMessage message) throws IOException
    {
        sendMessage(message);
    }

    public void sendGetChunkMessage(GetChunkMessage message) throws IOException
    {
        sendMessage(message);
    }

    public void sendDeleteMessage(StoredMessage message) throws IOException
    {
        sendMessage(message);
    }

    public void sendRemovedMessage(RemovedMessage message) throws IOException
    {
        sendMessage(message);
    }

    @Override
    protected void processHeader(byte[] header)
    {
        String[] messages = Message.splitHeader(new String(header, StandardCharsets.US_ASCII));

        try
        {
            // TODO Add more for enhancement messages:
            if(messages.length > 1)
            {
                processMessage(messages[0]);
            }
            else if(messages.length == 1)
            {
                processMessage(messages[0]);
            }
        }
        catch (InvalidParameterException e)
        {
            // Ignore message
            System.err.println("MCMulticastChannel::processHeader: Invalid header received. Ignoring header!");
        }
    }

    private void processMessage(String message) throws InvalidParameterException
    {
        String[] fields = Message.splitMessage(message);
        String messageType = fields[0];
        if(messageType.equals(StoredMessage.s_TYPE))
        {
            // TODO
            StoredMessage storedMessage = StoredMessage.createMessage(fields);
        }
        else if(messageType.equals(GetChunkMessage.s_TYPE))
        {
            // TODO
            GetChunkMessage getChunkMessage = GetChunkMessage.createMessage(fields);
        }
        else if(messageType.equals(DeleteMessage.s_TYPE))
        {
            // TODO
            DeleteMessage deleteMessage = DeleteMessage.createMessage(fields);
        }
        else if(messageType.equals(RemovedMessage.s_TYPE))
        {
            // TODO
            RemovedMessage removedMessage = RemovedMessage.createMessage(fields);
        }
    }
}
