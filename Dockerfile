FROM openjdk:8-jdk-alpine3.9

RUN apk add wget zip gnupg

RUN wget "https://archive.apache.org/dist/maven/maven-3/3.6.1/binaries/apache-maven-3.6.1-bin.zip" -O /tmp/apache-maven-3.6.1-bin.zip \
  && unzip -d /opt/ /tmp/apache-maven-3.6.1-bin.zip \
  && rm /tmp/apache-maven-3.6.1-bin.zip

RUN wget "http://apache.mirrors.ovh.net/ftp.apache.org/dist/incubator/netbeans/incubating-netbeans/incubating-11.0/incubating-netbeans-11.0-bin.zip" -O /tmp/incubating-netbeans-java-11.0-bin.zip \
  && unzip -d /opt/ /tmp/incubating-netbeans-java-11.0-bin.zip \
  && rm /tmp/incubating-netbeans-java-11.0-bin.zip