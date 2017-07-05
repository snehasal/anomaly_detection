import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Helpers {

	// Reading the File
	public List<String> readFile (String filePath) throws FileNotFoundException
	{
		Scanner sc = null;
		List<String> readInput = new ArrayList<String>();
		sc = new Scanner(new File (filePath));
		while (sc.hasNextLine())
		{
			readInput.add(sc.nextLine());
		}
		if(sc!=null)
			sc.close();
		return readInput;
	}

	// Writing the File
	public void writeFile (String filePath ,String json) throws IOException
	{
		BufferedWriter bw = null;
		FileWriter fw = null;
		File file = new File(filePath);

		if (!file.exists()) 
		{
			file.createNewFile();
		}
		
		fw = new FileWriter(file.getAbsoluteFile(), true);
		bw = new BufferedWriter(fw);
		bw.write(json+"");
		bw.newLine();
		System.out.println(json);
		if (bw != null)
			bw.close();
		if (fw != null)
			fw.close();
	}
	
	//Writing Logs
	public void writeLog (String info) 
	{
		System.out.println(info);
	}
}
