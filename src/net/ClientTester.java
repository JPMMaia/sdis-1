package net;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Created by Jo?o on 27/03/2015.
 */
public class ClientTester
{
    public static void main(String[] args)
    {
        try
        {
            Registry registry = LocateRegistry.getRegistry(Peer.s_SERVICE_HOST, Peer.s_SERVICE_PORT);
            IPeerService peerService = (IPeerService) registry.lookup(Peer.s_SERVICE_NAME);

            // peerService.backupFile("test_resources\\test.txt", 1);

            peerService.backupFile("test_resources\\gosto2.txt", 1);
        }
        catch (Exception e)
        {
            System.out.println(e);
            e.printStackTrace();
        }
    }
}