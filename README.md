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

## License

This project is licensed under the GNU General Public License v3.0 (GPLv3).
The full license text is available in the [LICENSE](LICENSE) file.

The licenses of third-party libraries included in this project are available
in [THIRD_PARTY_LICENSES.md](THIRD_PARTY_LICENSES.md).
