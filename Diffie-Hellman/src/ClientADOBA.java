import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;


public class ClientADOBA {
    private static final int PORT = 8002;
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
                System.out.println("generating basic elements...");
                BigInteger p = generatePrime(20);
                BigInteger basicElement = generatePrime(p.bitLength()-1);
                System.out.println("sending via secure channel...");
                Properties properties = new Properties();
                properties.setProperty("basicElement", basicElement.toString());
                properties.setProperty("p", p.toString());
                OutputStream secureChannel = new FileOutputStream("secureChannel.properties");
                properties.store(secureChannel, "basicElement & p ");
                secureChannel.close();
                out.write("generation basicElement & p complete\n");
                out.flush();

                System.out.println("generating secure key...");
                BigInteger secureKey = generatePrime(15);
                System.out.println("crafting public key...");
                BigInteger publicKey = generateKey(basicElement, p, secureKey);

                System.out.println("sending public key...");
                out.write(publicKey + "\n");
                out.flush();
                System.out.println("accepting interlocutor key...");
                BigInteger interlocutorPublicKey = new BigInteger(in.readLine());

                System.out.println("crafting common key");
                BigInteger commonSecureKey = generateKey(interlocutorPublicKey, p, secureKey);

                System.out.println("common key:");
                System.out.println(commonSecureKey);

                String message = codeOut(in.readLine(), convertToBytes(commonSecureKey.toString()));
                while (!message.equals("stop dialogue")){
                    System.out.println("ABOBA:"+message);
                    message = "hi, im adoba";
                    System.out.println("ADOBA:"+message);
                    out.write(codeIn(message, convertToBytes(commonSecureKey.toString()))+"\n");
                    out.flush();
                    message = codeOut(in.readLine(), convertToBytes(commonSecureKey.toString()));
                }
                System.out.println("ABOBA:"+message);



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

        System.out.println(codeOut(codeIn("hello", "123".getBytes()), "123".getBytes()));

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
    public static BigInteger generateKey(BigInteger basicElement, BigInteger p, BigInteger secureKey){
        return fastPower(basicElement, secureKey).mod(p);
    }
    public static BigInteger fastPower(BigInteger base, BigInteger exponent) {
        if (exponent.equals(BigInteger.ZERO)) {
            return BigInteger.ONE;
        } else if (exponent.mod(BigInteger.TWO).equals(BigInteger.ZERO)) {
            BigInteger squareRoot = fastPower(base, exponent.divide(BigInteger.TWO));
            return squareRoot.multiply(squareRoot);
        } else {
            return base.multiply(fastPower(base, exponent.subtract(BigInteger.ONE)));
        }
    }

    public static String codeIn(String input, byte[] key ) {

        byte[] inputBytes = input.getBytes();
        byte[] outputBytes = new byte[inputBytes.length];
        for (int i = 0; i < inputBytes.length; i++) {
            outputBytes[i] = (byte) (inputBytes[i] ^ key[i % key.length]);
        }
        return new String(outputBytes);
    }

    public static String codeOut(String input, byte[] key) {
        byte[] inputBytes = input.getBytes();
        byte[] outputBytes = new byte[inputBytes.length];
        for (int i = 0; i < inputBytes.length; i++) {
            outputBytes[i] = (byte) (inputBytes[i] ^ key[i % key.length]);
        }
        return new String(outputBytes);
    }
    public static byte[] convertToBytes(String num){
        byte[] result = new byte[num.length()];
        for (int i = 0; i < num.length(); i++) {
            result[i] = Byte.parseByte(String.valueOf(num.charAt(i)));
        }
        return result;
    }
}