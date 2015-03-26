package net.multicast;

import net.messages.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Created by João on 25/03/2015.
 */
public class MDRMulticastChannel extends MulticastChannel
{
    public MDRMulticastChannel(String address, int port) throws IOException
    {
        super(address, port);
    }

    @Override
    public void run()
    {

    }

    public void sendChunkMessage(ChunkMessage message, String body) throws IOException
    {
        sendMessage(message);
    }

    private void processHeader(byte[] header)
    {
        String[] messages = Message.splitHeader(new String(header, StandardCharsets.US_ASCII));

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

    private void processMessage(String message)
    {
        String[] fields = Message.splitMessage(message);
        String messageType = fields[0];
        if(messageType.equals(ChunkMessage.s_TYPE))
        {
            // TODO
        }
    }
}
