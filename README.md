# FileToPNG

[![GitHub License](https://img.shields.io/github/license/Justus-D/FileToPNG)](LICENSE)
[![GitHub Release](https://img.shields.io/github/v/release/Justus-D/FileToPNG?sort=semver&filter=v*)](https://github.com/Justus-D/FileToPNG/releases/latest)

**FileToPNG** is a tool that lets you write the contents of a file
into a PNG image. It can also retrieve the written data later on.
A hash is being calculated while writing data to a PNG file. This
ensures that possible corruption of the data can be detected
when restoring the data from one or more PNGs. There is no limit
for the file size of a stored file. Each generated PNG can hold
up to 1 200 000 000 bytes. After that, a new PNG is used to store
further data. Each generated PNG has a file name in the format
<code>YYYY-MM-DD_hh-mm-ss_part&lt;index&gt;.png</code>.
If you want to recover the data stored in a generated PNG, you need
to select the folder in which the generated PNG files are stored.
If there is more than one PNG file which stores a file, the first
one(s) will be selected for recovery of the file.

If you find this project useful, please consider giving it a star on GitHub.

## Installation

You can download the latest release [from GitHub](https://github.com/Justus-D/FileToPNG/releases/latest).
This application comes as a `.jar` file. You will need to install Java
(at least version 21) in order to run it.

Here are some OpenJDK distributions that allow you to run Java files:
- [Azul Zulu Builds of OpenJDK](https://www.azul.com/downloads/#zulu) (Linux, macOS, Windows)
- [OpenJDK](https://jdk.java.net/21/) (Linux, macOS, Windows - no installer available)
- [Microsoft Build of OpenJDK](https://learn.microsoft.com/de-de/java/openjdk/download#openjdk-21) (Linux, Mac, Windows)

Or use your favourite search engine to find one.

On Linux you can just use your favourite package manager to install
an OpenJDK version (21+).

## Why?

This application may seem completely useless. And to some extent, it is.
I learned a lot of Java in my university and this is my first real application
written in Java. The code may look a bit chaotic, so please don't judge me.
So this is really just a bit of coding practice for me.

However, there are cloud services that let you sync an unlimited amount of
photos in original quality. You could use this program to "convert" all your
files into photos and enjoy unlimited storage space. Even though this might work,
I wouldn't recommend it, as it is highly impractical. It may also be against the
terms of service of some providers. So please check those before you upload anything.

## The format

The file format of the output files is PNG. The pixels of the PNG represent the data
of the files.

### Basics

Each pixel has three color channels: R for red, G for green and B for blue.
Each color uses one byte. So that gives us three bytes of data per pixel.

### The header

The first lines of a FileToPNG-generated PNG file always contain metadata
about the file. In the following explanation I will use `header[index]` to
describe what line I am referring to. Counting begins at 0.

#### `header[0]`: Magic bytes

The first few pixels are what I call the "magic bytes". They are there to
identify whether a PNG file was generated with this tool or not. The pixels
are (in hexadecimal):
`#64652E` `#6A7573` `#747573` `#642E66` `#696C65` `#746F70` `#6E6700`.
The bytes of those pixels put together are just the package name in the
form of (ASCII) bytes: `de.justusd.filetopng`.

#### `header[1]`: Metadata

All bytes of pixel-data must be interpreted as a UTF-8 character sequence
until a null-byte (`0x00`) occurs. This is also known as a null-byte
terminated string. The resulting string consists of multiple key-value pairs
in the format: `key1=value;key2=value;key3=value`.

The following key-value pairs will usually be present:

| key                 | data type | description (default value)                                          |
|---------------------|-----------|----------------------------------------------------------------------|
| version             | int       | version number (1)                                                   |
| part                | int       | part index                                                           |
| fileSize            | long      | total file size in bytes                                             |
| contentLength       | long      | how many bytes of data there are in this part                        |
| edge                | int       | the size of one edge of the pixels that contain data                 |
| paddingTop          | int       | how many rows of pixels there are until the file content starts (96) |
| paddingBottom       | int       | how many rows of pixels there are after the file content ends (64)   |
| digestAlgorithm     | String    | which digest algorithm to use for hashing ("SHA-256")                |
| digestLength        | int       | how many bytes the digest occupies (32)                              |
| previousDigestIndex | int       | the index of the pixel-row containing the previous digest, always 3  |
| fileNameIndex       | int       | the index of the pixel-row containing the file name, always 3        |
| UUID                | String    | the string representation of a UUID, used to identify a single file  |

#### `header[2]`: Digest of previous part

The first 32 bytes of pixel-data represent the SHA-256 hash of the content
of the previous part.

#### `header[3]`: File name

All bytes of pixel-data must be interpreted as a UTF-8 character sequence
until a null-byte (`0x00`) occurs. The resulting string is the file name.

#### The rest of the header

The remaining rows of pixels are there for visually representing metadata
about the file. They will not be parsed on recovery and are there to help
identify the contents of the PNG file.

## License

This project is licensed under the GNU General Public License v3.0 (GPLv3).
The full license text is available in the [LICENSE](LICENSE) file.

The licenses of third-party libraries included in this project are available
in [THIRD_PARTY_LICENSES.md](THIRD_PARTY_LICENSES.md).
