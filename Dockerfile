FROM ubuntu:18.04
 
RUN apt-get update \
    && apt-get upgrade -y \
    && apt-get install -y wget zip ccze \
    && apt-get clean
RUN wget "http://apache.mirrors.ovh.net/ftp.apache.org/dist/incubator/netbeans/incubating-netbeans/incubating-11.0/incubating-netbeans-11.0-bin.zip" -O /tmp/incubating-netbeans-java-11.0-bin.zip \
  && unzip -d /opt/ /tmp/incubating-netbeans-java-11.0-bin.zip \
  && rm /tmp/incubating-netbeans-java-11.0-bin.zip
 
RUN apt-get -y install openjdk-11-jdk && apt-get clean
