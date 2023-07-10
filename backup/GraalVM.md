
 wget https://download.oracle.com/graalvm/17/latest/graalvm-jdk-17_linux-x64_bin.tar.gz 
 wget https://download.oracle.com/graalvm/20/latest/graalvm-jdk-20_linux-x64_bin.tar.gz

 export PATH=graalvm-jdk-17.0.7+8.1/bin:$PATH
 export JAVA_HOME=/home/user/graalvm-jdk-17.0.7+8.1
 java -version