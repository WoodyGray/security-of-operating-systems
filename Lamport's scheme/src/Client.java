import org.ini4j.Ini;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

public class Client {
    private static Socket clientSocket;
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
            clientSocket = new Socket("localhost", 8001);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            String message = in.readLine();
            System.out.println(message);
            message = bytesToHex(Hashing(N - Integer.parseInt(message), login + pass));
            System.out.println(message);
            out.write(message + "\n");
            out.flush();
            message = in.readLine();
            if (message.equals("1")){
                System.out.println("authorization success");
            } else if (message.equals("0")) {
                System.out.println("authorization false");
            }else if (message.equals("-1")){
                System.out.println("u need registration again");
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
}
