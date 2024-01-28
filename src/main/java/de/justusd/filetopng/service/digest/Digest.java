/*
 * FileToPNG
 * Copyright (c) 2024  Justus Dietrich <git@justus-d.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package de.justusd.filetopng.service.digest;

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
