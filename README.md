# anomaly_detection
This code is used for detecting Anomaly in the Stream Data. Prepared this as a part of CodingChallenge for Data Insight

### 1.	Summarization of the Approach: ###
   To implement the challenge, I have followed below steps.
   1. Read the batch_log file into readInput Array List
   2. Process the input file to build a model
      * Create objects of class PurchaseEvents and update eventDictionary. The eventDictionary is a hashmap with the key               as user and value as an Array list of Objects of class PuchaseEvent. These objects contain the transactions as                 seen in input data.
      * Populate the social Network which has friends of each user at a Degree of 1. is a hashmap with the key as user                 and value as a Set of friends of that user.
   3. Read Streaming Data into StreamData Array List. This data is processed line by line.
   4. For each Test Event:  
      - For a befriend/unfriend event – update the social Network of friends.
      - For a purchase Event:
       1. Retrieve the friends of the user up to degree D.
       2. Gather all transaction Data for the friendsList and put them in a priority Queue based on latest timestamp (first) and order of event occurrence (second).
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
   
   ### 4.Extras ###
   
   **Alternative Algorithm For adding a new friend to the social network**  
   Instead of rebuilding the whole social network, we can just change the necessary details in the social network.
   
   Let's assume A and B become friends and there is a social network that exists already.
   
   1.	Check depth of B in A's network. Since it will be greater than 1, change it to 1. Also keep track of previous neighbor via which A can reach B, which in this case would be B itself. Previous neighbor helps when we need to delete.  
   2.	In A's social network, check all the nodes and see at what depth they are connected to B with. If depth is greater than 2, change their depths to 2, change their previous neighbor to A and add them to a queue of nodes that need to be updated. Once all in A's network are updated, we pop top element in queue, set the depth to 2, and see in their network where B is. If B is now at a depth of more than 3, we do the same, with previous neighbor being the node itself instead of A and depth being 3.
   3.	Do this recursively until the queue is empty, or we reach our max depth of T.
   4. For B we need to do the same steps to integrate A to its list.

   **Alternative Algorithm For deleting a friend from the social network**  
   Suppose we want to unfriend A and B.

   1. Remove B from A's list.
   2. Add A to a queue. Go through A's list and find get all nodes that are connected to B with previous node as A. Add them to the queue. 
   3. For all nodes in the queue, do the same and add affected nodes in the queue. i.e. If we are processing node X, we see in X's list which nodes have connection to B with prevnode as X.
   4. Once we have all nodes we need, we start from A and see if we can get to B from any of the nodes in A by adding a depth of 1 to it. We process down the queue and keep doing this for all nodes. 
   5. Repeat step for until no nodes update anymore
(Replicating Bellman Ford algorithm)
   6. We need to do the same to remove A from B's network.
   7. Once that is done, we have successfully integrated A-B deletion from the network.
 

