import java.net.*;
import java.io.*;

public class TextEditor {

    // Initializing commands to be.
    private final String USER = "USER";
    private final String PASS = "PASS";
    private final String WRTE = "WRTE";
    private final String APND = "APND";
    private final String UPDT = "UPDT";
    private final String EXIT = "EXIT";

    // Java control socket and I/O stream creation
    private Socket controlSocket;
    private BufferedReader controlReader;
    private DataOutputStream outputStream;
    private OutputStreamWriter outputToServer;

    private final String username = "bilkentstu";
    private final String password = "cs421s2020";

    int version = 0;

    // Constructor
    public TextEditor(final String address, final int controlPort)
    {
        // Establishing a connection
        try
        {
            controlSocket = new Socket(address, controlPort);
            System.out.println("Connected");

            // Initializing taking input from the user and outputting result
            controlReader = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
            outputStream = new DataOutputStream(controlSocket.getOutputStream());
            outputToServer = new OutputStreamWriter(controlSocket.getOutputStream());

            // Send username to the server for authentication
            outputStream.writeBytes(USER + " " + username + "\r\n");
            outputStream.flush();
            System.out.println(USER + ": " + controlReader.readLine());

            // Sending password to the server for authentication
            outputStream.writeBytes(PASS + " " + password + "\r\n");
            outputStream.flush();
            System.out.println(PASS + ": " + controlReader.readLine());
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    private void writeToFile(int lineNumber, String phrase)
    {
        // Command to be sen to server side.
        String command = WRTE + " " + version + " " + lineNumber + " " + phrase + "\r\n";
        // Try sending information to server.
        try
        {
            // outputToServer = new OutputStreamWriter(controlSocket.getOutputStream());
            outputToServer.write(command, 0, command.length());
            outputToServer.flush();

            InputStreamReader inputStreamReader = new InputStreamReader(controlSocket.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            System.out.println(WRTE + ": " + bufferedReader.readLine());

            // System.out.println(WRTE + ": " + controlReader.readLine());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void appendFile(String phrase)
    {
        String command = APND + " " + version + " " + phrase + "\r\n";
        try
        {
            // outputToServer = new OutputStreamWriter(controlSocket.getOutputStream());
            outputToServer.write(command, 0, command.length());
            outputToServer.flush();

            InputStreamReader inputStreamReader = new InputStreamReader(controlSocket.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            System.out.println(APND + ": " + bufferedReader.readLine());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void updateFile()
    {
        // Creating the String command to send to the server side.
        String command = UPDT + " " + version + "\r\n";
        // sending the command to server
        try
        {
            // outputToServer = new OutputStreamWriter(controlSocket.getOutputStream());
            outputToServer.write(command, 0, command.length());
            outputToServer.flush();

            InputStreamReader inputStreamReader = new InputStreamReader(controlSocket.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            // System.out.println(UPDT + ": " + bufferedReader.readLine());

            // Response from the server
            String response = bufferedReader.readLine();
            System.out.println(UPDT + ": " + response);

            if (response.contains("INVALID"))
            {
                System.out.println("Up to date...");
            }
            else if (response.length() == 0)
            {
                System.out.println("Error, please check write or append operation");
            }
            else
            {
                String[] divided_response = response.split(" ");
                version = Integer.parseInt(divided_response[1]);
                System.out.println("Updated the version...");
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void exit()
    {
        String command = EXIT + "\r\n";

        try
        {
            outputToServer.write(command, 0, command.length());
            outputToServer.flush();

            InputStreamReader inputStreamReader = new InputStreamReader(controlSocket.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            System.out.println(EXIT + ": " + bufferedReader.readLine());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void closeSockets()
    {
        try
        {
            Thread.sleep(250);
            controlSocket.close();
            controlReader.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        if (args.length != 2)
        {
            System.out.println("Invalid format: TextEditor <Addr> <PORT>");
            return;
        }

        String host = args[0];
        int controlPort = Integer.parseInt(args[1]);
        TextEditor client = new TextEditor(host, controlPort);

        try
        {
            client.updateFile();
            client.writeToFile(5, "The Dark Side of The Moon");
            client.updateFile();
            client.writeToFile(12, "Another Brick in the Wall");
            client.updateFile();
            client.appendFile("Train of Thought");
            client.updateFile();
            client.exit();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        client.closeSockets();
    }
}
