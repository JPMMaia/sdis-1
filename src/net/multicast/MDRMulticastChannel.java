package net.multicast;

import net.MulticastChannel;
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

    public void sendChunkMessage(ChunkMessage message)
    {

    }
}
