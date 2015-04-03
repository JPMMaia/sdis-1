package net;

import net.Utils.IPFinder;
import net.chunks.*;
import net.messages.*;
import net.multicast.IMulticastChannelListener;
import net.multicast.MulticastChannelReceive;
import net.multicast.MulticastChannelSend;
import net.services.BackupService;
import net.services.RestoreService;
import net.services.UserService;
import net.tasks.ProcessGetChunkTask;
import net.tasks.SendChunkTcpTask;
import net.tasks.StoreTask;
import net.tasks.Task;

import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.InvalidParameterException;
import java.util.*;
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

    private static String s_MY_ADDRESS;

    // Multicast channels:
    private MulticastChannelReceive m_mcChannel;
    private MulticastChannelReceive m_mdbChannel;
    private MulticastChannelReceive m_mdrChannel;
    private MulticastChannelSend m_sendSocket;

    private ConcurrentHashMap<FileId, UserService> m_activeServices = new ConcurrentHashMap<>();
    private ArrayList<Task> m_waitingMessageTasks = new ArrayList<>();

    // Data the peer needs to maintain to backup:
    private long m_totalStorage = 5000000; // (bytes)
    private long m_freeStorage = m_totalStorage;
    private List<BackupFile> m_homeFiles = new ArrayList<>();
    private ConcurrentHashMap<Chunk, HashSet<String>> m_homeChunks = new ConcurrentHashMap<>(); // IP Addresses for each Chunk (that I did backup)
    private ConcurrentHashMap<Chunk, HashSet<String>> m_storedChunks = new ConcurrentHashMap<>(); // IP Addresses for each external Chunk I stored
    private ConcurrentHashMap<Chunk, HashSet<String>> m_tempStoredChunks = new ConcurrentHashMap<>(); // IP Addresses for each temporarily stored chunks

    // To restore:
    private ConcurrentHashMap<FileId, Chunk> m_receivedRecoverChunks = new ConcurrentHashMap<>();

    public Peer(String mcAddress, int mcPort, String mdbAddress, int mdbPort, String mdrAddress, int mdrPort) throws IOException
    {
        m_mcChannel = new MulticastChannelReceive(mcAddress, mcPort);
        m_mcChannel.addListener(this);

        m_mdbChannel = new MulticastChannelReceive(mdbAddress, mdbPort);
        m_mdbChannel.addListener(this);

        m_mdrChannel = new MulticastChannelReceive(mdrAddress, mdrPort);
        m_mdrChannel.addListener(this);

        m_sendSocket = new MulticastChannelSend();

        // Define my IP Address:
        s_MY_ADDRESS = IPFinder.getIP() + ":" + Integer.toString(m_sendSocket.getLocalPort());

        System.out.println("Peer running at address: " + s_MY_ADDRESS);
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

    public static void main(String[] args) throws IOException, AlreadyBoundException
    {
        // 239.1.0.1 8887 239.1.0.2 8888 239.1.0.3 8889
        if (args.length != 6)
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

        try
        {
            // Create peer service remote object:
            IPeerService peerService = (IPeerService) UnicastRemoteObject.exportObject(peer, Peer.s_SERVICE_PORT);

            // Bind in the registry:
            Registry registry = LocateRegistry.createRegistry(Peer.s_SERVICE_PORT);
            registry.rebind(Peer.class.getName(), peerService);
        } catch (Exception e)
        {
            System.err.println("Peer::main: RMI is already running on another Peer!");
        }

        System.out.println("Peer::main: Ready!");
    }

    @Override
    synchronized public String backupFile(String filename, int replicationDeg)
    {
        try
        {
            BackupFile file = new BackupFile(filename, new ReplicationDeg(replicationDeg));

            // Check if the service is concurrent:
            if (m_activeServices.containsKey(file.getFileId()))
                return "Peer::backupFile An action regarding that file is already executing, please wait!";

            UserService backup = new BackupService(file, this);
            Thread thread = new Thread(backup);
            thread.start();

            // Add to the active services list:
            m_activeServices.put(file.getFileId(), backup);

            return "Peer::backupFile Your backup request was registered! Please come again :)";
        } catch (Exception e)
        {
            return "Peer::backupFile A problem happened: " + e.getMessage();
        }
    }

    @Override
    synchronized public String printBackupFiles()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Index - Filename - Last Modified\n\n");

        for (int i = 0; i < m_homeFiles.size(); i++)
            builder.append(i).append(". - ")
                    .append(m_homeFiles.get(i).getFilePath()).append(" - ")
                    .append(m_homeFiles.get(i).getLastModified()).append("\n");

        builder.append("\n");

        return builder.toString();
    }

    @Override
    synchronized public String restoreFile(int fileIndex)
    {
        try
        {
            if (fileIndex >= m_homeFiles.size())
                return "Peer::restoreFile invalid file index";

            BackupFile file = m_homeFiles.get(fileIndex);

            // Check if the service is concurrent:
            if (m_activeServices.containsKey(file.getFileId()))
            {
                return "Peer::restoreFile An action regarding that file is already executing, please wait!";
            }

            UserService restore = new RestoreService(file, this);
            Thread thread = new Thread(restore);
            thread.start();

            // Add to the active services list:
            m_activeServices.put(file.getFileId(), restore);

            return "Peer::restoreFile Your restore request was registered! Please come again :)";
        } catch (Exception e)
        {
            return "Peer::restoreFile A problem happened: " + e.getMessage();
        }
    }

    @Override
    synchronized public String deleteFile(int fileIndex)
    {
        try
        {
            if (fileIndex >= m_homeFiles.size())
                return "Peer::restoreFile invalid file index";

            BackupFile file = m_homeFiles.get(fileIndex);

            // Check if the service is concurrent:
            if (m_activeServices.containsKey(file.getFileId()))
                return "Peer::deleteFile An action regarding that file is already executing, please wait!";

            // Delete traces of this home file:
            deleteHomeFile(file.getFileId());

            // Send deleteFile:
            DeleteMessage message = new DeleteMessage(new Version('1', '0'), file.getFileId());
            Header header = new Header();
            header.addMessage(message);
            sendHeaderMC(header);

            return "Peer::deleteFile Your delete file request was registered! Please come again :)";
        } catch (Exception e)
        {
            return "Peer::deleteFile A problem happened: " + e.getMessage();
        }
    }

    @Override
    synchronized public String setMaxDiskSpace(int bytes)
    {
        try
        {
            // é preciso ordenar os cenas e ver quais os chunks a retirar (critério, rep. degree maior que o necessário, mas pode ocorrer remover um q tenha replication degree <= desejado)
            // para os chunks a tirar, é preciso enviar "remove" e tirar dos m_storedChunks


            return "Peer::setMaskDiskSpace Your set disc space request was registered! Please come again :)";
        } catch (Exception e)
        {
            return "Peer::setMaskDiskSpace A problem happened: " + e.getMessage();
        }
    }

    @Override
    synchronized public String info()
    {
        String s1 = "There are " + m_activeServices.size() + " active services.\n";
        String s2 = "There are " + m_waitingMessageTasks.size() + " tasks waiting for msg.\n";
        String s3 = "There are " + m_homeFiles.size() + " home files.\n";
        String s4 = "Home chunks: " + m_homeChunks + "\n";
        String s5 = "Stored (backup) chunks: " + m_storedChunks + "\n";

        return s1 + s2 + s3 + s4 + s5;
    }

    @Override
    synchronized public void onDataReceived(byte[] data, int length, String peerAddress)
    {
        // Ignore test messages:
        if (new String(data).trim().equals("test"))
        {
            return;
        }

        // Ignore messages coming from this peer:
        if (peerAddress.equals(s_MY_ADDRESS))
        {
            return;
        }

        try
        {
            Header receivedHeader = new Header(data, length);

            System.out.println("DEBUG: Received " + receivedHeader.getMessage(0).getType() + " from " + peerAddress);

            Message receivedMsg = (Message) receivedHeader.getMessage(0);
            switch (receivedMsg.getType())
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
                    else if (isTemporarilyStoredChunk(chunkKey))
                        addTemporarilyStoredChunkIP(chunkKey, peerAddress);
                }
                break;

                case GetChunkMessage.s_TYPE:
                {
                    GetChunkMessage getChunkMessage = (GetChunkMessage) receivedMsg;

                    // Only respond to this get chunk message if we stored the chunk:
                    Chunk chunkKey = new Chunk(getChunkMessage.getFileId(), getChunkMessage.getChunkNo());
                    if (m_storedChunks.containsKey(chunkKey))
                    {
                        // Get the stored chunk and send it to the task:
                        Chunk wantedChunk = getStoredChunk(getChunkMessage.getFileId(), getChunkMessage.getChunkNo());

                        // Processing normal header:
                        if(receivedHeader.getMessageNumber() == 1)
                        {
                            Task task = new ProcessGetChunkTask(getChunkMessage, wantedChunk, peerAddress, this);
                            m_waitingMessageTasks.add(task);
                            new Thread(task).start();
                        }
                        // Processing enhanced message:
                        else if(receivedHeader.getMessageNumber() == 2)
                        {
                            IHeaderLine message = receivedHeader.getMessage(1);
                            if(message.getType().equals(TcpAvailableMessage.s_TYPE))
                            {
                                TcpAvailableMessage tcpAvailableMessage = (TcpAvailableMessage) message;

                                Task task = new SendChunkTcpTask(this, wantedChunk, peerAddress, tcpAvailableMessage.getPort());
                                new Thread(task).start();
                            }
                        }
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
                {
                    deleteStoredChunks(receivedMsg.getFileId());
                }
                break;

                case RemovedMessage.s_TYPE:
                {
                    //new RemoveTask((RemovedMessage) receivedMsg, peerAddress, this);
                }
                break;

                default:
                    System.err.println("Peer::onDataReceived Unknown message received: " + receivedMsg.getType());
                    break;
            }
        }
        catch (Exception e)
        {
            System.err.println("Peer::onDataReceived: Ignoring invalid header: " + e);
        }
    }

    synchronized private void distributeMessageServices(Message message, byte[] body)
    {
        for(FileId key: m_activeServices.keySet())
        {
            if (m_activeServices.get(key).wantsMessage(message, body))
                return;
        }
    }

    synchronized private void distributeMessageTasks(Message message, byte[] body)
    {
        for(Task task: m_waitingMessageTasks)
        {
            if (task.wantsMessage(message, body))
                return;
        }
    }

    synchronized private void deleteStoredChunks(FileId fileId)
    {
        // Remove all stored chunks that have the following fileId:
        for (Iterator<Map.Entry<Chunk, HashSet<String>>> it = m_storedChunks.entrySet().iterator(); it.hasNext(); )
        {
            Map.Entry<Chunk, HashSet<String>> entry = it.next();
            if(entry.getKey().getFileId().equals(fileId))
            {
                entry.getKey().deleteFile(); // delete physical file
                it.remove(); // delete from hashtable
            }
        }
    }

    @Override
    synchronized public void removeUserService(UserService service)
    {
        m_activeServices.remove(service.getFileId());
    }

    @Override
    synchronized public void removeTask(Task service)
    {
        m_waitingMessageTasks.remove(service);
    }

    @Override
    synchronized public void sendHeaderMDB(Header header)
    {
        try
        { m_sendSocket.sendHeader(header, m_mdbChannel.getAddress(), m_mdbChannel.getPort()); }
        catch (IOException e)
        { System.out.println("Oops, looks like sending to MDB went wrong!"); }
    }

    @Override
    synchronized public void sendHeaderMDR(Header header)
    {
        try
        { m_sendSocket.sendHeader(header, m_mdrChannel.getAddress(), m_mdrChannel.getPort()); }
        catch (IOException e)
        { System.out.println("Oops, looks like sending to MDR went wrong!"); }
    }

    @Override
    synchronized public void sendHeaderMC(Header header)
    {
        try
        { m_sendSocket.sendHeader(header, m_mcChannel.getAddress(), m_mcChannel.getPort()); }
        catch (IOException e)
        { System.out.println("Oops, looks like sending to MC went wrong!"); }
    }

    @Override
    synchronized public void addHomeFile(BackupFile file)
    {
        if (!m_homeFiles.contains(file))
            m_homeFiles.add(file);
        else
            System.out.println("DEBUG: Home file already exists!");
    }

    @Override
    synchronized public void addHomeChunk(Chunk identifier)
    {
        if (!m_homeChunks.containsKey(identifier))
            m_homeChunks.put(identifier, new HashSet<>());
        else
            System.out.println("DEBUG: Home chunk already exists!");
    }

    @Override
    synchronized public void addTemporarilyStoredChunk(Chunk chunk)
    {
        if (m_tempStoredChunks.containsKey(chunk))
            System.out.println("DEBUG: TemporarilyStored list already has that chunk!");
        else
        {
            HashSet<String> addresses = new HashSet<>();
            m_tempStoredChunks.put(chunk, addresses);
        }
    }

    @Override
    synchronized public void deleteTemporarilyStoredChunk(Chunk chunk)
    {
        m_tempStoredChunks.remove(chunk);
    }

    @Override
    synchronized public void moveTempChunkToStoredAndInc(Chunk chunk)
    {
        if (m_tempStoredChunks.containsKey(chunk))
        {
            HashSet<String> addresses = m_tempStoredChunks.get(chunk);
            m_tempStoredChunks.remove(chunk);

            addresses.add(s_MY_ADDRESS); // add my address!
            m_storedChunks.put(chunk, addresses);
        }
    }

    @Override
    synchronized public void addHomeChunkIP(Chunk identifier, String address)
    {
        if (!m_homeChunks.containsKey(identifier))
            System.out.println("DEBUG: The home chunk you're trying to add IP doesn't exist!");
        else
            m_homeChunks.get(identifier).add(address);
    }

    @Override
    synchronized public void addStoredChunkIP(Chunk chunk, String address)
    {
        if (!m_storedChunks.containsKey(chunk))
            System.out.println("DEBUG: The stored chunk you're trying to add IP doesn't exist!");
        else
            m_storedChunks.get(chunk).add(address);
    }

    @Override
    synchronized public void addTemporarilyStoredChunkIP(Chunk chunk, String address)
    {
        if (!m_tempStoredChunks.containsKey(chunk))
            System.out.println("DEBUG: The temporarily stored chunk you're trying to add IP doesn't exist!");
        else
            m_tempStoredChunks.get(chunk).add(address);
    }

    @Override
    synchronized public void deleteHomeFile(FileId fileId)
    {
        // Remove all home files that have the following fileId:
        ListIterator<BackupFile> iter = m_homeFiles.listIterator();
        while(iter.hasNext())
        {
            if(iter.next().getFileId().equals(fileId))
                iter.remove();
        }

        // Remove all home chunks that have the following fileId:
        for (Iterator<Map.Entry<Chunk, HashSet<String>>> it = m_homeChunks.entrySet().iterator(); it.hasNext(); )
        {
            Map.Entry<Chunk, HashSet<String>> entry = it.next();
            if(entry.getKey().getFileId().equals(fileId))
                it.remove();
        }
    }

    @Override
    synchronized public long getFreeSpace()
    {
        return m_freeStorage;
    }

    @Override
    synchronized public int getStoredMessagesReceivedHomeOrStored(Chunk identifier)
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
    synchronized public int getStoredMessagesReceivedTemporarily(Chunk identifier)
    {
        if (!m_tempStoredChunks.containsKey(identifier))
        {
            System.out.println("DEBUG: ReplicationDegree not found in temporarily stored, assumed 0");
            return 0;
        }
        else
            return m_tempStoredChunks.get(identifier).size();
    }

    @Override
    synchronized public boolean isHomeChunk(Chunk identifier)
    {
        return m_homeChunks.containsKey(identifier);
    }

    @Override
    synchronized public boolean isStoredChunk(Chunk chunk)
    {
        return m_storedChunks.containsKey(chunk);
    }

    @Override
    synchronized public boolean isTemporarilyStoredChunk(Chunk chunk)
    {
        return m_tempStoredChunks.containsKey(chunk);
    }

    @Override
    synchronized public Chunk getStoredChunk(FileId fileId, ChunkNo chunkNo)
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