FROM gitpod/workspace-full-vnc

RUN curl -o /tmp/netbeans-13-bin.zip https://downloads.apache.org/netbeans/netbeans/13/netbeans-13-bin.zip \
  && unzip -d /home/gitpod/ /tmp/netbeans-13-bin.zip \
  && rm /tmp/netbeans-13-bin.zip

# https://docs.oracle.com/javase/8/docs/technotes/guides/2d/flags.html#xrender
# not proprely render with VNC with true by default
RUN sed -i -e 's#netbeans_default_options="#netbeans_default_options="-J-Dsun.java2d.xrender=false #' /home/gitpod/netbeans/etc/netbeans.conf