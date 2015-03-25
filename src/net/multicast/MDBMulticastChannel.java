package net.multicast;

import net.MulticastChannel;
import net.messages.PutChunkMessage;

import java.io.IOException;

/**
 * Created by João on 25/03/2015.
 */
public class MDBMulticastChannel extends MulticastChannel
{
    public MDBMulticastChannel(String address, int port) throws IOException
    {
        super(address, port);
    }

    public void sendPutChunkMessage(PutChunkMessage message)
    {

    }
}
