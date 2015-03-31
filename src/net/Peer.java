package net;

import net.chunks.Chunk;
import net.chunks.FileId;
import net.chunks.StoredFile;
import net.messages.Header;
import net.messages.Message;
import net.messages.PutChunkMessage;
import net.messages.StoredMessage;
import net.multicast.IMulticastChannelListener;
import net.multicast.MCMulticastChannel;
import net.multicast.MDBMulticastChannel;
import net.multicast.MDRMulticastChannel;
import net.services.BackupService;
import net.services.UserService;
import net.tasks.StoreTask;

import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Jo�o on 20/03/2015.
 */
public class Peer implements IPeerService, IMulticastChannelListener, IPeerDataChange
{
    // RMI Service Attributes:
    public static final String s_SERVICE_NAME = Peer.class.getName();
    public static final String s_SERVICE_HOST = "localhost";
    public static final int s_SERVICE_PORT = 1099;

    // Multicast channels:
    private MCMulticastChannel m_mcChannel;
    private MDBMulticastChannel m_mdbChannel;
    private MDRMulticastChannel m_mdrChannel;

    // Data the peer needs to maintain:
    private List<StoredFile> m_storedFiles = new ArrayList<>();
    private ConcurrentHashMap<FileId, UserService> m_activeServices = new ConcurrentHashMap<>();
    private HashMap<Chunk, ArrayList<String>> m_storedChunks = new HashMap<>(); // Arraylist of IP Addresses for each Chunk

    public Peer(String mcAddress, int mcPort, String mdbAddress, int mdbPort, String mdrAddress, int mdrPort) throws IOException
    {
        m_mcChannel = new MCMulticastChannel(mcAddress, mcPort);
        m_mcChannel.addListener(this);

        m_mdbChannel = new MDBMulticastChannel(mdbAddress, mdbPort);
        m_mdbChannel.addListener(this);

        m_mdrChannel = new MDRMulticastChannel(mdrAddress, mdrPort);
        m_mdrChannel.addListener(this);
    }

    synchronized public void run()
    {
        Thread mcThread = new Thread(m_mcChannel);
        mcThread.start();

        Thread mdbThread = new Thread(m_mdbChannel);
        mdbThread.start();

        Thread mdrThread = new Thread(m_mdrChannel);
        mdrThread.start();
    }

    @Override
    synchronized public void backupFile(String filename, int replicationDeg) throws InvalidParameterException, IOException
    {
        System.out.println("Peer::backupFile: filename -> " + filename + "; replicationDeg -> " + replicationDeg);

        UserService backup = new BackupService(filename, replicationDeg, this);
        new Thread(backup).start();
    }

    @Override
    synchronized public void restoreFile(String filename) throws RemoteException
    {
        // TODO
    }

    @Override
    synchronized public void deleteFile(String filename) throws RemoteException
    {
        // TODO
    }

    @Override
    synchronized public void setMaxDiskSpace(int bytes) throws RemoteException
    {
        // TODO
    }

    @Override
    synchronized public void onDataReceived(byte[] data, String peerAddress)
    {
        Header receivedHeader = new Header(data);

        // Ignore all headers with more than one message
        if (receivedHeader.getMessageNumber() == 1)
        {
            Message receivedMessage = receivedHeader.getMessage(0);
            switch(receivedMessage.getType())
            {
                // TODO
                /*
                case PutChunkMessage.s_TYPE:
                    new StoreTask((PutChunkMessage) receivedMessage, receivedHeader.getBody(), peerAddress, this);
                    break;

                case StoredMessage.s_TYPE:
                    new StoredTask((StoredMessage) receivedMessage, peerAddress, this);
                    break;
                    */

                /*
                case "GETCHUNK":
                    new GetChunkTask((GetChunkMessage) receivedMessage, peerAddress, this);
                    break;

                case "CHUNK":
                    new ChunkTask((ChunkMessage) receivedMessage, receivedHeader.getBody(), peerAddress, this);
                    break;

                case "DELETE":
                    new DeleteTask((DeleteMessage) receivedMessage, peerAddress, this);
                    break;

                case "REMOVE":
                    new RemoveTask((RemovedMessage) receivedMessage, peerAddress, this);
                    break;
                */

                default:
                    System.out.println("Unknown message received: " + receivedMessage.getType());
                    break;
            }
        }
    }

    private static byte[] concatenateByteArrays(byte[] array1, byte[] array2)
    {
        byte[] output = new byte[array1.length + array2.length];

        System.arraycopy(array1, 0, output, 0, array1.length);
        System.arraycopy(array2, 0, output, array1.length, array2.length);

        return output;
    }

    public static void main(String[] args) throws IOException, AlreadyBoundException
    {
        // 239.0.0.1 1 239.0.0.2 2 239.0.0.3 3
        if(args.length != 6)
        {
            System.err.println("Peer::main: Number of arguments must be 6!");
            return;
        }

        String mcAddress = args[0];
        int mcPort = Integer.parseInt(args[1]);
        String mdbAddress = args[2];
        int mdbPort = Integer.parseInt(args[3]);
        String mdrAddress = args[4];
        int mdrPort = Integer.parseInt(args[5]);

        // Create peer:
        Peer peer = new Peer(mcAddress, mcPort, mdbAddress, mdbPort, mdrAddress, mdrPort);
        peer.run();

        // Create peer service remote object:
        IPeerService peerService = (IPeerService) UnicastRemoteObject.exportObject(peer, Peer.s_SERVICE_PORT);

        // Bind in the registry:
        Registry registry = LocateRegistry.createRegistry(Peer.s_SERVICE_PORT);
        registry.rebind(Peer.class.getName(), peerService);

        System.out.println("Peer::main: Ready!");
    }

    @Override
    public void sendHeaderMDB(Header header)
    {
        try
        {
            m_mdbChannel.sendHeader(header);
        }
        catch (IOException e)
        {
            System.out.println("Oops, looks like sending to MDB went wrong!");
        }
    }

    @Override
    synchronized public void addStoredFile(StoredFile file)
    {
        m_storedFiles.add(file);
    }

    @Override
    synchronized public int getRealReplicationDeg(Chunk chunk)
    {
        if (!m_storedChunks.containsKey(chunk))
            return 0;
        else
            return m_storedChunks.get(chunk).size();
    }
}
