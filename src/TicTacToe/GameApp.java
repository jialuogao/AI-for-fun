package TicTacToe;

import java.io.*;
import java.util.ArrayList;

public class GameApp {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int[][] board = new int[3][3];

		String dbFile = "db.txt";
		ArrayList<String> data = new ArrayList<String>();

		boolean hasData = readData(data, dbFile);
		if (!hasData) {
			data = initData();
			writeData(data, dbFile);
		} else {
			// calculations here
			writeData(data, dbFile);
		}
		
		
		String testcode = ""+212012010;
		System.out.println(testcode);
		int index=Integer.valueOf(testcode, 3);//convert from b3tob10
		System.out.println("Integer: "+index);
		System.out.println(data.get(index));
	}

	public static boolean readData(ArrayList<String> data, String fileName) {
		boolean hasData = false;
		try {
			FileReader fileReader = new FileReader(fileName);

			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line = "";
			while ((line = bufferedReader.readLine()) != null) {
				data.add(line);
			}

			bufferedReader.close();
			hasData = true;
		} catch (FileNotFoundException ex) {
			data = initData();
			writeData(data, fileName);
		} catch (IOException ex) {
			System.out.println("Error reading file '" + fileName + "'");
		}
		return hasData;
	}

	public static boolean writeData(ArrayList<String> data, String fileName) {
		boolean writeSucceed = false;
		try {
			FileWriter fileWriter = new FileWriter(fileName);

			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

			for (String line : data) {
				bufferedWriter.write(line);
				bufferedWriter.newLine();
			}
			writeSucceed = true;
			bufferedWriter.close();
		} catch (IOException ex) {
			System.out.println("Error writing to file '" + fileName + "'");
		}
		return writeSucceed;
	}

	public static ArrayList<String> initData() {
		ArrayList<String> initData = new ArrayList<String>();
		String line = "";
		for(int firstDig=0;firstDig<3;firstDig++) {
			initDataRec(line, initData,firstDig);			
		}
		return initData;
	}
	private static void initDataRec(String line,ArrayList<String> initData,int add) {
		line+=add;
		if(line.length()==9) {
			line+=" O 1 1 1 " 
				   + "1 1 1 " 
				   + "1 1 1";
			line+=" X 1 1 1 "
				   + "1 1 1 "
				   + "1 1 1";
			initData.add(line);
		}
		else if(line.length()<9){
			for(int data = 0; data<3 ;data++) {
				initDataRec(line,initData,data);
			}			
		}
	}


}
