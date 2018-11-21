# SWEN-789-Load-Balancer
Project For SWEN-789 Fall 2018

I) To run the project simply run TestServer.java
It will take maximum connection each web server can accept as 10, by default

if you want to change the maximum connection each web server can accept:
go to program arguments, enter the number you want to assign 
or 
run from cmdline as
$java TestServer <maxServerConnections>
(Usage: java TestServer <maxServerConnections>)


II) Scripts folder added, it will however not actually do anything if Tomcat is not configured locally.
But the load balancer also won't throw an error. It assumes the script brought up/down the server.
