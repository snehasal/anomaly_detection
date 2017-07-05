/*
 * This is an Anomaly Detection System used for flagging anomalous events.
 *
 Steps:
 
 		1. Read the batch_log file into readInput ArrayList
		 
		 2 a. Populate Social Network 
		 2 b. Create Purchase Objects and update Purchase Event Dictionary
		 
		 3. Read Test Data into StreamData ArrayList
		 
		 4. Read each test event:
		 	a. If friend / un-friend, update social network
		 	b if purchase:
		 	   - Create friend list of test user with degree D
		 	   - Pick up events from social network and put it in the priority Queue
		 	   - Pop top T events from heap based on timeStamp and store in TopT ArrayList
		 	   - Calculate Mean and SD 
		 	   - compare test amount with mean + 2sd
		 	
		 5. Write anomaly to output file
		 6. Add the event to Purchase Event Dictionary

*/

//Importing the files
import java.io.IOException;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

public class AnomalyDetector
{
	//Declaring global variables
	public static Map<String, ArrayList<PurchaseEvent>> eventDictionary = new HashMap<String, ArrayList<PurchaseEvent>>();
	public static HashMap<String, Set<String> >socialNetwork = new HashMap<String,Set<String> >();
	static int insertionid=1; //Used for maintaining the order of log data
	
	public static void main(String[] args) throws JSONException, IOException, ParseException 
	{
		// Reading the batch_log.json file
		Helpers h1 = new Helpers();
		List<String> readInput = new ArrayList<String>();
		int D = 0, T = 0;
		
		//Initializing the file names with command line parameters.
		String log_input_file = args[0];
		String stream_log_input_file = args[1];
		String output_file = args[2];
		
		h1.writeLog("Reading Input Data from  " +  log_input_file );
		h1.writeLog("Reading Streaming Data from  " + stream_log_input_file);
		readInput = h1.readFile(log_input_file);
		h1.writeLog("Finsihed reading input");
		
		// Extracting D and T from input file 
		JSONObject obj = new JSONObject(readInput.get(0));
		D = obj.getInt("D");
		T = obj.getInt("T");
		h1.writeLog("Value of D is " + D );
		h1.writeLog("Value of T is " + T );
		readInput.remove(0);
		
		createEventDictionary(readInput);
		h1.writeLog("Finsihed Creating event dict");
		h1.writeLog("Finsihed initializaing Social Network of friends");
		
		// Reading the stream_log.json file
		List<String> readStreamData = new ArrayList<String>();
		readStreamData = h1.readFile(stream_log_input_file);
		h1.writeLog("Reading Stream data");
		
		// Process Stream inputs
		processStreamInputs(readStreamData,D,T,h1,output_file);
	}

	// This method is used for creating a dictionary of the user's transaction
	public static void createEventDictionary(List<String> readInput) throws JSONException 
	{
		String eventtype = "";
		JSONObject obj; 
		for(int i= 0 ; i < readInput.size();i++)
		{
			obj = new JSONObject(readInput.get(i));
			eventtype = obj.getString("event_type");
	
			//Creating a Dictionary of Purchase Events
			if (eventtype.equals("purchase"))
			{
				processPurchaseEvents(obj);
			}
			
			//Creating a Social Network
			if(eventtype.equals("befriend"))
			{
				beFriend(obj);
			}
			if(eventtype.equals("unfriend"))
			{
				unFriend(obj);
			}
		}
	}
	
	// This method is used for processing the purchase Events
	private static void processPurchaseEvents(JSONObject obj) throws JSONException
	{
		String id =  obj.getString("id");
		//Add new entry in eventDictionary if user is not Present
		if(!eventDictionary.containsKey(id))
		{
			PurchaseEvent pe = new PurchaseEvent(obj.getDouble("amount"), insertionid, obj.getString("timestamp"));
			ArrayList<PurchaseEvent> eventlist = new ArrayList<PurchaseEvent>();
			eventlist.add(pe);
			eventDictionary.put(id, eventlist);
		}
		//Update eventDictionary if user is present
		else if(eventDictionary.containsKey(id))
		{
			PurchaseEvent pe = new PurchaseEvent(obj.getDouble("amount"), insertionid, obj.getString("timestamp"));
			ArrayList<PurchaseEvent> al = eventDictionary.get(id);
			al.add(pe);
			eventDictionary.put(id,al);
		}
		insertionid++; 
	}

	
	/*
	 *  This method is used for processing of stream events
	 *  It adds  the transactions of each to their specific sets
	 *  It uses the top T transactions for calculating the mean and sd
	 *  When an anomalous event is found, the event is flagged into the output file
	 *  The events are again used for updating the event dictionary and social netwrok
	*/
	private static void processStreamInputs(List<String> readStreamData, int D, int T, Helpers h1, String output_file) throws JSONException, IOException 
	{
		String eventtype = "";
		JSONObject obj; 
		for(int i= 0 ; i < readStreamData.size();i++)
		{
			if(readStreamData.get(i).length()>0)
			{	
				obj = new JSONObject(readStreamData.get(i));
				eventtype = obj.getString("event_type");
			
				//Updating the Social Network
				if(eventtype.equals("befriend"))
				{
					beFriend(obj);
				}
				if(eventtype.equals("unfriend"))
				{
					unFriend(obj);
				}
				
				//Processing of Purchase Events
				if (eventtype.equals("purchase"))
				{
					String id =  obj.getString("id");
					double amount = obj.getDouble("amount");
					double mean = 0d;
					double sd = 0d;
				
					if(D>0)
					{
						// Create friend list of test user with degree D from social network
						Set<String> friendsSetofDegreeD = createFriendsSetofDegreeD(id,D);
	
						// Pick up transactions of user's social network and put it in the priority Queue				
						PriorityQueue<PurchaseEvent> pq = createPriorityQueueofTransactions(friendsSetofDegreeD);
						
						// Pick top T events from priority Queue based on timeStamp and store it an Array\
						int newT = (pq.size()>T?T:pq.size());
						if(newT>1)
						{
							double topTAmount[] = new double[newT];
							double sum = 0;
							double amt ;
							PurchaseEvent pe  = null;
							if (!pq.isEmpty())
							{
								//Polling top T events for calculating mean and SD to detect Anomaly 
								for (int j = 0 ; j < newT ; j++)
								{
									pe = pq.poll();
									amt = pe.amount;
									sum += amt;
									topTAmount[j] = amt;
								}
							}
							
							// Calculate Mean
							mean = (sum/newT);
					
							//Calculate SD
							double tempsd = 0d;
							for (int j = 0 ; j < topTAmount.length ; j++)
							{
								
								tempsd += Math.pow((topTAmount[j]-mean),2.00);
							}
							sd = Math.sqrt(tempsd/newT);
							
							// compare test amount with mean + 2sd
							if(amount > (mean + (3*sd)))
							{
								String sb = formatOutputJsonObject(mean,sd,obj);
								//Writing Anomolous Events to File
								h1.writeLog("Writing Anomolous Event to "+ output_file);
								h1.writeFile(output_file , sb);
							}  
						}
					}
				processPurchaseEvents(obj);
				}
			}
		}		
	}

	//This method is used for formatting the anomalous event before writing it to a file.
	private static String formatOutputJsonObject(double mean, double sd, JSONObject obj) throws JSONException 
	{
		//Truncating the double value upto two decimal point
		String df_sd ;
		DecimalFormat df = new DecimalFormat("#.00");
		df.setRoundingMode(RoundingMode.DOWN); 
		String df_mean = df.format(mean);
		if (sd==0.0)
		{
			df_sd = "0.00";
		}
		else
		{
			df_sd = df.format(sd);
		}					
		//Creating a LinkedHashMap of the output 
		Map<String, String> jsonOrderedMap = new LinkedHashMap<String, String>();
		jsonOrderedMap.put("event_type","purchase");
		jsonOrderedMap.put("timestamp",obj.getString("timestamp")+"");
		jsonOrderedMap.put("id",obj.getString("id")+"");
		jsonOrderedMap.put("amount",obj.getDouble("amount")+"");
		jsonOrderedMap.put("mean", df_mean);
		jsonOrderedMap.put("sd",df_sd);
		
		//Converted LinkedHashMap to Json Object using Gson jar 
		Gson gson = new Gson();
		String json = gson.toJson(jsonOrderedMap, LinkedHashMap.class);
		String[] json_arr = json.split(",");
		String sb = "";
		sb = json_arr[0] + ", "  + json_arr[1] + ", "  
				+ json_arr[2].split(":")[0] + ": "+ json_arr[2].split(":")[1] + ", " 
				+ json_arr[3].split(":")[0] + ": "+ json_arr[3].split(":")[1] + ", " 
				+ json_arr[4].split(":")[0] + ": "+ json_arr[4].split(":")[1] + ", " 
				+ json_arr[5].split(":")[0] + ": "+ json_arr[5].split(":")[1];
		return sb;
	}

	// This method is used for creating a Priority queue for user's Social Network's Transactions
	private static PriorityQueue<PurchaseEvent> createPriorityQueueofTransactions(Set<String> friendsSetofDegreeD)
	{	
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		/*
		 *  Writing Comparator for Priority Queue:
		 *  Level 1: Descending Order of Timestamp
		 *  Level 2: Ascending order of insertion
		*/     
		PriorityQueue<PurchaseEvent> pq = new PriorityQueue<PurchaseEvent> (new Comparator<PurchaseEvent>() 
		{
			@Override
			public int compare(PurchaseEvent o1, PurchaseEvent o2) 
			{	
			    Timestamp timestamp1 = null ;
			    Timestamp timestamp2 = null;
				try 
				{
					timestamp1 = new java.sql.Timestamp(dateFormat.parse(o1.timestamp).getTime());
				    timestamp2 = new java.sql.Timestamp(dateFormat.parse(o2.timestamp).getTime());
				} 
				catch (ParseException e) 
				{		
					e.printStackTrace();
				}
			    int returnValue = timestamp2.compareTo(timestamp1);
				if(returnValue!= 0)
					return returnValue;
				else
				{
					if(o1.insertionId>o2.insertionId)
						return 1;
					else 
						return -1;
				}
			}
		}) ;
		
		//Adding Purchase Objects to Priority Queue
		ArrayList<PurchaseEvent> eventListStream = null;
		for (String user : friendsSetofDegreeD)
		{
			if(eventDictionary.containsKey(user))
			{
				eventListStream = eventDictionary.get(user);
				for(PurchaseEvent pe : eventListStream)
				{
					pq.add(pe);
				}
			}
		}
		return pq;
	}

	// This method is used for creating a set of friends of the user.
	private static Set<String> createFriendsSetofDegreeD(String id, int D)
	{
		int degree = 0;
		Set<String> backupFriendsSet = new HashSet<String>();
		Set<String> currentDegreeSet = new HashSet<String>();
		Set<String> finalFriendsSetofDegreeD = new HashSet<String>();
		if (socialNetwork.containsKey(id))
		{
			backupFriendsSet.addAll(socialNetwork.get(id));
			degree+=1;
			//add all degree 1 friends
			finalFriendsSetofDegreeD.addAll(backupFriendsSet);
			//process for next degree friends
			while(degree <= D)
			{
				currentDegreeSet = new HashSet<String>();
				//for each degree n friend
				for (String s: backupFriendsSet)
				{
					if (socialNetwork.containsKey(s))
					{
						for (String sq: socialNetwork.get(s))
						{
							if (!id.equals(sq))
							{
								if (currentDegreeSet.add(sq)) //avoid duplicates
									currentDegreeSet.add(sq);
								if (finalFriendsSetofDegreeD.add(sq)) //avoid duplicates
									finalFriendsSetofDegreeD.add(sq);
							}
						}
					}
				} //end for degree n friends
				degree++;
				backupFriendsSet = currentDegreeSet;
			}// end for all friends
		}
		return finalFriendsSetofDegreeD;
	}

	// This method is used for processing the event_type = befriend
	private static void beFriend(JSONObject obj) throws JSONException 
	{
		String id1 =  obj.getString("id1");
		String id2 =  obj.getString("id2");
		if(!socialNetwork.containsKey(id1))
		{
			addNewFriend_SocialNetwork(id1,id2);
		}
		if(!socialNetwork.containsKey(id2))
		{
			addNewFriend_SocialNetwork(id2,id1);
		}
		if(socialNetwork.containsKey(id1))
		{
			updateExisitngFriend_SocialNetwork(id1,id2);
		}
		if(socialNetwork.containsKey(id2))
		{
			updateExisitngFriend_SocialNetwork(id2,id1);	
		}	
	}

	// This method is used for updating the social network for the event_type = unFriend for an existing friend
	private static void updateExisitngFriend_SocialNetwork(String id1, String id2)
	{
		Set<String> friends = socialNetwork.get(id1);
		if(friends.add(id2))
			friends.add(id2);
		socialNetwork.put(id1,friends);	
	}

	// This method is used for updating the social network for the event_type = unFriend for a new friend
	private static void addNewFriend_SocialNetwork(String id1, String id2) 
	{
		Set<String> friends = new HashSet<String>();
		friends.add(id2);
		socialNetwork.put(id1,friends);
	}

	
	// This method is used for processing the event_type = unFriend
	private static void unFriend(JSONObject obj) throws JSONException
	{
		String id1 =  obj.getString("id1");
		String id2 =  obj.getString("id2");
		if(socialNetwork.containsKey(id1))
		{
			unFriend_updateSocialNetwork(id1,id2);
		}
		if(socialNetwork.containsKey(id2))
		{
			unFriend_updateSocialNetwork(id2,id1);
		}
	}
	
	
	// This method is used for updating the social network for the event_type = unFriend
	private static void unFriend_updateSocialNetwork(String id1, String id2)
	{
		Set<String> friends = socialNetwork.get(id1);
		friends.remove(id2);
		if (friends.isEmpty())
			socialNetwork.remove(id1);
		else
			socialNetwork.put(id1,friends);
	}
}
	
