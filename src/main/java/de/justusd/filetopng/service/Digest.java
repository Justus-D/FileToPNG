package de.justusd.filetopng.service;

import org.jetbrains.annotations.NotNull;

import java.security.MessageDigest;
import java.security.MessageDigestSpi;
import java.security.NoSuchAlgorithmException;

public class Digest {

    MessageDigest messageDigest;

    public Digest() {
        this.messageDigest = this.getMdInstance();
    }

    public void update(byte[] input) {
        this.messageDigest.update(input);
    }

    public void update(byte[] input, int offset, int len) {
        this.messageDigest.update(input, offset, len);
    }

    public byte[] digest() {
        try {
            MessageDigest clone = (MessageDigest) this.messageDigest.clone();
            return clone.digest();
        } catch (CloneNotSupportedException ignored) {} // this will never happen
        return new byte[32]; // if it does: here is a byte[]
    }

    @NotNull
    private MessageDigest getMdInstance() {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            return messageDigest;
        } catch (NoSuchAlgorithmException ignored) {} // this will never happen
        return this.getMdInstance(); // yeah
    }

}
