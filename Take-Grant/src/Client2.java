import java.io.*;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;


public class Client2 {
    private static Socket clientSocket;
    private static BufferedReader in;
    private static BufferedWriter out;

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        tryConnection("client2");

    }
    private static void tryConnection(String login){
        try{
            SecureRandom random = new SecureRandom();
            clientSocket = new Socket("localhost", 8000);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));

            String messageIn = in.readLine();
            System.out.println(messageIn);
            String messageOut = console.readLine();
            out.write(messageOut + "\n");
            out.flush();

            while (!messageOut.equals("/cancel chat")){
                messageIn = in.readLine();
                System.out.println(messageIn);
                while (in.ready()) {
                    messageIn = in.readLine();
                    System.out.println(messageIn);
                }
                messageOut = console.readLine();
                out.write(messageOut + "\n");
                out.flush();
            }


        } catch (Exception e){
            System.out.println("something wrong");
            System.out.println(e);
        }finally {
            try {
                clientSocket.close();
                in.close();
                out.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
