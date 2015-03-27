package net;

import org.junit.Assert;
import org.junit.Test;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Created by João on 27/03/2015.
 */
public class ClientTester
{
    @Test
    public void test()
    {
        try
        {
            Registry registry = LocateRegistry.getRegistry(Peer.s_HOST, Peer.s_PORT);
            PeerService peerService = (PeerService) registry.lookup(Peer.s_NAME);

            peerService.backupFile("test_resources\\test.txt", 1);
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
    }
}
