package net.multicast;

import net.MulticastChannel;
import net.messages.GetChunkMessage;
import net.messages.RemovedMessage;
import net.messages.StoredMessage;

import java.io.IOException;

/**
 * Created by Jo�o on 20/03/2015.
 */
public class MCMuliticastChannel extends MulticastChannel
{
    public MCMuliticastChannel(String address, int port) throws IOException
    {
        super(address, port);
    }

    public void sendStoredMessage(StoredMessage message)
    {

    }

    public void sendGetChunkMessage(GetChunkMessage message)
    {

    }

    public void sendDeleteMessage(StoredMessage message)
    {

    }

    public void sendRemovedMessage(RemovedMessage message)
    {

    }
}
