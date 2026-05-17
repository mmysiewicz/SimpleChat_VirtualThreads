/**
 *
 *  @author Mysiewicz Michał s32528
 *
 */

package zad1;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;

import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

public
class ChatClient implements Callable<ChatClient> {

    private String host;
    private int port;
    private String id;
    private StringBuffer sb = new StringBuffer();

    private Socket socket;
    private Thread thread;
    private PrintWriter out;
    private boolean running;

    public ChatClient(String host, int port, String id) {
        this.host = host;
        this.port = port;
        this.id = id;
    }

    @Override
    public ChatClient call() {
        return this;
    }

    public void login() {
        try {

            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port));
            out = new PrintWriter(socket.getOutputStream(), true);
            running = true;

            thread = new Thread(this::readData);
            thread.start();

            send("login " + id);

        } catch (IOException e) {
            sb.append("*** ").append(e.toString()).append("\n");
        }
    }

    public void logout() {
        send("logout");
        running = false;

        try {

            if(socket != null && !socket.isClosed()) {
                socket.close();
            }
            if(thread != null) {
                thread.join();
            }

        } catch (InterruptedException | IOException e) {
            Thread.currentThread().interrupt();
            sb.append("*** ").append(e.toString()).append("\n");
        }
    }

    public void send(String request) {
        if(out != null) {
            out.println(request);
        }
    }

    public String getChatView(){
        return "== " + id + " chat view\n" + sb.toString();
    }

    public void readData() {

        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            while (running && (line = in.readLine()) != null) {
                sb.append(line).append("\n");
            }

        } catch (IOException e) {}
    }

    @Override
    public String toString(){
        return id;
    }

    public String getId(){
        return id;
    }
}



