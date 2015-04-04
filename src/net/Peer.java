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
import net.tasks.*;

import java.io.*;
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
public class Peer implements IPeerService, IMulticastChannelListener, IPeerDataChange, Serializable
{
    // RMI Service Attributes:
    public static final String s_SERVICE_NAME = Peer.class.getName();
    public static final String s_SERVICE_HOST = "localhost";
    public static final int s_SERVICE_PORT = 1099;

    private static String s_MY_ADDRESS;
    private static String s_SERIALIZATION_FOLDER = "serialization/";
    private static String s_SERIALIZATION_FILE = "session.ser";

    // Multicast channels:
    transient private MulticastChannelReceive m_mcChannel;
    transient private MulticastChannelReceive m_mdbChannel;
    transient private MulticastChannelReceive m_mdrChannel;
    transient private MulticastChannelSend m_sendSocket;

    transient private ConcurrentHashMap<FileId, UserService> m_activeServices = new ConcurrentHashMap<>();
    transient private ArrayList<Task> m_waitingMessageTasks = new ArrayList<>();

    // Data the peer needs to maintain to backup:
    private long m_totalStorage = 5000000; // (bytes)
    private long m_freeStorage = m_totalStorage;
    private List<BackupFile> m_homeFiles = new ArrayList<>();
    private ConcurrentHashMap<Chunk, HashSet<String>> m_homeChunks = new ConcurrentHashMap<>(); // IP Addresses for each Chunk (that I did backup)
    private ConcurrentHashMap<Chunk, HashSet<String>> m_storedChunks = new ConcurrentHashMap<>(); // IP Addresses for each external Chunk I stored
    private ConcurrentHashMap<Chunk, HashSet<String>> m_tempStoredChunks = new ConcurrentHashMap<>(); // IP Addresses for each temporarily stored chunks
    private HashMap<String, DeleteMessage> m_deleteMessages = new HashMap<>();

    public Peer()
    {
    }

    public void initialize(String mcAddress, int mcPort, String mdbAddress, int mdbPort, String mdrAddress, int mdrPort) throws IOException
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

    public void saveState()
    {
        FileOutputStream fileOut = null;
        try
        {
            fileOut = new FileOutputStream(s_SERIALIZATION_FOLDER + s_SERIALIZATION_FILE);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(this);
            out.close();
            fileOut.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
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

    public static void main(String[] args) throws IOException, AlreadyBoundException, ClassNotFoundException
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

        // Create serialization folder if it doesn't exists:
        File folderSer = new File(s_SERIALIZATION_FOLDER);
        if(!folderSer.exists())
            folderSer.mkdir();

        // Create chunks folder if it doesn't exists:
        File folderChunks = new File(Chunk.s_CHUNK_DIRECTORY);
        if(!folderChunks.exists())
            folderChunks.mkdir();

        // Create restore folder if it doesn't exists:
        File folderRestore = new File(BackupFile.s_RESTORE_DIRECTORY);
        if(!folderRestore.exists())
            folderRestore.mkdir();

        Peer peer;

        // If serialization file exists:
        File file = new File(s_SERIALIZATION_FOLDER + s_SERIALIZATION_FILE);
        if(file.exists())
        {
            FileInputStream fileIn = new FileInputStream(file);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            peer = (Peer) in.readObject();
            in.close();
            fileIn.close();
        }
        else
        {
            peer = new Peer();
        }

        // Initialize peer:
        peer.initialize(mcAddress, mcPort, mdbAddress, mdbPort, mdrAddress, mdrPort);
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
        peer.validateChunkFiles();
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

            saveState();
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

            saveState();
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

            saveState();
            return "Peer::deleteFile Your delete file request was registered! Please come again :)";
        } catch (Exception e)
        {
            return "Peer::deleteFile A problem happened: " + e.getMessage();
        }
    }

    @Override
    synchronized public String setMaxDiskSpace(int newStorage)
    {
        try
        {
            // If we're upgrading space, it's simple => just replace the space
            if (newStorage >= m_totalStorage)
            {
                m_freeStorage += (newStorage - m_totalStorage);
                m_totalStorage = newStorage;
                saveState();
                return "Peer::setMaxDiskSpace Upgraded space! Hurray! => new Total storage: " + m_totalStorage + " | => new Free storage: " + m_freeStorage;
            }

            // If we're downgrading storage space: the case if there's still any free space or 0
            if (Math.abs(newStorage - m_totalStorage) <= m_freeStorage)
            {
                m_freeStorage -= Math.abs(newStorage - m_totalStorage);
                m_totalStorage = newStorage;
                saveState();
                return "Peer::setMaxDiskSpace decreased space (with no need for backup) => new Total storage: " + m_totalStorage + " | => new Free storage: " + m_freeStorage;
            }
            else // if we're downgrading and we need to delete chunks!
            {
                m_freeStorage -= Math.abs(newStorage - m_totalStorage);
                m_totalStorage = newStorage;

                System.out.println("Fiquei com free storage negativo: " + m_freeStorage);

                freeSpaceByDeletingChunks(Math.abs(m_freeStorage));
                saveState();
                return "Peer::setMaxDiskSpace decreased space (with no need for backup) => new Total storage: " + m_totalStorage + " | => new Free storage: " + m_freeStorage;
            }
        } catch (Exception e)
        {
            return "Peer::setMaxDiskSpace A problem happened: " + e.getMessage();
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

                    // Distribute messages for Reclaim tasks:
                    distributeMessageTasks(receivedMsg, receivedHeader.getBody());
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
                    DeleteMessage message = (DeleteMessage) receivedMsg;
                    m_deleteMessages.put(message.getFileId().getValue(), message);
                    deleteStoredChunks(message.getFileId());
                }
                break;

                case RemovedMessage.s_TYPE:
                {
                    Chunk chunkKey = new Chunk(receivedMsg.getFileId(), ((RemovedMessage) receivedMsg).getChunkNo());

                    // Remove IP of the peer that removed the chunk:
                    if (isStoredChunk(chunkKey))
                    {
                        removeStoredChunkIP(chunkKey, peerAddress);

                        // I need to do this to get the chunk that has the optimal replication information
                        Chunk storedChunk = getStoredChunk(receivedMsg.getFileId(), ((RemovedMessage) receivedMsg).getChunkNo());

                        // If the optimal replication is lower than what I wanted, let's start a ReclaimTask that may start a PutChunkTask
                        if (m_storedChunks.get(chunkKey).size() < storedChunk.getOptimalReplicationDeg().getValue())
                        {
                            Task task = new ReclaimTask(storedChunk, this);
                            m_waitingMessageTasks.add(task);
                            new Thread(task).start();
                        }
                    }
                }
                break;

                case ValidMessage.s_TYPE:
                {
                    ValidMessage message = (ValidMessage) receivedMsg;
                    String fileId = message.getFileId().getValue();

                    DeleteMessage deleteMessage = m_deleteMessages.get(fileId);
                    if(deleteMessage == null)
                        break;

                    Header header = new Header();
                    header.addMessage(deleteMessage);
                    sendHeaderMC(header);
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
            return;
        }

        saveState();
    }

    synchronized private void distributeMessageServices(Message message, byte[] body)
    {
        for(FileId key: m_activeServices.keySet())
        {
            m_activeServices.get(key).wantsMessage(message, body);
        }
    }

    synchronized private void distributeMessageTasks(Message message, byte[] body)
    {
        for(Task task: m_waitingMessageTasks)
        {
            task.wantsMessage(message, body);
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

        // Remove all files in disk that have the following fileId:
        File folder = new File(Chunk.s_CHUNK_DIRECTORY);
        File[] listOfFiles = folder.listFiles();
        for(File file : listOfFiles)
        {
            if(file.isFile())
            {
                String name = file.getName();
                String[] split = name.split("\\.");
                if(split.length != 0)
                {
                    if(split[0].equals(fileId.getValue()))
                        file.delete();
                }
                else
                {
                    if(name.equals(fileId.getValue()))
                        file.delete();
                }
            }
        }

        saveState();
    }

    @Override
    synchronized public void removeUserService(UserService service)
    {
        m_activeServices.remove(service.getFileId());
        saveState();
    }

    @Override
    synchronized public void removeTask(Task service)
    {
        m_waitingMessageTasks.remove(service);
        saveState();
    }

    @Override
    synchronized public void sendHeaderMDB(Header header)
    {
        try
        {
            m_sendSocket.sendHeader(header, m_mdbChannel.getAddress(), m_mdbChannel.getPort());
        }
        catch (IOException e)
        {
            System.out.println("Oops, looks like sending to MDB went wrong!");
        }
    }

    @Override
    synchronized public void sendHeaderMDR(Header header)
    {
        try
        {
            m_sendSocket.sendHeader(header, m_mdrChannel.getAddress(), m_mdrChannel.getPort());
        }
        catch (IOException e)
        {
            System.out.println("Oops, looks like sending to MDR went wrong!");
        }
    }

    @Override
    synchronized public void sendHeaderMC(Header header)
    {
        try
        {
            m_sendSocket.sendHeader(header, m_mcChannel.getAddress(), m_mcChannel.getPort());
        }
        catch (IOException e)
        {
            System.out.println("Oops, looks like sending to MC went wrong!");
        }
    }

    @Override
    synchronized public void addHomeFile(BackupFile file)
    {
        if (!m_homeFiles.contains(file))
        {
            m_homeFiles.add(file);
            saveState();
        }
        else
            System.out.println("DEBUG: Home file already exists!");
    }

    @Override
    synchronized public void addHomeChunk(Chunk identifier)
    {
        if (!m_homeChunks.containsKey(identifier))
        {
            m_homeChunks.put(identifier, new HashSet<>());
            saveState();
        }

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
            m_tempStoredChunks.put(chunk, new HashSet<>());
            saveState();
        }
    }

    @Override
    synchronized public void deleteTemporarilyStoredChunk(Chunk chunk)
    {
        m_tempStoredChunks.remove(chunk);
        saveState();
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
            saveState();
        }
    }

    @Override
    synchronized public void addHomeChunkIP(Chunk identifier, String address)
    {
        if (!m_homeChunks.containsKey(identifier))
            System.out.println("DEBUG: The home chunk you're trying to add IP doesn't exist!");
        else
        {
            m_homeChunks.get(identifier).add(address);
            saveState();
        }
    }

    @Override
    synchronized public void addStoredChunkIP(Chunk chunk, String address)
    {
        if (!m_storedChunks.containsKey(chunk))
            System.out.println("DEBUG: The stored chunk you're trying to add IP doesn't exist!");
        else
        {
            m_storedChunks.get(chunk).add(address);
            saveState();
        }
    }

    @Override
    synchronized public void addTemporarilyStoredChunkIP(Chunk chunk, String address)
    {
        if (!m_tempStoredChunks.containsKey(chunk))
            System.out.println("DEBUG: The temporarily stored chunk you're trying to add IP doesn't exist!");
        else
        {
            m_tempStoredChunks.get(chunk).add(address);
            saveState();
        }
    }

    @Override
    synchronized public void removeStoredChunkIP(Chunk identifier, String address)
    {
        if (m_storedChunks.containsKey(identifier))
        {
            m_storedChunks.get(identifier).remove(address);
            saveState();
        }
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

        saveState();
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

    @Override
    synchronized public long getFreeSpace()
    {
        return m_freeStorage;
    }

    @Override
    synchronized public void decreaseFreeSpace(long value)
    {
        if (value < 0 || value > m_freeStorage)
        {
            System.out.println("Cannot decrease negative values in free space!");
            return;
        }

        m_freeStorage -= value;

        saveState();
    }

    @Override
    synchronized public void cleanUnnacessaryChunks()
    {
        // Remove all home chunks that have the following fileId:
        for (Iterator<Map.Entry<Chunk, HashSet<String>>> it = m_storedChunks.entrySet().iterator(); it.hasNext(); )
        {
            Map.Entry<Chunk, HashSet<String>> entry = it.next();

            // If we have a higher replication degree that what we needed, delete it and send Remove message:
            if(entry.getKey().getOptimalReplicationDeg().getValue() > entry.getValue().size())
            {
                // Send remove message:
                RemovedMessage message = new RemovedMessage(new Version('1','0'), entry.getKey().getFileId(), entry.getKey().getChunkNo());
                Header header = new Header();
                header.addMessage(message);
                sendHeaderMC(header);

                it.remove();
            }
        }

        saveState();
    }

    @Override
    synchronized public void freeSpaceByDeletingChunks(long spaceToDelete)
    {
        while(spaceToDelete > 0)
        {
            Chunk toDelete = getMostReplicatedRatioChunk();

            // Send remove message:
            RemovedMessage message = new RemovedMessage(new Version('1','0'), toDelete.getFileId(), toDelete.getChunkNo());
            Header header = new Header();
            header.addMessage(message);
            sendHeaderMC(header);

            m_storedChunks.remove(toDelete);

            // Update values:
            m_freeStorage += toDelete.getData().length;
            spaceToDelete -= toDelete.getData().length;
        }

        saveState();
    }

    synchronized public Chunk getMostReplicatedRatioChunk()
    {
        float tempRatio = -1;
        Chunk returnValue = null;

        for(Chunk key: m_storedChunks.keySet())
        {
            float chunkRatio = ((float) m_storedChunks.get(key).size()) / key.getOptimalReplicationDeg().getValue();

            if (chunkRatio > tempRatio)
            {
                tempRatio = chunkRatio;
                returnValue = key;
            }
        }

        return returnValue;
    }

    synchronized private HashSet<String> getChunkFilesInDisk()
    {
        File folder = new File(Chunk.s_CHUNK_DIRECTORY);
        File[] listOfFiles = folder.listFiles();

        HashSet<String> fileIds = new HashSet<>();
        for(File file : listOfFiles)
        {
            if(file.isFile())
            {
                String name = file.getName();
                String[] split = name.split("\\.");
                if(split.length != 0)
                {
                    fileIds.add(split[0]);
                }
                else
                {
                    fileIds.add(name);
                }
            }
        }

        return fileIds;
    }

    synchronized public void validateChunkFiles() throws IOException
    {
        HashSet<String> fileIds = getChunkFilesInDisk();

        for(String fileId : fileIds)
        {
            ValidMessage message = new ValidMessage(new Version('1', '0'), new FileId(fileId));

            Header header = new Header();
            header.addMessage(message);

            System.out.println("Peer::validateChunkFiles Send VALID");
            sendHeaderMC(header);
        }
    }
}