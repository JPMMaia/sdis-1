package net.chunks;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by João on 25/03/2015.
 */
public class FileId
{
    private static final int s_MAX_SIZE = 64;
    private String m_value;

    public FileId(String value) throws InvalidParameterException
    {
        if(value.length() != s_MAX_SIZE)
            throw new InvalidParameterException("FileId::constructor: Value must have a length of 64!");

        m_value = value;
    }

    public FileId(File file, byte[] fileData) throws UnsupportedEncodingException, NoSuchAlgorithmException
    {
        String bitString = file.getAbsolutePath() + file.lastModified() + new String(fileData, StandardCharsets.US_ASCII);

        // Encrypt:
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(bitString.getBytes(StandardCharsets.US_ASCII));
        m_value = FileId.hashToString(md.digest());
    }

    @Override
    public int hashCode()
    {
        return m_value.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof FileId))
            return false;
        else
            return this.m_value.equals(((FileId) obj).m_value);
    }

    @Override
    public String toString()
    {
        return m_value;
    }

    public String getValue()
    {
        return m_value;
    }

    public void setValue(String value)
    {
        m_value = value;
    }

    private static String hashToString(byte[] hash)
    {
        StringBuilder builder = new StringBuilder(hash.length * 2);

        for (byte b : hash)
        {
            byte lsb = (byte)(b % (byte) 16);
            if(lsb < 0)
                lsb -= 0xF0;
            byte msb = (byte)(b >> 4);
            if(msb < 0)
                msb -= 0xF0;

            builder.append(byteToChar(msb));
            builder.append(byteToChar(lsb));
        }

        return builder.toString();
    }

    private static char byteToChar(byte b)
    {
        if(b >= 0 && b <= 9)
            return (char) ((char) b + '0');

        return (char) ((char) b + 'A' - 0x0A);
    }
}
