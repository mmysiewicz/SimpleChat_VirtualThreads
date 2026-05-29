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


            thread = new Thread(this::readData);
            thread.start();

            send("login " + id);

        } catch (IOException e) {

        }
    }

    public void logout() {
        send("logout");
    }

    public void send(String request) {
        if(out != null) {
            out.println(request);
        }
    }

    public String getChatView(){
        return sb.toString();
    }

    public void readData() {

        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            while ((line = in.readLine()) != null) {
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



