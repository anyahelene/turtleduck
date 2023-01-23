package turtleduck.shell;

import java.nio.ByteBuffer;

import turtleduck.messaging.BaseConnection;
import turtleduck.messaging.Message;
import turtleduck.messaging.Router;
import turtleduck.shell.loader.FileManager;
import turtleduck.terminal.PseudoTerminal;
import turtleduck.util.Dict;

public class ConsoleMain {
    public static void main(String[] args) {
        PseudoTerminal terminal = new PseudoTerminal();
        terminal.terminalListener(s -> {
            System.out.print(s);
            System.out.flush();
        });
        Router router = new Router();
        String user = System.getenv("USER");
        router.init("console", user != null ? user : "tduck");
       // JavaShell jsh = new JavaShell(null, null, terminal.createCursor(), router);
        /*jsh.eval("2+2", 1, Dict.create()).onSuccess(result -> {
            System.out.println(result.toJson());
        });*/
       // jsh.inspect("turtleduck.shell.JavaShell shell; shell.eval(", -1, 0).onSuccess(result -> System.out.println(result.toJson()));
       // jsh.inspect("java.util.List", -1, 0).onSuccess(result -> System.out.println(result.toJson()));
        System.out.println(PseudoTerminal.class.getResourceAsStream("/java doc/turtleduck/terminal/PseudoTerminal__Javadoc.json"));
        System.out.println();

    }

    static class DirectConnector extends BaseConnection {
        public DirectConnector() {
            super("direct:");
        }

        @Override
        public void socketSend(Message data) {
            System.out.println(data.toJson());
        }

        @Override
        public void socketSend(Message data, ByteBuffer[] buffers) {
            System.out.println(data.toJson());
        }

    }
}
