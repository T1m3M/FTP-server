import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {

	public static void main(String[] args) {

		InetAddress host = null;
		boolean login = false; // login status

		try {
			host = InetAddress.getLocalHost();

		} catch (UnknownHostException e) {
			System.out.println("Can not connect to the host! :(");
			System.exit(1);
		}
		
		Scanner input = null; // for user input

		Socket myConnection = null; // the first TCP socket
		Scanner fromServer = null; // the response of the server stream
		PrintWriter toServer = null; // the request to the server stream
		
		Socket dataConnection = null; // the second TCP socket
		
		String request = "", response = "";

		try {
			myConnection = new Socket(host, 1234);

			fromServer = new Scanner(myConnection.getInputStream()); // gets the response
			toServer = new PrintWriter(myConnection.getOutputStream(), true); // sends the request

			input = new Scanner(System.in);
				
			System.out.print("Username: ");
			
			/// gets the user name and sends it to the server
			request = input.nextLine();
			toServer.println(request);

			/// receives the response and prints it
			response = fromServer.nextLine();
			System.out.print(response);
			
			/// if user name is correct then it will ask for the password
			if(!response.equals("Login Failed and the connection will terminate"))
			{
				
				/// gets the password from the user and sends it to the server
				request = input.nextLine();
				toServer.println(request);
				
				/// receives the response from the server and prints it
				response = fromServer.nextLine();
				System.out.println("\n" + response);
				
				if(response.equals("Login Successfully")) {
					login = true;
				}
			}
			else {
				System.out.println('\n');
			}
			
			// when the user is logged in
			while(login) {
				
				System.out.print("> ");
				
				/// gets the user command and sends it to the server
				request = input.nextLine();
				toServer.println(request);
				
				response = fromServer.nextLine();
				System.out.println(response);
				
				// if the server is sending a file
				if(response.substring(0, 18).equals("Downloading file: ")) {
					dataConnection = new Socket(host, 1555);
					
					// the stream which the file will go onto
					InputStream dataInput = dataConnection.getInputStream();
					
					// creating a file with the same name as the original file
					OutputStream out = new FileOutputStream(response.substring(18));
					
					// an array to receive the file bytes into it
					byte[] b = new byte[20 * 1024];
					
					int i;
					
					// writing to the file
					while((i = dataInput.read(b)) > 0) {
						out.write(b, 0, i);
					}
					
					// closing the file receiving streams once it's transfered
					dataInput.close();
					out.close();
					
					// receiving the status
					response = fromServer.nextLine();
					System.out.println(response);
					
					// closing the second TCP if the user command is "close"
					if(response.equals("Connection is terminated")) {dataConnection.close();}
					
				}
				
				// if the user logged out
				else if (response.equals("Loggin out from your account...")) {
					System.out.println("Logged out!\n");
					login = false;
					break;
				}
				
			}

		} catch (IOException e) {
			System.out.println("Error! Connection problem! :(");
			System.exit(1);

		}
		
		// if the user is logged out close the connections and the socket
		if(!login) {
			try {
				
				System.out.println("Closing the connection...");
				fromServer.close();
				toServer.close();
				input.close();
				myConnection.close();
				System.out.println("The connection is closed!");
				
			} catch (IOException e) {
				System.out.println("Error! Can not close the connection :(");
				System.exit(1);
			}
			
		}
		
		
		

	}

}
