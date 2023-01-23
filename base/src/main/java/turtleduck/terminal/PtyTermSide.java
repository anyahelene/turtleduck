package turtleduck.terminal;

import java.util.function.Consumer;


/**
 * Terminal side of a pseudo terminal.
 * 
 * 
 * @author anya
 *
 */
public interface PtyTermSide {

    /**
     * Attach a listener that receives output from to the terminal.
     * 
     * @param listener
     */
    void terminalListener(Consumer<String> listener);

    /**
     * Send input from the terminal to the host program.
     * 
     * Input is delivered through input listeners or on stdin.
     * 
     * @param input
     */
    void writeToHost(String input);

}
