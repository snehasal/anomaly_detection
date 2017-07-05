//This class stores the information about Purchase events ofeach user
public class PurchaseEvent 
{
	double amount;
	int insertionId ;
	String timestamp;

	PurchaseEvent(double amount , int insertionId, String timestamp)
	{
		this.amount = amount;
		this.insertionId = insertionId;
		this.timestamp = timestamp;
	}
}
