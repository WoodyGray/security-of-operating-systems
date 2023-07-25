import org.ini4j.Ini;

import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.StringJoiner;

public class Client {
    private static Socket clientSocket;
    private static final int rounds = 22;
    private static BufferedReader in;
    private static BufferedWriter out;
    private static final int N = 1000;

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        tryConnection("client2");
        //System.out.println(Arrays.hashCode(Hashing(1, "124")));



        /*
        try{
            clientSocket = new Socket("localhost", 6001);
            reader = new BufferedReader(new InputStreamReader(System.in));
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            boolean runUsl = true;
            String message;
            while (runUsl){
                if (reader.ready()){
                    message = reader.readLine();
                    if (message.equals("cl")){
                        out.write(message + "\n");
                        out.flush();
                        runUsl = false;
                    }else {
                        out.write(message + "\n");
                        out.flush();
                    }
                }
            }
        } catch (Exception e){
            System.out.println("Что-то пошло не так");
            System.out.println(e);
        }
         */
    }
    private static void printBytes(byte[] array){
        StringJoiner result = new StringJoiner("");
        for (byte c: array
        ) {
            result.add(String.valueOf(c));
        }
        System.out.println(result);
    }
    public static byte[] stringToByteArray (String s) {
        byte[] byteArray = new byte[s.length()/2];
        String[] strBytes = new String[s.length()/2];
        int k = 0;
        for (int i = 0; i < s.length(); i=i+2) {
            int j = i+2;
            strBytes[k] = s.substring(i,j);
            byteArray[k] = (byte)Integer.parseInt(strBytes[k], 16);
            k++;
        }
        return byteArray;
    }
    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
    private static void tryConnection(String login){
        try{
            SecureRandom random = new SecureRandom();
            clientSocket = new Socket("localhost", 8001);
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
    public static byte[] Hashing(int n, byte[] hash) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] result = messageDigest.digest(hash);
        for (int i = 0; i < n - 1; i++) {
            result = messageDigest.digest(result);
        }
        return result;
    }
    public static byte[] Hashing(int n, String line) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] result = messageDigest.digest(line.getBytes(StandardCharsets.UTF_8));
        for (int i = 0; i < n - 1; i++) {
            result = messageDigest.digest(result);
        }
        return result;
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
