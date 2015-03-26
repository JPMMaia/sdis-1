package net.multicast;

import net.messages.ChunkMessage;

import java.io.IOException;

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

    public void sendChunkMessage(ChunkMessage message) throws IOException
    {
        sendMessage(message);
    }
}
