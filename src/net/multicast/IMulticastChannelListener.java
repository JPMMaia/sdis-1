package net.multicast;

/**
 * Created by Jo�o on 30/03/2015.
 */
public interface IMulticastChannelListener
{
    void onDataReceived(byte[] data, int length, String peerAddress);
}
