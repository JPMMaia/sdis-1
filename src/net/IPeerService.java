package net;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.InvalidParameterException;

/**
 * Created by João on 27/03/2015.
 */
public interface IPeerService extends Remote
{
    String backupFile(String filename, int replicationDeg) throws RemoteException;
    String printBackupFiles() throws RemoteException;
    String restoreFile(int fileIndex) throws RemoteException;
    String deleteFile(int fileIndex) throws RemoteException;
    String setMaxDiskSpace(int bytes) throws RemoteException;
    String info() throws RemoteException;
}
