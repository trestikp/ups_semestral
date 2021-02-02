package network;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

public class TcpConnection {
    private Socket soc;
    BufferedReader reader;
    PrintWriter writer;
    int port;
    String host;

    public TcpConnection(String host, int port) {
        open(host, port);
    }

    public Socket getSoc() {
        return soc;
    }

    private void open(String host, int port) {
        this.host = host;
        this.port = port;

        boolean exc = false;

        try {
//            soc = new Socket(host, port);
            soc = new Socket();
            soc.connect(new InetSocketAddress(host, port), 5000);
        } catch(IOException e) {
            System.err.println("IO Exception on socket creation");
            exc = true;
        } catch(IllegalArgumentException e) {
            System.err.println("Illegal argument on socket creation");
            exc = true;
        } catch(NullPointerException e) {
            System.err.println("Null pointer on socket creation");
            exc = true;
        } catch(Exception e) {
            System.err.println("Unknown socket exception!\n");
            exc = true;
            e.printStackTrace();
        }

        if(exc) return;

        try {
            reader = new BufferedReader(new InputStreamReader(soc.getInputStream()));
            writer = new PrintWriter(new OutputStreamWriter(soc.getOutputStream()));
        } catch(IOException e) {
            System.err.println("IO Exception on stream creation");
        } catch(NullPointerException e) {
            System.err.println("Null pointer on stream creation");
        } catch(Exception e) {
            System.err.println("Unknown stream exception!\n");
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            reader.close();
            writer.close();

            soc.close();
        } catch (IOException e) {
            System.err.println("IO Exception on closing");
        } catch(Exception e) {
            System.err.println("Failed to safely close connection");
        }
    }

    public void sendMessageTxt(String msg) {
        if(!msg.contains("PING"))
            System.out.println("Sending: " + msg);

        try {
            writer.print(msg);
            writer.flush();
        } catch (Exception e) {
            System.err.println("Failed to send message");
        }
    }

    public TcpMessage receiveMessage() {
        String res;

        try {
            if(reader.ready()) {
                res = reader.readLine();
                if(res == null) return null;

                if(!res.contains("PING"))
                    System.out.println(res);

                return new TcpMessage(res);
            }
        } catch (IOException e) {
            System.err.println("Failed to receive message");
        }

        return null;
    }
}
