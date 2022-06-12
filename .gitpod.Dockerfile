FROM gitpod/workspace-full-vnc

RUN curl -o /tmp/netbeans-13-bin.zip https://downloads.apache.org/netbeans/netbeans/13/netbeans-13-bin.zip \
  && unzip -d /home/gitpod/ /tmp/netbeans-13-bin.zip \
  && rm /tmp/netbeans-13-bin.zip

# to avoid java.lang.UnsatisfiedLinkError: /home/gitpod/.sdkman/candidates/java/11.0.13.fx-zulu/lib/libawt_xawt.so: libXtst.so.6: cannot open shared object file: No such file or directory
RUN sudo apt install libxtst6

COPY .gitpod/settings.xml /home/gitpod/.m2/settings.xml