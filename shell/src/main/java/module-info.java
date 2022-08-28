module turtleduck.shell {
    exports turtleduck.shell;

    requires java.logging;
    requires jdk.jshell;
    requires transitive turtleduck.base;
    requires org.objectweb.asm;
    requires org.objectweb.asm.util;
    requires org.apache.commons.text;
    requires org.slf4j;
    requires turtleduck.anno;
    requires jdk.javadoc;
    requires java.compiler;
    requires org.eclipse.jetty.websocket.jetty.common;
    requires org.eclipse.jetty.websocket.servlet;
    requires org.eclipse.jetty.websocket.jetty.api;
    requires org.eclipse.jetty.websocket.jetty.server;
    requires org.eclipse.jetty.server;
    requires org.eclipse.jetty.servlet;
}