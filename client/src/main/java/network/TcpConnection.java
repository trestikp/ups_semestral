package network;

import javafx.scene.Parent;

import java.io.*;
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
            soc = new Socket(host, port);
            //TODO: exceptions as GUI errors + logs?
        } catch(IOException e) {
            System.out.println("IO Exception on socket creation");
            exc = true;
        } catch(IllegalArgumentException e) {
            System.out.println("Illegal argument on socket creation");
            exc = true;
        } catch(NullPointerException e) {
            System.out.println("Null pointer on socket creation");
            exc = true;
        } catch(Exception e) {
            System.out.println("Unknown socket exception!\n");
            exc = true;
            e.printStackTrace();
        }

        if(exc) return;

        try {
            reader = new BufferedReader(new InputStreamReader(soc.getInputStream()));
            writer = new PrintWriter(new OutputStreamWriter(soc.getOutputStream()));
        } catch(IOException e) {
            System.out.println("IO Exception on stream creation");
        } catch(NullPointerException e) {
            System.out.println("Null pointer on stream creation");
        } catch(Exception e) {
            System.out.println("Unknown stream exception!\n");
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            reader.close();
            writer.close();

            soc.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(TcpMessage msg) {
        try {
            writer.print(msg.toString());
            writer.flush();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessageTxt(String msg) {
        try {
            writer.print(msg);
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public TcpMessage recieveMessage() {
        String res;

        try {
            if(reader.ready()) {
                res = reader.readLine();
                if(res == null) return null;
                System.out.println(res);
                return new TcpMessage(res);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
