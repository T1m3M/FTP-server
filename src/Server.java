import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.BufferedReader;

public class Server {
	
	public static ServerSocket DataServer = null; // The second TCP server socket
	
	public static void main(String[] args) {

		ServerSocket myServer = null; // the first TCP server socket

		try {

			// connecting the first TCP server socket to the port 1234
			myServer = new ServerSocket(1234);

		} catch (IOException e) {
			System.out.println("Can not connect to the server! :(");
			System.exit(1);
		}
		
		try {
			// connecting the second TCP server socket to the port 1555
			DataServer = new ServerSocket(1555);
		} catch (IOException e) {
			System.out.println("Data transfer connection problem! :(");
			System.exit(1);
		}

		// the thread loop for multiple clients
		while (true) {
			try {
				
				// the first TCP socket accept and initializing a thread
				Socket myConnection = myServer.accept();
				ClientHandler cHandler = new ClientHandler(myConnection);
				cHandler.start();
				
				System.out.println("\nA new client is joining the server!");

			} catch (IOException e) {
				System.out.println("Can not accept the connection! :(");
				System.exit(1);
			}

		}

	}

}

class ClientHandler extends Thread {
	private Socket client;
	private Scanner input;
	private PrintWriter output;		
	private boolean login = false;
	private Socket DataConnection;
	private OutputStream dataOutput;
	private InputStream in;

	public ClientHandler(Socket soc) {
		client = soc; // assigns the client connection to the current connection

		try {
			// gets the client's streams
			input = new Scanner(client.getInputStream());
			output = new PrintWriter(client.getOutputStream(), true);

		} catch (IOException e) {
			System.out.println("Can not assign the client's streams! :(");

		}
		

	}
	
	// to check if the user name exists in the accounts.txt file
	private String checkUsername(String msg) {
		
		File file = new File("../accounts.txt");
		BufferedReader reader = null;
		
		try {
			
			reader = new BufferedReader(new FileReader(file)); // opens the file
			String line = reader.readLine(); // reads line the first line
			
			int counter = 0;
			
			while (line != null) {
				
				// search for user name among all the users
				if(counter % 2 == 0 && msg.equals(line)) {return line;}
				
				line = reader.readLine();
				counter++;
			}
			
			reader.close();
			
		} catch (IOException e) {
			System.out.println("Can not access accounts.txt file");
		}
		
		return null; /// if no user name doesn't match any of the users (no match)

	}
	
	// to check if the password is correct for a specific user
	private String checkPassword(String user, String msg) {
		
		File file = new File("../accounts.txt");
		BufferedReader reader = null;
		
		try {
			
			reader = new BufferedReader(new FileReader(file)); // opens the file
			String line = reader.readLine(); // reads line the first line
			
			int counter = 0;
			
			while (line != null) {
				
				// search for password if match for this user name
				if(counter % 2 == 0 && user.equals(line)) {
					line = reader.readLine();
					
					if(msg.equals(line))
						return line;
				}
				
				line = reader.readLine();
				counter++;
			}
			
			reader.close();
			
		} catch (IOException e) {
			System.out.println("Can not access accounts.txt file");
		}
		
		return null; /// if no password doesn't match the username's password (uncorrect password)

	}

	// the interaction between the client and the server inside the thread
	public void run() {

		// the client credentials
		String username = "", password = "";
		
		// gets the user name from the client
		String msgIn = input.nextLine();
		
		// check if the user name exists
		username = checkUsername(msgIn);
		
		// if the user name exists ask for password
		if (msgIn.equals(username))
		{
			
			output.println("Password: ");
			msgIn = input.nextLine();
			
			// check if the password is correct
			password = checkPassword(username, msgIn);
			
			// if the password is correct alert success and update the login status
			if(msgIn.equals(password))
			{
				output.println("Login Successfully");
				System.out.println("The client logged in");
				
				login = true;
				
			}
			else {
				output.println("Login Failed and the connection will terminate");
			}
		}
		else {
			output.println("Login Failed and the connection will terminate");
		}
		

		// if the client is logged in
		while(login) {

			// getting the user's commands
			msgIn = input.nextLine();
			
			// showing the available directories for this user
			if(msgIn.equals("show my directories")) {
				
				File file = new File("../domains.txt");
				BufferedReader reader = null;
				
				String dirs = "";
				
				try {
					
					reader = new BufferedReader(new FileReader(file)); // opens the file
					String line = reader.readLine(); // reads line the first line
					
					while (line != null) {
						
						// getting the directories' names for this user
						if(line.equals(username)) {
							line = reader.readLine();
							
							while(!line.trim().equals("")) {
								dirs += line + "/\t\t";
								line = reader.readLine();
							}
							
							break;
							
						}
						
						line = reader.readLine();	
					}
					
					reader.close();
					
				} catch (IOException e) {
					System.out.println("Can not access domains.txt file");
				}
				
				// the response with the user's directories
				output.println(dirs);
				
			}
			
			// if the user wants to go inside a directory
			else if(msgIn.length() > 4 && msgIn.substring(0, 4).equals("show")) {
				
				String foldername = msgIn.substring(5);
				String folderpath = "../" + username + "/" + foldername;
				
				String files = "";
				
				File folder = new File(folderpath);
				File[] allFiles = folder.listFiles();
				
				// getting all the filenames inside this directory
				for (int i = 0; i < allFiles.length; i++) {
				  if (allFiles[i].isFile()){
				    files += allFiles[i].getName() + "\t\t";
				  }
				}

				// responds with all the filenames
				output.println(files);
				
				// if the user wants to download a file or close the directory
				while(true) {
					
					msgIn = input.nextLine();
					
					// if the user typed a filename to download
					if(files.contains(msgIn)) {
						output.println("Downloading file: " + msgIn);
						
						try {
							
							/// getting the full path of this file that user needs to download
							File myFile = new File("../" + username + "/" + foldername + "/" + msgIn);
							
							// accepting the second TCP connection to transfer the data onto
							DataConnection = Server.DataServer.accept();
							
							// the stream which the file will go through to the user
							dataOutput = DataConnection.getOutputStream();
							
							// the stream for the needed file to read the data from
							in = new FileInputStream(myFile);

							// an array to receive the file bytes into it
							byte[] b = new byte[20*1024];
							
							int i;
							
							// sending the file bytes to the client
							while((i = in.read(b)) > 0) {
								dataOutput.write(b, 0, i);
							}
							
							// closing the file sending streams once it's transfered
							dataOutput.close();
							in.close();
							
							// the response to alert success
							output.println("The file has been successfully downloaded!");

						} catch (IOException e) {
							System.out.println("Data transfer connection problem! Cannot accept the connection :(");
							System.exit(1);
						}
						
						
					}
					
					// if the connection is open and the user typed "close"
					else if (DataConnection != null && msgIn.equals("close")) {							
						try {
							// closing the second TCP socket
							DataConnection.close();
							output.println("Connection is terminated");
							
							break;

						} catch (IOException e) {
							System.out.println("Can not close the file transfer stream! :(");
							System.exit(1);
						}
					}
					
					// if the user typed an unknown filename
					else {
						output.println("Sorry, The file isn't found!");
					}
				}
				
				
			}
			
			// if user wants to logout from the account
			else if(msgIn.equals("logout")) {
				output.println("Loggin out from your account...");
				login = false;
				break;
			}
			
			// if the user entered an unknown command
			else {
				output.println("Incorrect command!");
			}
			
		}
		
		// closing the input and output streams if the user logged out
		input.close();
		output.close();
		
		
		// if the user is no longer logged in then close the first TCP connection
		if(!login) {
			try {
				client.close();
				System.out.println("The client is out!");
				
			} catch (IOException e) {
				System.out.println("Can not close the client's connection! :(");
			}
		}

	}

}