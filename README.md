# CSCI-2251-Final-Project

This is the repository for my CSCI 2251 (Intermediate Programming) final project

This project consists of a front end user interface designed to interact with a backend SQLite database via server/client communication. The server maintains the database,
and all necessary queries are stored on the server, while the client only acts as the interface to interact with this.

The setting is a rental management company who maintains records of clients, and information. There is both excel export functionality, along with a dummy email set up that
will email clients/tenants.

This project is intended to run on machines with Java SDK 1.8 in their classpath as per the course requirements. 

To run:

Store Client.jar, Server.jar and FinalRental.db in the same folder

In one instance of terminal/command line, type 'java -jar Server.jar'

In another instance type 'java -jar Client.jar'

Type: 'localhost' when prompted

The program will start and you may now interact with the database. 
