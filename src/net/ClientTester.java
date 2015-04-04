package net;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Created by Jo?o on 27/03/2015.
 */
public class ClientTester
{
    IPeerService m_peerService;

    public ClientTester() throws RemoteException, NotBoundException
    {
        Registry registry = LocateRegistry.getRegistry(Peer.s_SERVICE_HOST, Peer.s_SERVICE_PORT);
        m_peerService = (IPeerService) registry.lookup(Peer.s_SERVICE_NAME);
    }

    public void parse(String args[]) throws RemoteException
    {
        if (args.length == 0)
        {
            System.out.println("Usage:");
            System.out.println("backup <filepath> <replication_degree>");
            System.out.println("printfiles");
            System.out.println("restore <index from printfiles>");
            System.out.println("delete <index from printfiles>");
            System.out.println("setdisk <size in bytes>");
            System.out.println("info");
        }

        else if (args[0].equals("backup"))
        {
            if (args.length == 3)
                System.out.println(m_peerService.backupFile(args[1], Integer.parseInt(args[2])) + "\n");
            else
                System.out.println("Invalid argument number");
        }

        else if (args[0].equals("restore"))
        {
            if (args.length == 2)
                System.out.println(m_peerService.restoreFile(Integer.parseInt(args[1])) + "\n");
            else
                System.out.println("Invalid argument number");
        }

        else if (args[0].equals("delete"))
        {
            if (args.length == 2)
                System.out.println(m_peerService.deleteFile(Integer.parseInt(args[1])) + "\n");
            else
                System.out.println("Invalid argument number");
        }

        else if (args[0].equals("printfiles"))
        {
            if (args.length == 1)
                System.out.println(m_peerService.printBackupFiles() + "\n");
            else
                System.out.println("Invalid argument number");
        }

        else if (args[0].equals("info"))
        {
            if (args.length == 1)
                System.out.println(m_peerService.info() + "\n");
            else
                System.out.println("Invalid argument number");
        }

        else if (args[0].equals("setdisk"))
        {
            if (args.length == 2)
                System.out.println(m_peerService.setMaxDiskSpace(Integer.parseInt(args[1])) + "\n");
            else
                System.out.println("Invalid argument number");
        }

        else System.out.println("Uknown command");
    }

    public static void main(String[] args)
    {
        try
        {
            ClientTester tester = new ClientTester();
            tester.parse(args);
        }
        catch (Exception e)
        {
            System.out.println(e);
            e.printStackTrace();
            System.exit(-1);
        }
    }
}