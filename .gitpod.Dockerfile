FROM gitpod/workspace-full-vnc

RUN curl -o /tmp/netbeans-13-bin.zip https://downloads.apache.org/netbeans/netbeans/13/netbeans-13-bin.zip \
  && unzip -d /home/gitpod/ /tmp/netbeans-13-bin.zip \
  && rm /tmp/netbeans-13-bin.zip