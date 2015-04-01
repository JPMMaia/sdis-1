package net;

import net.chunks.*;
import net.messages.*;
import net.multicast.IMulticastChannelListener;
import net.multicast.MCMulticastChannel;
import net.multicast.MDBMulticastChannel;
import net.multicast.MDRMulticastChannel;
import net.services.BackupService;
import net.services.UserService;
import net.tasks.ProcessGetChunkTask;
import net.tasks.StoreTask;
import net.tasks.Task;

import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashSet;
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

    private ConcurrentHashMap<FileId, UserService> m_activeServices = new ConcurrentHashMap<>();
    private ArrayList<Task> m_waitingMessageTasks = new ArrayList<>();

    // Data the peer needs to maintain to backup:
    private long m_totalStorage = 5000000; // (bytes)
    private long m_freeStorage = m_totalStorage;
    private List<BackupFile> m_homeFiles = new ArrayList<>();
    private ConcurrentHashMap<Chunk, HashSet<String>> m_homeChunks = new ConcurrentHashMap<>(); // Arraylist of IP Addresses for each Chunk
    private ConcurrentHashMap<Chunk, HashSet<String>> m_storedChunks = new ConcurrentHashMap<>();

    // To restore:
    private ConcurrentHashMap<FileId, Chunk> m_receivedRecoverChunks = new ConcurrentHashMap<>();

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

        BackupFile file = new BackupFile(filename, new ReplicationDeg(replicationDeg));

        // Check if the service is concurrent:
        if (m_activeServices.containsKey(file.getFileId()))
        {
            System.out.println("Peer => An action regarding that file is already executing, please wait!");
            return;
        }

        UserService backup = new BackupService(file, this);
        Thread thread = new Thread(backup);
        thread.start();

        // Add to the active services list:
        m_activeServices.put(file.getFileId(), backup);
    }

    @Override
    synchronized public void printBackupFiles()
    {
        System.out.println("Index - Filename - Last Modified");

        for(int i=0; i < m_homeFiles.size(); i++)
        {
            System.out.println(i + ". - "
                    + m_homeFiles.get(i).getFilePath()
                    + m_homeFiles.get(i).getLastModified());
        }

        System.out.println("\n");
    }

    @Override
    synchronized public void restoreFile(int fileIndex)
    {

    }

    @Override
    synchronized public void deleteFile(String filename)
    {
        // TODO
    }

    @Override
    synchronized public void setMaxDiskSpace(int bytes)
    {
        // TODO
    }

    @Override
    synchronized public void onDataReceived(byte[] data, int length, String peerAddress)
    {
        try
        {
            Header receivedHeader = new Header(data, length);

            // Ignore all headers with more than one message
            if (receivedHeader.getMessageNumber() == 1)
            {
                Message receivedMsg = receivedHeader.getMessage(0);
                switch(receivedMsg.getType())
                {
                    // TODO
                    case PutChunkMessage.s_TYPE:
                    {
                        if (receivedHeader.getBody() == null)
                            throw new InvalidParameterException("Peer::onDataReceived PutChunkMessage must have body!");

                        new Thread(new StoreTask((PutChunkMessage) receivedMsg, receivedHeader.getBody(), peerAddress, this)).start();
                    }
                        break;

                    case StoredMessage.s_TYPE:
                    {
                        //new Thread(new ProcessStoredTask((StoredMessage) receivedMsg, peerAddress, this)).start();
                        Chunk chunkKey = new Chunk(receivedMsg.getFileId(), ((StoredMessage) receivedMsg).getChunkNo());

                        // Only process a Stored Message if we have that chunk:
                        if (isHomeChunk(chunkKey))
                            addHomeChunkIP(chunkKey, peerAddress);
                        else if (isStoredChunk(chunkKey))
                            addStoredChunkIP(chunkKey, peerAddress);
                    }
                        break;

                    case GetChunkMessage.s_TYPE:
                    {
                        // Only respond to this get chunk message if we stored the chunk:
                        Chunk chunkKey = new Chunk(receivedMsg.getFileId(), ((GetChunkMessage) receivedMsg).getChunkNo());
                        if (m_storedChunks.containsKey(chunkKey))
                        {
                            // Get the stored chunk and send it to the task:
                            Chunk wantedChunk = getStoredChunk(receivedMsg.getFileId(), ((GetChunkMessage) receivedMsg).getChunkNo());

                            Task task = new ProcessGetChunkTask((GetChunkMessage) receivedMsg, wantedChunk, peerAddress, this);
                            m_waitingMessageTasks.add(task);
                            new Thread(task).start();
                        }
                    }
                        break;

                    case ChunkMessage.s_TYPE:
                    {
                        if (receivedHeader.getBody() == null)
                            throw new InvalidParameterException("Peer::onDataReceived ChunkMessage must have body!");

                        distributeMessageServices(receivedMsg, receivedHeader.getBody());
                        distributeMessageTasks(receivedMsg, receivedHeader.getBody());
                    }
                        break;


                    case DeleteMessage.s_TYPE:
                        deleteStoredChunks(receivedMsg.getFileId());
                        break;

                    /*
                    case "REMOVE":
                        new RemoveTask((RemovedMessage) receivedMsg, peerAddress, this);
                        break;
                    */

                    default:
                        System.err.println("Peer::onDataReceived Unknown message received: " + receivedMsg.getType());
                        break;
                }
            }
            System.err.println("Peer::onDataReceived Received header with more than 1 message, ignoring");
        }
        catch(Exception e)
        {
            System.err.println("Peer::onDataReceived: Ignoring invalid header: " + e.getMessage());
        }
    }

    private synchronized void distributeMessageServices(Message message, byte[] body)
    {
        for(FileId key: m_activeServices.keySet())
        {
            if (m_activeServices.get(key).wantsMessage(message, body))
                return;
        }
    }

    private synchronized void distributeMessageTasks(Message message, byte[] body)
    {
        for(Task task: m_waitingMessageTasks)
        {
            if (task.wantsMessage(message, body))
                return;
        }
    }

    private synchronized void deleteStoredChunks(FileId fileId)
    {
        for(Chunk chunkKey: m_storedChunks.keySet())
        {
            if (chunkKey.getFileId().equals(fileId))
            {
                chunkKey.deleteFile();
                m_storedChunks.remove(chunkKey);
            }
        }
    }

    public static void main(String[] args) throws IOException, AlreadyBoundException
    {
        // 239.1.0.1 8887 239.1.0.2 8888 239.1.0.3 8889
        if(args.length != 6)
        {
            System.err.println("Peer::main: Number of arguments must be 6!");
            System.out.println("Assumed default values: 239.1.0.1 8887 239.1.0.2 8888 239.1.0.3 8889");

            args = new String[6];
            args[0] = "239.1.0.1";
            args[1] = "8887";
            args[2] = "239.1.0.2";
            args[3] = "8888";
            args[4] = "239.1.0.3";
            args[5] = "8889";
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
    public synchronized void removeUserService(UserService service)
    {
        m_activeServices.remove(service.getFileId());
    }

    @Override
    public synchronized void removeTask(Task service)
    {
        m_waitingMessageTasks.remove(service);
    }

    @Override
    public synchronized void sendHeaderMDB(Header header)
    {
        try
        { m_mdbChannel.sendHeader(header); }
        catch (IOException e)
        { System.out.println("Oops, looks like sending to MDB went wrong!"); }
    }

    @Override
    public synchronized void sendHeaderMDR(Header header)
    {
        try
        { m_mdrChannel.sendHeader(header); }
        catch (IOException e)
        { System.out.println("Oops, looks like sending to MDR went wrong!"); }
    }

    @Override
    public synchronized void sendHeaderMC(Header header)
    {
        try
        { m_mcChannel.sendHeader(header); }
        catch (IOException e)
        { System.out.println("Oops, looks like sending to MC went wrong!"); }
    }

    @Override
    public synchronized void addHomeFile(BackupFile file)
    {
        if (!m_homeFiles.contains(file))
            m_homeFiles.add(file);
        else
            System.out.println("DEBUG: Home file already exists!");
    }

    @Override
    public synchronized void addHomeChunk(Chunk identifier)
    {
        if (!m_homeChunks.containsKey(identifier))
            m_homeChunks.put(identifier, new HashSet<>());
        else
            System.out.println("DEBUG: Home chunk already exists!");
    }

    @Override
    public synchronized void addStoredChunk(Chunk chunk)
    {
        HashSet<String> listIPs = new HashSet<>();
        listIPs.add("localhost");
        m_storedChunks.put(chunk, listIPs);

        // Store the chunk physically
        chunk.storeFile();
    }

    @Override
    public synchronized void addHomeChunkIP(Chunk identifier, String address)
    {
        if (!m_homeChunks.containsKey(identifier))
            System.out.println("DEBUG: The home chunk you're trying to add IP doesn't exist!");
        else
            m_homeChunks.get(identifier).add(address);
    }

    @Override
    public synchronized void addStoredChunkIP(Chunk chunk, String address)
    {
        if (!m_storedChunks.containsKey(chunk))
            System.out.println("DEBUG: The stored chunk you're trying to add IP doesn't exist!");
        else
            m_storedChunks.get(chunk).add(address);
    }

    @Override
    public synchronized long getFreeSpace()
    {
        return m_freeStorage;
    }

    @Override
    public synchronized int getRealReplicationDeg(Chunk identifier)
    {
        if (!m_homeChunks.containsKey(identifier))
        {
            if (!m_storedChunks.containsKey(identifier))
            {
                System.out.println("DEBUG: ReplicationDegree not found in homeChunks or storedChunks, assumed 0");
                return 0;
            }
            else
                return m_storedChunks.get(identifier).size();
        }
        else
            return m_homeChunks.get(identifier).size();
    }

    @Override
    public synchronized boolean isHomeChunk(Chunk identifier)
    {
        return m_homeChunks.containsKey(identifier);
    }

    @Override
    public synchronized boolean isStoredChunk(Chunk chunk)
    {
        return m_storedChunks.containsKey(chunk);
    }

    @Override
    public synchronized Chunk getStoredChunk(FileId fileId, ChunkNo chunkNo)
    {
        Chunk keyChunk = new Chunk(fileId, chunkNo);

        for(Chunk key: m_storedChunks.keySet())
        {
            if (key.equals(keyChunk))
                return key;
        }

        System.out.println("Peer::getStoredChunk - No chunk was found!");
        return null;
    }
}
