package net;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.InvalidParameterException;

/**
 * Created by João on 27/03/2015.
 */
public interface PeerService extends Remote
{
    void backupFile(String filename, int replicationDeg) throws IOException, InvalidParameterException;
    void restoreFile(String filename) throws RemoteException;
    void deleteFile(String filename) throws RemoteException;
    void setMaxDiskSpace(int bytes) throws RemoteException;
}
