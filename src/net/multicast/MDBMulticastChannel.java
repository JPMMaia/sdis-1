package net.multicast;

import net.messages.Message;
import net.messages.PutChunkMessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;

/**
 * Created by João on 25/03/2015.
 */
public class MDBMulticastChannel extends MulticastChannel
{
    public MDBMulticastChannel(String address, int port) throws IOException
    {
        super(address, port);
    }

    public void sendPutChunkMessage(PutChunkMessage message, byte[] body) throws IOException
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
        catch(InvalidParameterException e)
        {
            System.err.println("MDBMulticastChannel::processHeader: Invalid header received. Ignoring header!");
        }
    }

    private void processMessage(String message)
    {
        String[] fields = Message.splitMessage(message);
        String messageType = fields[0];
        if(messageType.equals(PutChunkMessage.s_TYPE))
        {
            PutChunkMessage putChunkMessage = PutChunkMessage.createMessage(fields);
        }
    }
}
