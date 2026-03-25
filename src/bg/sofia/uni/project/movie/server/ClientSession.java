package bg.sofia.uni.project.movie.server;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicReference;

public class ClientSession {
    private static final int BUFFER_SIZE = 8192;

    private final ByteBuffer readBuffer;
    private final AtomicReference<ByteBuffer> writeBuffer;
    private final StringBuilder commandBuilder;

    public ClientSession() {
        this.readBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        this.writeBuffer = new AtomicReference<>();
        this.commandBuilder = new StringBuilder();
    }

    public ByteBuffer getReadBuffer() {
        return readBuffer;
    }

    public void setResponse(String response) {
        ByteBuffer buffer = ByteBuffer.wrap((response + "\n").getBytes());
        writeBuffer.set(buffer);
    }

    public ByteBuffer getWriteBuffer() {
        return writeBuffer.get();
    }

    public void clearWriteBuffer() {
        writeBuffer.set(null);
    }

    public StringBuilder getCommandBuilder() {
        return commandBuilder;
    }

    public void appendToCommand(char c) {
        commandBuilder.append(c);
    }

    public void resetCommandBuilder() {
        commandBuilder.setLength(0);
    }

    public String getAndResetCommand() {
        String command = commandBuilder.toString().trim();
        resetCommandBuilder();
        return command;
    }
}