# anomaly_detection
This code is used for detecting Anomaly in the Stream Data. Prepared this as a part of CodingChallenge for Data Insight

### 1.	Summarization of the Approach: ###
   To implement the challenge, I have followed below steps.
   Steps Followed:
   1. Read the batch_log file into readInput Array List
   2. Process the input file to build a model
      * a. Create objects of class PurchaseEvents and update eventDictionary. The eventDictionary is a hashmap with the key              as user and value as an Array list of Objects of class PuchaseEvent. These objects contain the transactions as                seen in input data.
      * b. Populate the social Network which has friends of each user at a Degree of 1. is a hashmap with the key as user                and value as a Set of friends of that user.
   3. Read Streaming Data into StreamData Array List. This data is processed line by line.
   4. For each Test Event:
      - a. For a befriend/unfriend event – update the social Network of friends.
      - b.	For a purchase Event:
         I.	  Retrieve the friends of the user up to degree D.
         II.  Gather all transaction Data for the friendsList and put them in a priority Queue based on latest timestamp                     (first) and order of event(second)
         III. Calculate mean and standard deviation of the top T events and compare it with test amount.
         IV.  If the amount is found to be anomalous, write it to the output file
         V.	  Add the purchase event to eventDictionary(step 2a)


### 2.	Dependencies: ###
   I have used two external jars for processing the Json inputs and outputs.
    
   1. java-json.jar: To Read Input Files into Json Objects
      Download Location: http://www.java2s.com/Code/JarDownload/java/java-json.jar.zip
              
   2. gson-2.6.2-javadoc.jar: To write the output to Json Objects
      Download Location: https://repo1.maven.org/maven2/com/google/code/gson/gson/2.6.2/
      File to Download: gson-2.6.2-javadoc.jar


### 3.	Run instructions: ###

   The ‘src’ folder contains all the java files(*.java) containing the source code. It also contains a folder ‘jars’ which        has external java libraries used in my program. 
   The run.sh contains the script to compile and run the Java Program. The run.sh has to be modified in    case the java file    name or the input/output directory of the program changes. 
   The ‘javac’ command in run.sh is used for compiling the main program file – ‘AnomalyDetector.java’. Also, the location of      jars is specified while compiling the main program.
   The ‘java’ command in run.sh is used for running the code. Here, I have included the jars to be used, name of the java        file with source code, input files and output file.
