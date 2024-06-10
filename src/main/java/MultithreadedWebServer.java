import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultithreadedWebServer {

    private Socket clientSocket;
    private ServerSocket serverSocket;
    private ExecutorService executorService;

    public String readClientMessage(Socket clientSocket) throws IOException {
        System.out.println("reading client message");
        BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        StringBuilder request = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            request.append(line + "\r\n");
            if (line.isEmpty()) {
                break;
            }
        }
        System.out.println("Request: " + request.toString());
        return request.toString();
    }

    public MultithreadedWebServer(int port, int num_threads) throws IOException {
        this.clientSocket = null;
        this.serverSocket = new ServerSocket(port);
        this.serverSocket.setReuseAddress(true);
        this.executorService = Executors.newFixedThreadPool(num_threads);
    }

    public void handleConnection(Socket clientSocket) {
        try {
            System.out.println("handling connection");
            String clientMessage = readClientMessage(clientSocket);
            String outputString = RequestProcessor.handleRequest(clientMessage);
            clientSocket.getOutputStream().write(outputString.getBytes());
            clientSocket.close();
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }

    public void start() {
        while (true) {
            try {
                clientSocket = serverSocket.accept();
                // Timeout
                clientSocket.setSoTimeout(9000);
                System.out.println("accepted new connection");
                executorService.submit(() -> handleConnection(clientSocket));
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            }
        }
    }
    
}
