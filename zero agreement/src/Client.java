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
        Ini ini = new Ini(new File("D:\\study\\ВлГУ\\BOS\\3 лаба\\java\\untitled\\src\\configLog.ini"));

        for (int i = 1; i <= 3; i++) {
            String var = "var1." + i;
            if (ini.get(var, "choice").equals("log")){
                tryAuthorization(ini.get(var, "login"), ini.get(var, "password"));
            }
        }
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
    private static void tryAuthorization(String login, String pass){
        try{
            SecureRandom random = new SecureRandom();
            clientSocket = new Socket("localhost", 8001);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            //1 этап
            BigInteger n = new BigInteger(in.readLine());
            //2 этап
            BigInteger secret = generatePrime(n.bitLength());
            BigInteger V = secret.pow(2).mod(n);
            int i = 0;
            String message = " ";
            out.write(V + "\n");
            out.flush();
            while (i != rounds && !message.equals("stop the authorization")) {
                BigInteger r = new BigInteger(n.bitLength(), random);
                while (r.compareTo(n) >= 0) {
                    r = new BigInteger(n.bitLength(), random);
                }
                BigInteger X = r.pow(2).mod(n);
                out.write(X + "\n");
                out.flush();
                //3 Этап
                int e = Integer.parseInt(in.readLine());
                BigInteger Y = e == 0 ? r : r.multiply(secret.pow(e)).mod(n);
                out.write(Y + "\n");
                out.flush();

                message = in.readLine();
                if (message.equals("fail")) {
                    System.out.println("authorization failed");
                    message = "stop the authorization";
                } else {
                    System.out.println("authorization continue...");
                    message = "continue the authorization";
                }
                i++;
                if (i == rounds){
                    message = "authorization success\n";
                }
                out.write(message + "\n");
                out.flush();
            }
            if (i == rounds) {
                System.out.println("authorization success");
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
