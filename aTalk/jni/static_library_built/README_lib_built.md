This directory contains information to build the various jni static libraries used in aTalk;
during Android Studio built process using NDK for the shared libraries build in aTalk/libs.

The static libraries are built uses the shell scripts in each module sub-directory. The scripts are
for Linux/Ubuntu OS with proper development environment setup i.e.
* sudo apt-get --quiet --yes install build-essential git autoconf libtool pkg-config gperf gettext yasm python-lxml
* The compiled static libraries for each sub-module is manually copies into the aTalk/jin/<sub-module>/android.

Please refer to each sub-directory for more information.

ffmpeg
-------
* kept for reference only - not use. Refer to ffmpeg-x264 for the actual static libraries used in atalk

ffmpeg-x264
------
* Build static libraries for the various architectures used for aTalk/jin/ffmpeg

libvpx
------
* Build static libraries for the various architectures used for aTalk/jin/vpx

openssl
------
* Build static libraries for the various architectures used for aTalk/jin/openssl
