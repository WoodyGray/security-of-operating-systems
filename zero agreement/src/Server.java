import org.ini4j.Ini;

import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;


public class Server {
    private static final int PORT = 8001;
    private static List<ClientThread> clients = new ArrayList<>();

    public static class ClientThread extends Thread{
        private BufferedReader in;
        private BufferedWriter out;
        private Socket connection;
        public ClientThread(Socket connection) throws IOException {
            this.connection = connection;
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
            start();
        }

        @Override
        public void run() {
            try {

                BigInteger p = generatePrime(1024);
                BigInteger q = generatePrime(1024);
                BigInteger n = p.multiply(q);
                out.write(n+ "\n");
                out.flush();

                BigInteger V = new BigInteger(in.readLine());

                boolean runUsl = true;
                String message = "continue the authorization";
                while (runUsl && message.equals("continue the authorization")) {
                    BigInteger X = new BigInteger(in.readLine());

                    int e = (int) Math.round(Math.random());
                    out.write(e + "\n");
                    out.flush();

                    BigInteger Y = new BigInteger(in.readLine());
                    message = Y.pow(2).mod(n).equals(X.multiply(V.pow(e)).mod(n)) ? "accept" : "fail";
                    runUsl = message.equals("accept");
                    out.write(message + "\n");
                    out.flush();
                    message = in.readLine();
                }
                if (message.equals("authorization success")){
                    System.out.println("authorization success");
                }


            } catch (IOException e) {
                System.out.println("error1");
                throw new RuntimeException(e);
            }finally {
                try {
                    clients.remove(this);
                    connection.close();
                    in.close();
                    out.close();

                }catch (Exception e){

                }
            }
        }
    }


    public static void main(String[] args) throws IOException {

        boolean runUsl = true;
        ServerSocket server = new ServerSocket(PORT);
        clients = new ArrayList<>();
        System.out.println("Server running...");
        while (runUsl){
            try{
                Socket clientConn = server.accept();
                clients.add(new ClientThread(clientConn));
                System.out.println("new client connect");
            }catch (Exception e){

            }

        }
    }

    public static BigInteger generatePrime(int size) {
        BigInteger prime;
        Random rnd = new Random();

        // Генерируем случайное нечетное число заданного размера бит
        BigInteger num = new BigInteger(size, rnd).setBit(size - 1).setBit(0);

        // Проверяем, является ли число простым
        while (!num.isProbablePrime(100)) {
            // Увеличиваем число на 2, чтобы получить следующее нечетное число
            num = num.add(BigInteger.TWO);
        }

        prime = num;
        return prime;
    }
}