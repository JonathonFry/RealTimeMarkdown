rootdir=`pwd`
echo "Installing go dependencies"
go get "github.com/olahol/melody"
go get "github.com/russross/blackfriday"

echo "Installing gradle dependencies"
cd android/AndroidRealTime && ./gradlew build

cd $rootdir

echo "Installing pods"
cd ios/RealTime && pod install