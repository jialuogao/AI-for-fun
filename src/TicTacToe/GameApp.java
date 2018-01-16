package TicTacToe;

import java.io.*;
import java.util.*;

public class GameApp {

	private static Scanner input = new Scanner(System.in);
	private static Random randGenerator = new Random();

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		int[][] board = new int[3][3];
		
		// read rule db
		double dbVersion = 0.0;
		String dbStartingFile = "db-" + ("" + dbVersion).substring(0, 3) + ".txt";
		ArrayList<String> data = new ArrayList<String>();
		boolean hasData = readData(data, dbStartingFile);
		if (!hasData) {
			// initiate data and write to data file
			data = initData();
			writeData(data, dbStartingFile);
		}

		// UI
		System.out.println("Input \"training\" to start training");
		System.out.println("Input \"gaming\" to start play");
		String inputCommand = input.nextLine();
		int switchValue = 0;
		if (inputCommand.equalsIgnoreCase("training")) {
			switchValue = 1;
		} else if (inputCommand.equalsIgnoreCase("gaming")) {
			switchValue = 2;
		}

		switch (switchValue) {
		case 1:
			dbVersion= training(dbVersion,data);
			break;
		case 2:
			// game
			String P1_40 = "db-2.0.txt";
			String P2_20 = "db-1.0.txt";
			ArrayList<String> data1 = new ArrayList<String>();
			readData(data1, dbStartingFile);
			ArrayList<String> data2 = new ArrayList<String>();
			readData(data2, dbStartingFile);
			
			int[][] b= {{1,0,2},{0,1,2},{2,0,1}};
			int[] c = {2,2};
			int d =1;
			int [] e = {-999,-999};
			boolean a=isWin(b,c,d,e);
			System.out.println(a);
			game(data1,data2);
			break;
		default:
			break;
		}
	}

	// read in data from file
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
			// initiate data and create data file
			data = initData();
			writeData(data, fileName);
		} catch (IOException ex) {
			System.out.println("Error reading file '" + fileName + "'");
		}
		return hasData;
	}

	// write data to file
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

	// initiating data
	public static ArrayList<String> initData() {
		ArrayList<String> initData = new ArrayList<String>();
		String line = "";
		for (int firstDig = 0; firstDig < 3; firstDig++) {
			initDataRec(line, initData, firstDig);
		}
		return initData;
	}
	private static void initDataRec(String line, ArrayList<String> initData, int add) {
		line += add;
		if (line.length() == 9) {
			//check if the situation is possible
			int count1=0;
			int count2=0;
			for(int i=0;i<line.length();i++) {
				if(Integer.parseInt(Character.toString(line.charAt(i)))==1) {
					count1++;
				}
				else if(Integer.parseInt(Character.toString(line.charAt(i)))==2) {
					count2++;
				}
			}
			if (Math.abs(count1-count2)<2) {
				line += " O 10000 10000 10000 " + "10000 10000 10000 " + "10000 10000 10000";
				line += " X 10000 10000 10000 " + "10000 10000 10000 " + "10000 10000 10000";					
			}
			initData.add(line);
		} else if (line.length() < 9) {
			for (int data = 0; data < 3; data++) {
				initDataRec(line, initData, data);
			}
		}
	}
	
	
	
	//train the AI
		public static void game(ArrayList<String> data1,ArrayList<String> data2) {
			
			int OwinGames=0;
			int XwinGames=0;
			int tieGames=0;
			int totalGames=0;
			//for (int n=0;n<40;n++) {
				// play 100000 games per iteration
				//for (int i = 0; i < 1000000; i++) {
					totalGames++;
					
					ArrayList<String> logO = new ArrayList<String>();
					ArrayList<String> logX = new ArrayList<String>();
					//System.out.println(n+"  "+(i+1));/////////////////////////////////////////////////////////////////////////////
					
					// play the game and change "data"
					boolean gameover = false;
					int[][] board = new int[3][3];
					// pick a random player to start
					// O-true X-false
					boolean start = randGenerator.nextBoolean();
					boolean player = start;
					while (!gameover) {
						// read in the board
						String gameStatus = readGameStatus(board);
						//search for strategy
						String[] strategy=new String[0];
						if(player) {
							strategy = searchStrategy(gameStatus, data1, player);
						}
						else
							strategy = searchStrategy(gameStatus, data2, player);
						// play
						// Symbol that a player use
						// 1 for O
						// 2 for X
						int playerSymbol = 0;
						if(player) {
							playerSymbol = 1;
						}
						else {
							playerSymbol = 2;
						}
						
						// find the right step to play
						int step = calculateStep(strategy,board);
						//no place resulted tieGames
						if(step==-999) {
							gameover=true;
							tieGames++;
						}else {
							if(player) {
								logO.add(gameStatus+" "+step);
							}
							else {
								logX.add(gameStatus+" "+step);
							}
						}
						//convert step into x,y coordinate location
						int[] location=convertLocation(step);
						//play
						board = playStep(board, location, playerSymbol);
						//check win condition
						int [] lastlocation = {-999,-999};
						
						System.out.println(gameStatus);/////////////////////////////////////////////////////////////////
						
						boolean isWin = isWin(board,location,playerSymbol,lastlocation);
						// change strategy based on W/L result
						if(isWin)
							gameover=true;
						player = !player;
					}
				//}
			//}
			System.out.println(OwinGames);
			System.out.println(XwinGames);
			System.out.println(tieGames);
			System.out.println(totalGames);
		}
		// convert game board into a String
	
	
	
	//train the AI
	public static double training(double dbVersion,ArrayList<String> data) {
		
		int OwinGames=0;
		int XwinGames=0;
		int tieGames=0;
		int totalGames=0;
		for (int n=0;n<40;n++) {
			// play 100000 games per iteration
			for (int i = 0; i < 1000000; i++) {
				totalGames++;
				
				ArrayList<String> logO = new ArrayList<String>();
				ArrayList<String> logX = new ArrayList<String>();
				//System.out.println(n+"  "+(i+1));/////////////////////////////////////////////////////////////////////////////
				
				// play the game and change "data"
				boolean gameover = false;
				int[][] board = new int[3][3];
				// pick a random player to start
				// O-true X-false
				boolean start = randGenerator.nextBoolean();
				boolean player = start;
				while (!gameover) {
					// read in the board
					String gameStatus = readGameStatus(board);
					//search for strategy
					String[] strategy=searchStrategy(gameStatus, data, player);
					// play
					// Symbol that a player use
					// 1 for O
					// 2 for X
					int playerSymbol = 0;
					if(player) {
						playerSymbol = 1;
					}
					else {
						playerSymbol = 2;
					}
					
					//System.out.println(gameStatus);/////////////////////////////////////////////////////////////////
					
					// find the right step to play
					int step = calculateStep(strategy,board);
					//no place resulted tieGames
					if(step==-999) {
						gameover=true;
						tieGames++;
						//O first
						if(start) {
							//-O
							for(String o:logO) {
								String[] content=o.split(" ");
								int dbindex = Integer.valueOf(content[0], 3);
								int dbstep = Integer.parseInt(content[1]);
								String dbline = data.get(dbindex);
								String[] dblineSplit = dbline.split(" ");
								dblineSplit[dbstep+2] = ""+(Integer.parseInt(dblineSplit[dbstep+2])-1);
								String newdbline = "";
								for(String part:dblineSplit) {
									newdbline=newdbline+part+" ";
								}
								data.set(dbindex, newdbline);
							}
							//+X
							for(String x:logX) {
								String[] content=x.split(" ");
								int dbindex = Integer.valueOf(content[0], 3);
								int dbstep = Integer.parseInt(content[1]);
								String dbline = data.get(dbindex);
								String[] dblineSplit = dbline.split(" ");
								dblineSplit[dbstep+12] = ""+(Integer.parseInt(dblineSplit[dbstep+12])+1);
								String newdbline = "";
								for(String part:dblineSplit) {
									newdbline=newdbline+part+" ";
								}
								data.set(dbindex, newdbline);
							}
						}
						//X first
						else {
							//+O
							for(String o:logO) {
								String[] content=o.split(" ");
								int dbindex = Integer.valueOf(content[0], 3);
								int dbstep = Integer.parseInt(content[1]);
								String dbline = data.get(dbindex);
								String[] dblineSplit = dbline.split(" ");
								dblineSplit[dbstep+2] = ""+(Integer.parseInt(dblineSplit[dbstep+2])+1);
								String newdbline = "";
								for(String part:dblineSplit) {
									newdbline=newdbline+part+" ";
								}
								data.set(dbindex, newdbline);
							}
							//-X
							for(String x:logX) {
								String[] content=x.split(" ");
								int dbindex = Integer.valueOf(content[0], 3);
								int dbstep = Integer.parseInt(content[1]);
								String dbline = data.get(dbindex);
								String[] dblineSplit = dbline.split(" ");
								dblineSplit[dbstep+12] = ""+(Integer.parseInt(dblineSplit[dbstep+12])-1);
								String newdbline = "";
								for(String part:dblineSplit) {
									newdbline=newdbline+part+" ";
								}
								data.set(dbindex, newdbline);
							}
						}
					}else {
						if(player) {
							logO.add(gameStatus+" "+step);
						}
						else {
							logX.add(gameStatus+" "+step);
						}
					}
					//convert step into x,y coordinate location
					int[] location=convertLocation(step);
					//play
					board = playStep(board, location, playerSymbol);
					//check win condition
					int [] lastlocation = {-999,-999};
					boolean isWin = isWin(board,location,playerSymbol,lastlocation);
					// change strategy based on W/L result
					if(isWin) {
						//O win
						if(playerSymbol==1) {
							OwinGames++;
							//+O
							for(String o:logO) {
								String[] content=o.split(" ");
								int dbindex = Integer.valueOf(content[0], 3);
								int dbstep = Integer.parseInt(content[1]);
								String dbline = data.get(dbindex);
								String[] dblineSplit = dbline.split(" ");
								dblineSplit[dbstep+2] = ""+(Integer.parseInt(dblineSplit[dbstep+2])+3);
								String newdbline = "";
								for(String part:dblineSplit) {
									newdbline=newdbline+part+" ";
								}
								data.set(dbindex, newdbline);
							}
							//-X
							for(String x:logX) {
								String[] content=x.split(" ");
								int dbindex = Integer.valueOf(content[0], 3);
								int dbstep = Integer.parseInt(content[1]);
								String dbline = data.get(dbindex);
								String[] dblineSplit = dbline.split(" ");
								dblineSplit[dbstep+12] = ""+(Integer.parseInt(dblineSplit[dbstep+12])-3);
								String newdbline = "";
								for(String part:dblineSplit) {
									newdbline=newdbline+part+" ";
								}
								data.set(dbindex, newdbline);
							}
						}
						//X win
						else if(playerSymbol==2) {
							XwinGames++;
							//-O
							for(String o:logO) {
								String[] content=o.split(" ");
								int dbindex = Integer.valueOf(content[0], 3);
								int dbstep = Integer.parseInt(content[1]);
								String dbline = data.get(dbindex);
								String[] dblineSplit = dbline.split(" ");
								dblineSplit[dbstep+2] = ""+(Integer.parseInt(dblineSplit[dbstep+2])-3);
								String newdbline = "";
								for(String part:dblineSplit) {
									newdbline=newdbline+part+" ";
								}
								data.set(dbindex, newdbline);
							}
							//+X
							for(String x:logX) {
								String[] content=x.split(" ");
								int dbindex = Integer.valueOf(content[0], 3);
								int dbstep = Integer.parseInt(content[1]);
								String dbline = data.get(dbindex);
								String[] dblineSplit = dbline.split(" ");
								dblineSplit[dbstep+12] = ""+(Integer.parseInt(dblineSplit[dbstep+12])+3);
								String newdbline = "";
								for(String part:dblineSplit) {
									newdbline=newdbline+part+" ";
								}
								data.set(dbindex, newdbline);
							}
						}
						gameover=true;
					}
					player = !player;
				}
			}
			// create new version for data file
			dbVersion += 0.1;
			writeData(data, "db-" + ("" + dbVersion).substring(0, 3) + ".txt");
		}
		System.out.println(OwinGames);
		System.out.println(XwinGames);
		System.out.println(tieGames);
		System.out.println(totalGames);
		
		return dbVersion;
	}
	// convert game board into a String
	private static String readGameStatus(int[][] board) {
		// read the game status
		String gameStatus = "";
		for (int j = 0; j < 3; j++) {
			for (int k = 0; k < 3; k++) {
				gameStatus += board[j][k];
			}
		}
		return gameStatus;
	}
	//find strategy in db based on game status
	private static String[] searchStrategy(String gameStatus,ArrayList<String> data,boolean player) {
		// convert game status from b3 to b10 to find the index of line in ArrayList
		int index = Integer.valueOf(gameStatus, 3);
		String strategyLine = data.get(index);
		int beginIndex = strategyLine.indexOf('O') + 2;
		int endIndex = strategyLine.indexOf('X') - 1;
		String[] strategy = null;
		if (player) {
			// strategy for player O
			strategy = strategyLine.substring(beginIndex, endIndex).split(" ");
		} else {
			// strategy for player X
			strategy = strategyLine.substring(endIndex + 3).split(" ");
		}
		return strategy;
	}
	// choose step randomly based on strategy
	private static int calculateStep(String[] strategy,int[][] board) {
		int result = -999;
		int bound = 0;
		int[] stratInt = new int[strategy.length];
		for (int i = 0; i < strategy.length; i++) {
			stratInt[i] = Integer.parseInt(strategy[i]);
			int[] location = convertLocation(i);
			if(board[location[1]][location[0]]==0)
				bound += stratInt[i];
		}
		// choose step randomly based on strategy
		int step=-999;
		if(bound>0) {
			step = randGenerator.nextInt(bound);			
			for (int i = 0; i < stratInt.length; i++) {
				int[] location = convertLocation(i);
				if(board[location[1]][location[0]]==0) {
					if (step <= stratInt[i]) {
						result = i;
						i = stratInt.length;
					} else {
						step -= stratInt[i];
					}
				}
			}
		}
		return result;
	}
	//convert step into x,y coordinates
	private static int[] convertLocation(int step) {
		int[] location=new int[2];
		switch (step) {
		case 0:
			location[0] = 0;
			location[1] = 0;
			break;
		case 1:
			location[0] = 0;
			location[1] = 1;
			break;
		case 2:
			location[0] = 0;
			location[1] = 2;
			break;
		case 3:
			location[0] = 1;
			location[1] = 0;
			break;
		case 4:
			location[0] = 1;
			location[1] = 1;
			break;
		case 5:
			location[0] = 1;
			location[1] = 2;
			break;
		case 6:
			location[0] = 2;
			location[1] = 0;
			break;
		case 7:
			location[0] = 2;
			location[1] = 1;
			break;
		case 8:
			location[0] = 2;
			location[1] = 2;
			break;
		}
		return location;
	}
	//if already exist a play on a place, ignore the place
	
	//play
	private static int[][] playStep(int[][] board, int[] location, int playerSymbol) {	
		board[location[1]][location[0]] = playerSymbol;
		return board;
	}
	
	//check if the game is over
	private static boolean isWin(int[][] board,int[] location,int playerSymbol,int[] lastlocation) {
		for(int x=-1;x<2;x++) {
			for(int y=-1;y<2;y++) {
				int locx = location[0];
				int locy = location[1];
				//x in range
				if(locx+x<3 && locx+x>-1) {
					//y in range
					if(locy+y<3 && locy+y>-1) {
						// not it-self
						if(locy+y!=locy || locx+x!=locx) {
							//not last location
							if(location[0]+x!=lastlocation[0] || location[1]+y!=lastlocation[1]) {
								//if there is a friendly symbol next to location
								if(board[locy + y][locx + x]==playerSymbol) {
									//has a opposite side
									if(locx-x<3 && locx-x>-1 && locy-y<3 && locy-y>-1) {	
										//if there is a friendly symbol on the opposite side of the location
										if(board[locy -y][locx-x]==playerSymbol) {
											return true;
										}
										else {
											int[] nextloc = {locx+x,locy+y};
											return isWin(board,nextloc,playerSymbol,location);
										}
									}
									else {
										int[] nextloc = {locx+x,locy+y};
										return isWin(board,nextloc,playerSymbol,location);
									}
								}															
							}
						}						
					}
				}
			}
		}
		return false;
	}
}
