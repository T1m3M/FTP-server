# FTP Server
### June 2020

Note: This was the final project I made for the Netowrks-1 course by Java programming language

I used socket-programming to program a server which has directories and files that can be accessed through the clients .. The authentication is through the accounts.txt file (Username followed by the password) and the scope of each user is determined by domains.txt file (Username followed by the permitted directories).

## commands:
- `show my directories`: showing the domain of the user which can be accessed
- `show <directory name>`: going inside a directory and printing the content of it
- `<filename>`: downloading the file for the client
- `close`: terminating the connection between the client/server
- `logout`: logging out from the user account
