# JavaRainforestEMU-2
Java library for communicating with a Rainforest EMU-2 under Linux via USB serial.

I'm just getting a start on this library, if you have experience with these modules, please don't hesitate to contribute!

A quick guide to using the software.

Compiling:

     You will need Apache Maven installed, I'm using that for the build management.
     I am using the rxtx-osgi serial library https://mvnrepository.com/artifact/org.rxtx/rxtx-osgi?repo=opennms
          Under Ubuntu I needed to install librxtx-java version 2.2pre2 (which is why I used the OSGi variant)
          You might need to experiment with different versions of rxtx on other platforms.
     
     To build the package and create a runnable command line jar in ./target
         run "mvn clean install"
           
     To execute 
         java -jar JavaRainforestEMU2.{version}.jar -p /dev/ttyACM0

         (You may need to change the port it's connecting to, mine was ttyACM0)

     Required libraries are in the ./target/lib folder, if you want to copy it elsewhere. 
         
     The -c option will eventually cause the command line app to send it's JSON output to my home automation controller,
     so that I can log and manage household power use automagically.  It's also on github, feel free to take a look.


PosiCat
