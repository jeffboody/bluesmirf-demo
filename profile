# App
export APP=BlueSmirfDemo

# Update SDK to point to the Android SDK
SDK=/home/jeffboody/android/android-sdk-linux_86

#-- DON'T CHANGE BELOW LINE --

export PATH=$SDK/tools:$SDK/platform-tools:$PATH
echo "sdk.dir=$SDK" > project/local.properties

export TOP=`pwd`
alias croot='cd $TOP'
