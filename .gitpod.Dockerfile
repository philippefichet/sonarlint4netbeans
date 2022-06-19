FROM gitpod/workspace-full-vnc:2022-06-17-15-14-36

RUN curl -o /tmp/netbeans-13-bin.zip https://archive.apache.org/dist/netbeans/netbeans/13/netbeans-13-bin.zip \
  && unzip -d /home/gitpod/ /tmp/netbeans-13-bin.zip \
  && rm /tmp/netbeans-13-bin.zip

# to avoid java.lang.UnsatisfiedLinkError: /home/gitpod/.sdkman/candidates/java/11.0.13.fx-zulu/lib/libawt_xawt.so: libXtst.so.6: cannot open shared object file: No such file or directory
RUN sudo apt install libxtst6

COPY .gitpod/settings.xml /home/gitpod/.m2/settings.xml