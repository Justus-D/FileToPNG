/*
 * BdfUtil
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
package de.justusd.bdfutil;

public class BdfGlyph {

    private int encoding;
    private String charName;
    private int width;
    private int height;
    private boolean[][] pixelMatrix;

    BdfGlyph(BdfGlyphPrototype prototype) {
        this.encoding = prototype.encoding;
        this.charName = prototype.charName;
        this.width = prototype.width;
        this.height = prototype.height;
        this.pixelMatrix = prototype.pixelMatrix;
    }

    public boolean[][] getPixelMatrix() {
        boolean[][] pixelMatrix = new boolean[this.pixelMatrix.length][];
        for (int i = 0; i < pixelMatrix.length; i++) {
            pixelMatrix[i] = new boolean[this.pixelMatrix[i].length];
            System.arraycopy(this.pixelMatrix[i], 0, pixelMatrix[i], 0, this.pixelMatrix[i].length);
        }
        return pixelMatrix;
    }

}
