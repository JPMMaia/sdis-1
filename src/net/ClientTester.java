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
    String m_rmiName;
    int m_rmiPort;

    public ClientTester() throws RemoteException, NotBoundException
    {
    }

    public void parse(String args[]) throws RemoteException, NotBoundException
    {
        if (args.length < 3)
        {
            System.out.println("Usage:");
            System.out.println("<rmi object name> <rmi port> backup <filepath> <replication_degree>");
            System.out.println("<rmi object name> <rmi port> printfiles");
            System.out.println("<rmi object name> <rmi port> restore <index from printfiles>");
            System.out.println("<rmi object name> <rmi port> delete <index from printfiles>");
            System.out.println("<rmi object name> <rmi port> setdisk <size in bytes>");
            System.out.println("<rmi object name> <rmi port> info");
        }

        m_rmiName = args[0];
        m_rmiPort = Integer.parseInt(args[1]);

        Registry registry = LocateRegistry.getRegistry(Peer.s_SERVICE_HOST, m_rmiPort);
        m_peerService = (IPeerService) registry.lookup(m_rmiName);

        if (args[2].equals("backup"))
        {
            if (args.length - 2 == 3)
                System.out.println(m_peerService.backupFile(args[3], Integer.parseInt(args[4])) + "\n");
            else
                System.out.println("Invalid argument number");
        }

        else if (args[2].equals("restore"))
        {
            if (args.length - 2 == 2)
                System.out.println(m_peerService.restoreFile(Integer.parseInt(args[3])) + "\n");
            else
                System.out.println("Invalid argument number");
        }

        else if (args[2].equals("delete"))
        {
            if (args.length - 2 == 2)
                System.out.println(m_peerService.deleteFile(Integer.parseInt(args[3])) + "\n");
            else
                System.out.println("Invalid argument number");
        }

        else if (args[2].equals("printfiles"))
        {
            if (args.length - 2 == 1)
                System.out.println(m_peerService.printBackupFiles() + "\n");
            else
                System.out.println("Invalid argument number");
        }

        else if (args[2].equals("info"))
        {
            if (args.length - 2 == 1)
                System.out.println(m_peerService.info() + "\n");
            else
                System.out.println("Invalid argument number");
        }

        else if (args[2].equals("setdisk"))
        {
            if (args.length - 2 == 2)
                System.out.println(m_peerService.setMaxDiskSpace(Integer.parseInt(args[3])) + "\n");
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