# anomaly_detection
This code is used for detecting Anomaly in the Stream Data. Prepared this as a part of CodingChallenge for Data Insight

### 1.	Summarization of the Approach: ###
   To implement the challenge, I have followed below steps.
   Steps Followed:
   1. Read the batch_log file into readInput Array List
   2. Process the input file to build a model
      * Create objects of class PurchaseEvents and update eventDictionary. The eventDictionary is a hashmap with the key               as user and value as an Array list of Objects of class PuchaseEvent. These objects contain the transactions as                 seen in input data.
      * Populate the social Network which has friends of each user at a Degree of 1. is a hashmap with the key as user                 and value as a Set of friends of that user.
   3. Read Streaming Data into StreamData Array List. This data is processed line by line.
   4. For each Test Event:  
      - For a befriend/unfriend event – update the social Network of friends.
      - For a purchase Event:
       1. Retrieve the friends of the user up to degree D.
       2. Gather all transaction Data for the friendsList and put them in a priority Queue based on latest timestamp (first) and order of event(second).
       3. Calculate mean and standard deviation of the top T events and compare it with test amount.
       4. If the amount is found to be anomalous, write it to the output file.
       5. Add the purchase event to eventDictionary(step 2a).

### 2.	Dependencies: ###
   I have used two external jars for processing the Json inputs and outputs.
    
   1. java-json.jar: To Read Input Files into Json Objects
      Download Location: http://www.java2s.com/Code/JarDownload/java/java-json.jar.zip
              
   2. gson-2.6.2-javadoc.jar: To write the output to Json Objects
      Download Location: https://repo1.maven.org/maven2/com/google/code/gson/gson/2.6.2/
      File to Download: gson-2.6.2-javadoc.jar

### 3.	Run instructions: ###

   The ‘src’ folder contains all the java files(*.java) containing the source code. It also contains a folder ‘jars’ which        has external java libraries used in my program.  
   The run.sh contains the script to compile and run the Java Program. The run.sh has to be modified in case the java file        name or the input/output directory of the program changes.  
   The ‘javac’ command in run.sh is used for compiling the main program file – ‘AnomalyDetector.java’. Also, the location        of jars is specified while compiling the main program.  
   The ‘java’ command in run.sh is used for running the code. Here, I have included the jars to be used, name of the java file    with source code, input files and output file.
   
   ### 4. Alternative Algorithm ###
   For adding a new friends to the social network,instead of rebuilding the whole social network, we can just change the      necessary details in the social network.

   Let's assume A and B become friends and there is a social network that exists already.

   1. Chech depth of B in A's network. Since it will be greater than 1, change it to 1. Also keep track of previous neighbour via which A can reach B, which in this case would be B itself. Previous neighbour helps when we need to delete.
   2. In A's social network, check all the nodes and see at what depth they are connected to B with. If depth is greater than 2, change their depths to 2, change their previous neighbour to A and add them to a queue of nodes that need to be updated. Once all in A's network are updated, we pop top element in queue, set the depth to 2, and see in their network where B is. If B is now at a depth of more than 3, we do the same, with previous neighbour being the node itself instead of A and depth being 3. 
   3. Do this recursively until the queue is empty, or we reach our max depth of T.  
   
   For B we need to do the same steps to integrate A to its list. 

