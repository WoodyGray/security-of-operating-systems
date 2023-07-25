import org.ini4j.Ini;

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Properties;
import java.util.Random;
import java.util.StringJoiner;

public class ClientABOBA {
    private static Socket clientSocket;
    private static final int rounds = 22;
    private static BufferedReader in;
    private static BufferedWriter out;
    private static final int N = 1000;

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        for (int i = 1; i <= 3; i++) {
            tryConnection();
        }

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
    private static void tryConnection(){
        try{
            System.out.println("Connecting to client socket...");
            clientSocket = new Socket("localhost", 8002);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            System.out.println("connecting to socket complete");

            System.out.println("waiting for generate elements...");
            if (in.readLine().equals("generation basicElement & p complete")){
                System.out.println("start unboxing elements...");
                Properties properties = new Properties();
                FileInputStream file = new FileInputStream("secureChannel.properties");
                properties.load(file);

                BigInteger basicElement = new BigInteger(properties.getProperty("basicElement"));
                BigInteger p = new BigInteger(properties.getProperty("p"));
                file.close();

                System.out.println("generating secure key...");
                BigInteger secureKey = generatePrime(15);
                System.out.println("creating public key...");
                BigInteger publicKey = generateKey(basicElement, p, secureKey);

                System.out.println("accepting the interlocutor key...");
                BigInteger interlocutorPublicKey = new BigInteger( in.readLine());
                System.out.println("sending public key...");
                out.write(publicKey + "\n");
                out.flush();

                System.out.println("generating common key...");
                BigInteger commonSecureKey = generateKey(interlocutorPublicKey, p, secureKey);
                System.out.println("common key:");
                System.out.println(commonSecureKey);

                //общение
                String message = "hi, im aboba";
                System.out.println("ABOBA:"+message);
                out.write(codeIn(message, convertToBytes(commonSecureKey.toString())) + "\n");
                out.flush();
                message = codeOut(in.readLine(), convertToBytes(commonSecureKey.toString()));
                System.out.println("ADOBA:"+message);
                message = "stop dialogue";
                System.out.println("ABOBA:"+message);
                out.write(codeIn(message, convertToBytes(commonSecureKey.toString())) + "\n");
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
