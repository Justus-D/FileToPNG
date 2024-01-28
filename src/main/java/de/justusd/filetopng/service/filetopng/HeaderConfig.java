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
package de.justusd.filetopng.service.filetopng;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

class HeaderConfig {

    private final Map<String, String> config = new HashMap<>();
    private final int part;
    private final long contentLength;
    private final long fileSize;

    public HeaderConfig(byte[] bytes) throws IOException {
        String rawConfig = getStringFromBytes(bytes);
        String[] values = rawConfig.split(";");
        for (String value : values) {
            String[] kv = value.split("=");
            if (kv.length == 2) {
                config.put(kv[0], kv[1]);
            }
        }
        this.part = this.getIntValue("part");
        this.fileSize = this.getLongValue("fileSize");
        this.contentLength = this.getIntValue("contentLength");
    }

    public static String getStringFromBytes(byte[] bytes) { // nullbyte-terminated
        int j;
        for (j = 0; j < bytes.length && bytes[j] != (byte) 0x00; j++) continue;
        return new String(bytes, 0, j, StandardCharsets.UTF_8);
    }

    public int getIntValue(String key) throws IOException {
        try {
            return Integer.parseInt(this.config.get(key));
        } catch (NumberFormatException e) {
            throw new IOException("Header corrupt: could not get int value for '" + key + "'.");
        }
    }

    public long getLongValue(String key) throws IOException {
        try {
            return Long.parseLong(this.config.get(key));
        } catch (NumberFormatException e) {
            throw new IOException("Header corrupt: could not get long value for '" + key + "'.");
        }
    }

    public UUID getUUID() throws IOException {
        try {
            return UUID.fromString(this.config.get("UUID"));
        } catch (IllegalArgumentException e) {
            throw new IOException("Header corrupt: could not parse UUID.");
        }
    }

    public int getPart() {
        return this.part;
    }

    public long getContentLength() {
        return this.contentLength;
    }

    public long getFileSize() {
        return this.fileSize;
    }

    public int getEdge() throws IOException {
        return this.getIntValue("edge");
    }

    public int getPaddingTop() throws IOException {
        return this.getIntValue("paddingTop");
    }

    public int getPaddingBottom() throws IOException {
        return this.getIntValue("paddingTop");
    }

    public int getPreviousDigestIndex() throws IOException {
        return this.getIntValue("previousDigestIndex");
    }

    public int getFileNameIndex() throws IOException {
        return this.getIntValue("fileNameIndex");
    }

}
