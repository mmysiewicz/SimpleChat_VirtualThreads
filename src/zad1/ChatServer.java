/**
 *
 *  @author Mysiewicz Michał s32528
 *
 */

package zad1;


import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public
    class ChatServer {


    private int port;

    private Thread serverThread;
    private ServerSocket serverSocket;
    private ExecutorService executor;
    private boolean running = true;

    private Map<Socket, String> clients = new ConcurrentHashMap<>();
    private Map<Socket, PrintWriter> clientWriters = new ConcurrentHashMap<>();

    private StringBuilder sb = new StringBuilder();
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss.SSSSSSSSS");


    public ChatServer(int port) {
        this.port = port;
    }

    public void startServer() {
        running = true;
        executor = Executors.newVirtualThreadPerTaskExecutor();
        serverThread = Thread.startVirtualThread(() -> {
            serviceConnections();
        });
        System.out.println("Server started\n");
    }

    public void stopServer() {
        running = false;

        broadcast("ChatServer: chat closed");

        synchronized (sb){
            sb.append(now()).append(" ChatServer: chat closed\n");
        }

        try{
            if(serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch(IOException ex) {}

        if(serverThread != null) {
            serverThread.interrupt();
            try {
                serverThread.join();
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        if(executor != null) {
            executor.shutdownNow();
        }

        for (Socket socket : clients.keySet()) {
            try{
                socket.close();
            } catch(IOException ex) {}
        }
        System.out.println("Server stopped");
    }

    private String now(){
        return LocalTime.now().format(dtf);
    }

    public void serviceConnections() {
        try{
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(port));


            while(running && !Thread.currentThread().isInterrupted()) {
                Socket clientSocket = serverSocket.accept();

                executor.submit(() -> serviceRequest(clientSocket));
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void serviceRequest(Socket clientSocket) {
        try(BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            clientWriters.put(clientSocket, out);
            String requestString = "";
            while(running && (requestString = in.readLine()) != null) {
                requestString = requestString.trim();
                if(!requestString.isEmpty()) {
                    processCommand(clientSocket, requestString);
                }
            }

        } catch (IOException e) {
        }
    }

    public void processCommand(Socket clientSocket, String requestString) throws IOException {
        String broadcastString = "";
        boolean doLogout = false;

        if(requestString.startsWith("login ")){
            String id = requestString.substring(6);
            clients.put(clientSocket, id);
            broadcastString = id + " logged in";
        } else if(requestString.startsWith("logout")){
            String id = clients.get(clientSocket);
            broadcastString = id + " logged out";
            doLogout = true;
        } else {
            String id = clients.getOrDefault(clientSocket, "Unknown");
            broadcastString = id + ": " + requestString;
        }

        synchronized (sb){
            sb.append(now()).append(" ").append(broadcastString).append("\n");
        }
        broadcast(broadcastString);

        if(doLogout){
            closeConnection(clientSocket);
        }
    }

    public void broadcast(String message){

        for(PrintWriter out : clientWriters.values()){
            try {
                out.println(message);
            } catch (Exception e) {}
        }
    }

    public void closeConnection(Socket clientSocket){
        try{
            clients.remove(clientSocket);
            clientWriters.remove(clientSocket);
            if(clientSocket != null && !clientSocket.isClosed()){
                clientSocket.close();
            }
        } catch (IOException e) {
        }
    }

    public String getServerLog() {
        return sb.toString();
    }
}
