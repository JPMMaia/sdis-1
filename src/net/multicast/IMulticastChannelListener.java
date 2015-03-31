package net.multicast;

/**
 * Created by João on 30/03/2015.
 */
public interface IMulticastChannelListener
{
    void onDataReceived(byte[] data, int length, String peerAddress);
}
