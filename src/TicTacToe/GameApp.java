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
		double dbVersion = 4.0;
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
		System.out.println("Input \"training with\" to start training with set version");
		System.out.println("Input \"gaming\" to start play");
		System.out.println("Input \"playing\" to start play");
		String inputCommand = input.nextLine();
		if (inputCommand.equalsIgnoreCase("training")) {
			dbVersion = training(dbVersion, data);
		} 
		else if(inputCommand.equalsIgnoreCase("training with")) {
			String trainingFile = "db-0.0.txt";
			ArrayList<String> data2 = new ArrayList<String>();
			boolean hasData2 = readData(data2,trainingFile);
			if(!hasData2) {
				System.out.println("Date does not exist");
			}
			else {
				dbVersion = training(dbVersion, data, data2);				
			}
		}
		else if (inputCommand.equalsIgnoreCase("gaming")) {
			// game
			String P1 = "db-4.9.txt";
			String P2 = "db-0.0.txt";
			ArrayList<String> data1 = new ArrayList<String>();
			readData(data1, P1);
			ArrayList<String> data2 = new ArrayList<String>();
			readData(data2, P2);


			game(data1, data2);
		}else if(inputCommand.equalsIgnoreCase("playing")) {
			
			String AIversion = "db-5.0.txt";
			ArrayList<String> AIdata = new ArrayList<String>();
			readData(AIdata,AIversion);
			
			System.out.println("Do you want to start with O or X?");
			String symbolInput = input.nextLine();
			int playerSymbol = 0;
			ArrayList<String> logO = new ArrayList<String>();
			ArrayList<String> logX = new ArrayList<String>();
			if(symbolInput.equalsIgnoreCase("O")) {
				playerSymbol = 1;
			}
			else if(symbolInput.equalsIgnoreCase("X")) {
				playerSymbol = 2;
			}
			System.out.println("Do you want to start first?");
			String startInput = input.nextLine();
			boolean isWin = false;
			boolean isLost = false;
			boolean isTie = false;
			boolean isFair = true;
			if(startInput.equalsIgnoreCase("yes")) {
				System.out.println("please input your play in \"x,y\" format");
				String[] locationString = input.nextLine().split(",");
				int[] location = new int[2];
				location[0] = Integer.parseInt(locationString[0]);
				location[1] = Integer.parseInt(locationString[1]);
				
				String gameStatus=readGameStatus(board);
				if (playerSymbol==2) {
					gameStatus = reverseStatus(gameStatus);
				}
				int step = convertStep(location);
				if(playerSymbol == 1)
					logO.add(gameStatus + " " + step);
				else if(playerSymbol==2)
					logX.add(gameStatus+" "+step);
				
				board = playStep(board,location,playerSymbol);
				//System.out.println(readGameStatus(board));
				printBoard(board);
				isWin = isWin(board,location,playerSymbol,new ArrayList<int[]>());
			}
			while(!isWin&&!isLost&&!isTie&&isFair) {
				boolean isEnd = isEnd(board);
				
				if(!isEnd) {
					int AISymbol = 0;
					if(playerSymbol==1) {
						AISymbol = 2;
					}
					else if(playerSymbol==2){
						AISymbol = 1;
					}
					// read in the board
					String gameStatus = readGameStatus(board);	
					// search for strategy
					String[] strategy = new String[0];
					if (AISymbol==2) {
						gameStatus = reverseStatus(gameStatus);
					}
					strategy = searchStrategy(gameStatus, AIdata);
					// find the right step to play
					int step = calculateStep(strategy, board);
					if(step == -1) {
						isLost = true;
						if(AISymbol ==1) {
							if(logO.size()>0 && logX.size()>0)
								trainingWin(logO,logX,data,2);
						}
						else {
							if(logO.size()>0 && logX.size()>0)
								trainingWin(logO,logX,data,1);
						}
					}
					else {
						// convert step into x,y coordinate location
						int[] AIlocation = convertLocation(step);
						
						if(AISymbol ==1) {
							logO.add(gameStatus+" "+step);
						}else if(AISymbol ==2) {
							logX.add(gameStatus+" "+step);
						}
						
						// play
						if(AIlocation[0]<0||AIlocation[0]>2||AIlocation[1]<0||AIlocation[1]>2) {
							System.out.println("badbadbad");
							//out of range
						}
						board = playStep(board, AIlocation, AISymbol);
						System.out.println("AI played: "+AIlocation[0]+","+AIlocation[1]);
						printBoard(board);
						isLost = isWin(board,AIlocation,AISymbol,new ArrayList<int[]>());
						isEnd = isEnd(board);						
					}
					if(isLost) {
						System.out.println("You lost the game!");
						if(logO.size()>0 && logX.size()>0)
							trainingWin(logO,logX,data,AISymbol);
					}
					else if(!isEnd){
						System.out.println("please input your play in \"x,y\" format");
						String[] locationString = input.nextLine().split(",");
						int[] location = new int[2];
						location[0] = Integer.parseInt(locationString[0]);
						location[1] = Integer.parseInt(locationString[1]);
						
						gameStatus=readGameStatus(board);
						if (playerSymbol==2) {
							gameStatus = reverseStatus(gameStatus);
						}
						step = convertStep(location);
						if(playerSymbol == 1)
							logO.add(gameStatus + " " + step);
						else if(playerSymbol==2)
							logX.add(gameStatus+" "+step);
						if(board[location[1]][location[0]]!=0) {
							System.out.println("you made an invalid move, please restart the game");
							isFair = false;
							break;
						}
						board = playStep(board,location,playerSymbol);
						//System.out.println(readGameStatus(board));
						printBoard(board);
						isWin = isWin(board,location,playerSymbol,new ArrayList<int[]>());
					}
				}
				else {
					isTie = true;
					System.out.println("The game ended tie");
				}
			}
			if(isWin) {
				System.out.println("You won the game!");
				if(logO.size()>0 && logX.size()>0)
					trainingWin(logO,logX,data,playerSymbol);
			}
			if(isTie) {
				trainingTie(logO,logX,data,playerSymbol==1);
			}
			//writeData(data,dbStartingFile);
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
			// check if the situation is possible
			int count1 = 0;
			int count2 = 0;
			String dataString = " S ";
			for (int i = 0; i < line.length(); i++) {
				if (Integer.parseInt(Character.toString(line.charAt(i))) == 1) {
					dataString += "0 ";
					count1++;
				} else if (Integer.parseInt(Character.toString(line.charAt(i))) == 2) {
					dataString += "0 ";
					count2++;
				} else {
					dataString +="10000 ";
				}
			}
			if (Math.abs(count1 - count2) < 2 && count2 >= count1) {
				line += dataString;
			}
			initData.add(line);
		} else if (line.length() < 9) {
			for (int data = 0; data < 3; data++) {
				initDataRec(line, initData, data);
			}
		}
	}
	
	private static void trainingTie(ArrayList<String> logO,ArrayList<String> logX,ArrayList<String> data,boolean start) {
		for (String o : logO) {
			String[] content = o.split(" ");
			int dbindex = Integer.valueOf(content[0], 3);
			int dbstep = Integer.parseInt(content[1]);
			String dbline = data.get(dbindex);
			String[] dblineSplit = dbline.split(" ");
			//O first
			if(start)
				dblineSplit[dbstep + 2] = "" + (int)((Integer.parseInt(dblineSplit[dbstep + 2]) * 0.99 + 0.5));
			//X first
			else
				dblineSplit[dbstep + 2] = "" + (int)((Integer.parseInt(dblineSplit[dbstep + 2]) *1.01 + 0.5));
			String newdbline = "";
			for (String part : dblineSplit) {
				newdbline = newdbline + part + " ";
			}
			data.set(dbindex, newdbline);
		}
		// change X
		for (String x : logX) {
			String[] content = x.split(" ");
			Integer dbindex = Integer.valueOf(content[0], 3);
			int dbstep = Integer.parseInt(content[1]);
			String dbline = data.get(dbindex);
			String[] dblineSplit = dbline.split(" ");
			//O first
			if(start)
				dblineSplit[dbstep + 2] = "" + (int)((Integer.parseInt(dblineSplit[dbstep + 2]) * 0.99 + 0.5));
			//X first
			else
				dblineSplit[dbstep + 2] = "" + (int)((Integer.parseInt(dblineSplit[dbstep + 2]) *1.01 +0.5));
			String newdbline = "";
			for (String part : dblineSplit) {
				newdbline = newdbline + part + " ";
			}
			data.set(dbindex, newdbline);
		}
	}
	private static void trainingWin(ArrayList<String> logO,ArrayList<String> logX,ArrayList<String> data,int playerSymbol) {
		// O win
		if (playerSymbol == 1) {
			// +O
			for (int o=0;o<logO.size();o++) {
				String[] content = logO.get(o).split(" ");
				int dbindex = Integer.valueOf(content[0], 3);
				int dbstep = Integer.parseInt(content[1]);
				String dbline = data.get(dbindex);
				String[] dblineSplit = dbline.split(" ");
				dblineSplit[dbstep + 2] = ""
						+ Math.max(0, (int)((Integer.parseInt(dblineSplit[dbstep + 2]) +Math.pow(4, o))));
				String newdbline = "";
				for (String part : dblineSplit) {
					newdbline = newdbline + part + " ";
				}
				data.set(dbindex, newdbline);
			}
			String lasto = logO.get(logO.size() - 1);
			String[] contentlasto = lasto.split(" ");
			int dbindexlasto = Integer.valueOf(contentlasto[0], 3);
			int dbsteplasto = Integer.parseInt(contentlasto[1]);
			String dblinelasto = data.get(dbindexlasto);
			String[] dblineSplitlasto = dblinelasto.split(" ");
			for(int i = 0;i<9;i++) {
				dblineSplitlasto[i + 2]=""+ Math.max(0, (Integer.parseInt(dblineSplitlasto[i + 2]) - 1000));
			}
			dblineSplitlasto[dbsteplasto + 2] = ""
					+ Math.max(0, (Integer.parseInt(dblineSplitlasto[dbsteplasto + 2]) + 2000));
			String newdblinelasto = "";
			for (String part : dblineSplitlasto) {
				newdblinelasto = newdblinelasto + part + " ";
			}
			data.set(dbindexlasto, newdblinelasto);
			// -X
			for (int x =0;x<logX.size();x++) {
				String[] content = logX.get(x).split(" ");
				int dbindex = Integer.valueOf(content[0], 3);
				int dbstep = Integer.parseInt(content[1]);
				String dbline = data.get(dbindex);
				String[] dblineSplit = dbline.split(" ");
				dblineSplit[dbstep + 2] = ""
						+ Math.max(0, (int)((Integer.parseInt(dblineSplit[dbstep + 2]) -Math.pow(4, x))));
				String newdbline = "";
				for (String part : dblineSplit) {
					newdbline = newdbline + part + " ";
				}
				data.set(dbindex, newdbline);
			}
			String lastx = logX.get(logX.size() - 1);
			String[] contentlastx = lastx.split(" ");
			int dbindexlastx = Integer.valueOf(contentlastx[0], 3);
			int dbsteplastx = Integer.parseInt(contentlastx[1]);
			String dblinelastx = data.get(dbindexlastx);
			String[] dblineSplitlastx = dblinelastx.split(" ");
			dblineSplitlastx[dbsteplastx + 2] = ""
					+ Math.max(0, (Integer.parseInt(dblineSplitlastx[dbsteplastx + 2]) - 5000));
			String newdblinelastx = "";
			for (String part : dblineSplitlastx) {
				newdblinelastx = newdblinelastx + part + " ";
			}
			data.set(dbindexlastx, newdblinelastx);
		}
		// X win
		else if (playerSymbol == 2) {
			// -O
			for (int o=0;o<logO.size();o++) {
				String[] content = logO.get(o).split(" ");
				int dbindex = Integer.valueOf(content[0], 3);
				int dbstep = Integer.parseInt(content[1]);
				String dbline = data.get(dbindex);
				String[] dblineSplit = dbline.split(" ");
				dblineSplit[dbstep + 2] = ""
						+ Math.max(0, (int)(Integer.parseInt(dblineSplit[dbstep + 2]) - Math.pow(4, o)));
				String newdbline = "";
				for (String part : dblineSplit) {
					newdbline = newdbline + part + " ";
				}
				data.set(dbindex, newdbline);
			}
			
			if(logO.size()==0) {
				//
				System.out.println();
			}
			
			String lasto = logO.get(logO.size() - 1);
			String[] contentlasto = lasto.split(" ");
			int dbindexlasto = Integer.valueOf(contentlasto[0], 3);
			int dbsteplasto = Integer.parseInt(contentlasto[1]);
			String dblinelasto = data.get(dbindexlasto);
			String[] dblineSplitlasto = dblinelasto.split(" ");
			dblineSplitlasto[dbsteplasto + 2] = ""
					+ Math.max(0, (Integer.parseInt(dblineSplitlasto[dbsteplasto + 2]) - 5000));
			String newdblinelasto = "";
			for (String part : dblineSplitlasto) {
				newdblinelasto = newdblinelasto + part + " ";
			}
			data.set(dbindexlasto, newdblinelasto);
			// +X
			for (int x =0;x<logX.size();x++) {
				String[] content = logX.get(x).split(" ");
				int dbindex = Integer.valueOf(content[0], 3);
				int dbstep = Integer.parseInt(content[1]);
				String dbline = data.get(dbindex);
				String[] dblineSplit = dbline.split(" ");
				dblineSplit[dbstep + 2] = ""
						+ Math.max(0, (int)(Integer.parseInt(dblineSplit[dbstep + 2]) + Math.pow(4, x)));
				String newdbline = "";
				for (String part : dblineSplit) {
					newdbline = newdbline + part + " ";
				}
				data.set(dbindex, newdbline);
			}
			String lastx = logX.get(logX.size() - 1);
			String[] contentlastx = lastx.split(" ");
			int dbindexlastx = Integer.valueOf(contentlastx[0], 3);
			int dbsteplastx = Integer.parseInt(contentlastx[1]);
			String dblinelastx = data.get(dbindexlastx);
			String[] dblineSplitlastx = dblinelastx.split(" ");
			for(int i = 0;i<9;i++) {
				dblineSplitlastx[i + 2]=""+ Math.max(0, (Integer.parseInt(dblineSplitlastx[i + 2]) - 1000));
			}
			dblineSplitlastx[dbsteplastx + 2] = ""
					+ Math.max(0, (Integer.parseInt(dblineSplitlastx[dbsteplastx + 2]) + 2000));
			String newdblinelastx = "";
			for (String part : dblineSplitlastx) {
				newdblinelastx = newdblinelastx + part + " ";
			}
			data.set(dbindexlastx, newdblinelastx);
		}
	}
	//train with version if lose to the version
	private static void trainingWithLose(ArrayList<String> logNew,ArrayList<String> data) {
		for (int x =0;x<logNew.size();x++) {
			String[] content = logNew.get(x).split(" ");
			int dbindex = Integer.valueOf(content[0], 3);
			int dbstep = Integer.parseInt(content[1]);
			String dbline = data.get(dbindex);
			String[] dblineSplit = dbline.split(" ");
			dblineSplit[dbstep + 2] = ""
					+ Math.max(0, (int)((Integer.parseInt(dblineSplit[dbstep + 2]) -Math.pow(20, x))));
			String newdbline = "";
			for (String part : dblineSplit) {
				newdbline = newdbline + part + " ";
			}
			data.set(dbindex, newdbline);
		}
		String last = logNew.get(logNew.size() - 1);
		String[] contentlast = last.split(" ");
		int dbindexlast = Integer.valueOf(contentlast[0], 3);
		int dbsteplast = Integer.parseInt(contentlast[1]);
		String dblinelast = data.get(dbindexlast);
		
		//System.out.println("before "+dblinelast);
		
		String[] dblineSplitlast = dblinelast.split(" ");
		
		//System.out.println(dblineSplitlast[dbsteplast + 2]);
		//System.out.println(last);
		
		dblineSplitlast[dbsteplast + 2] = ""
				+ Math.max(0, (Integer.parseInt(dblineSplitlast[dbsteplast + 2]) - 5000));

		//System.out.println(dblineSplitlast[dbsteplast + 2]);
		
		String newdblinelast = "";
		for (String part : dblineSplitlast) {
			newdblinelast = newdblinelast + part + " ";
		}
		data.set(dbindexlast, newdblinelast);
		
		//System.out.println("after  "+newdblinelast);
		
	}
	// train the AI
	public static double training(double dbVersion, ArrayList<String> data) {

		int OwinGames = 0;
		int XwinGames = 0;
		int tieGames = 0;
		int totalGames = 0;
		int OffGames = 0;
		int XffGames =0;
		int trainingIter = 15;
		for (int n = 0; n < trainingIter; n++) {
			// play 10000 games per iteration
			for (int i = 0; i < 100000; i++) {
				totalGames++;
				
				ArrayList<String> logO = new ArrayList<String>();
				ArrayList<String> logX = new ArrayList<String>();
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
					if(!player)
						gameStatus = reverseStatus(gameStatus);
					// search for strategy
					String[] strategy = searchStrategy(gameStatus, data);
					// play
					// Symbol that a player use
					// 1 for O
					// 2 for X
					int playerSymbol = 0;
					if (player) {
						playerSymbol = 1;
					} else {
						playerSymbol = 2;
					}				
					// find the right step to play
					int step = calculateStep(strategy, board);
					// no place resulted tieGames
					if (step == -999) {
						gameover = true;
						tieGames++;
						// change O
						trainingTie(logO,logX,data,start);
					} else if(step == -1) {
						gameover = true;
						if(player) {
							OffGames++;
							XwinGames++;
							if(logO.size()>0 && logX.size()>0)
								trainingWin(logO,logX,data,2);
						}
						else {
							XffGames++;
							OwinGames++;
							if(logO.size()>0 && logX.size()>0)
								trainingWin(logO,logX,data,1);
						}
					}
					// not tie
					else {
						if (player) {
							logO.add(gameStatus + " " + step);
						} else {
							logX.add(gameStatus + " " + step);
						}
						
						// convert step into x,y coordinate location
						int[] location = convertLocation(step);
						// play
						board = playStep(board, location, playerSymbol);
						// check win condition
						ArrayList<int[]> lastlocation = new ArrayList<int[]>();
						boolean isWin = isWin(board, location, playerSymbol, lastlocation);
						// change strategy based on W/L result
						if (isWin) {
							if(logO.size()>0 && logX.size()>0)
								trainingWin(logO,logX,data,playerSymbol);
							if(playerSymbol ==1) {
								OwinGames++;
							}else if(playerSymbol ==2) {
								XwinGames++;
							}
							gameover = true;
						}
						player = !player;
					}
				}
			}
			// create new version for data file
			dbVersion += 0.1;
			writeData(data, "db-" + ("" + dbVersion).substring(0, 3) + ".txt");
		}
		System.out.println("OwinGames: "+OwinGames);
		System.out.println("XwinGames: "+XwinGames);
		System.out.println("tieGames: "+tieGames);
		System.out.println("totalGames: "+totalGames);
		System.out.println("OffGames: "+OffGames);
		System.out.println("XffGames: "+XffGames);
		return dbVersion;
	}
	//training with version
	public static double training(double dbVersion, ArrayList<String> data1,ArrayList<String> data2) {
		
		ArrayList<String> data = data1;
		int OwinGames = 0;
		int XwinGames = 0;
		int tieGames = 0;
		int totalGames = 0;
		int XffGames = 0;
		int OffGames = 0;
		int trainingIter = 10;
		for (int n = 0; n < trainingIter; n++) {
			// play 10000 games per iteration
			for (int i = 0; i < 1000000; i++) {
				totalGames++;
				
				ArrayList<String> logO = new ArrayList<String>();
				ArrayList<String> logX = new ArrayList<String>();
				// play the game and change "data"
				boolean gameover = false;
				int[][] board = new int[3][3];
				// pick a random player to start
				// true data1 go; false data2 go
				boolean player = randGenerator.nextBoolean();
				while (!gameover) {
					// read in the board
					String gameStatus = readGameStatus(board);
					data = data1;
					if(!player) {
						gameStatus = reverseStatus(gameStatus);	
						data = data2;
					}
					// search for strategy
					String[] strategy = searchStrategy(gameStatus, data);
					// play
					// Symbol that a player use
					// 1 for O
					// 2 for X
					int playerSymbol = 0;
					if (player) {
						playerSymbol = 1;
					} else {
						playerSymbol = 2;
					}				
					// find the right step to play
					int step = calculateStep(strategy, board);
					
//					if(step==0 && Integer.parseInt(strategy[0])==0) {
//						System.out.println(gameStatus);
//						printBoard(board);
//						System.out.println(step);
//						for(String a:strategy) {
//							System.out.print(a+" ");
//						}
//						System.out.println();
//					}
					
					
					// no place resulted tieGames
					if (step == -999) {
						gameover = true;
						tieGames++;
						// change O
						//trainingTie(logO,logX,data,start);
					} else if(step == -1) {
						gameover = true;
						if(player) {
							OffGames++;
							XwinGames++;
							if(logO.size()>0)
								trainingWithLose(logO,data);
						}
						else {
							XffGames++;
							OwinGames++;
						}
					}
					// not tie
					else {
						if (player) {
							logO.add(gameStatus + " " + step);
						} else {
							logX.add(gameStatus + " " + step);
						}
						
						// convert step into x,y coordinate location
						int[] location = convertLocation(step);
						// play
						board = playStep(board, location, playerSymbol);
						// check win condition
						ArrayList<int[]> lastlocation = new ArrayList<int[]>();
						boolean isWin = isWin(board, location, playerSymbol, lastlocation);
						// change strategy based on W/L result
						if (isWin) {
							if(!player) {
								if(logO.size()>0) {
									//System.out.println(data1.get(Integer.valueOf(logO.get(logO.size()-1).split(" ")[0],3)));
									trainingWithLose(logO,data1);
									//System.out.println(data1.get(Integer.valueOf(logO.get(logO.size()-1).split(" ")[0],3)));
									//System.out.println("time");
								}
							}
							if(playerSymbol ==1) {
								OwinGames++;
							}else if(playerSymbol ==2) {
								XwinGames++;
							}								
							gameover = true;
						}
						if(player) {
							data=data2;
						}
						else {
							data=data1;
						}
						player = !player;
					}
				}
			}
			// create new version for data file
			dbVersion += 0.1;
			writeData(data1, "db-" + ("" + dbVersion).substring(0, 3) + ".txt");
		}
		System.out.println("OwinGames: "+OwinGames);
		System.out.println("XwinGames: "+XwinGames);
		System.out.println("tieGames: "+tieGames);
		System.out.println("totalGames: "+totalGames);
		System.out.println("OffGames: "+OffGames);
		System.out.println("XffGames: "+XffGames);
		return dbVersion;
	}

	public static void game(ArrayList<String> data1, ArrayList<String> data2) {

		int OwinGames = 0;
		int XwinGames = 0;
		int tieGames = 0;
		int totalGames = 0;
		int OffGames = 0;
		int XffGames = 0;
		int gameNum = 10000000;
		for (int i = 0; i < gameNum; i++) {
			totalGames++;

			ArrayList<String> logO = new ArrayList<String>();
			ArrayList<String> logX = new ArrayList<String>();

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
				// search for strategy
				String[] strategy = new String[0];
				if (player) {
					strategy = searchStrategy(gameStatus, data1);
				} else {
					gameStatus = reverseStatus(gameStatus);
					strategy = searchStrategy(gameStatus, data2);
				}
				// play
				// Symbol that a player use
				// 1 for O
				// 2 for X
				int playerSymbol = 0;
				if (player) {
					playerSymbol = 1;
				} else {
					playerSymbol = 2;
				}

				// find the right step to play
				int step = calculateStep(strategy, board);
				// no place resulted tieGames
				if (step == -999) {
					gameover = true;
					tieGames++;
					// System.out.println("tie");
				} else if(step == -1) {
					gameover = true;
					if(player) {
						OffGames++;
						XwinGames++;
					}
					else {
						XffGames++;
						OwinGames++;
					}
//					System.out.println("log o");
//					for(String o:logO) {
//						System.out.println(o);
//					}
//					System.out.println("log x");
//					for(String x:logX) {
//						System.out.println(x);
//					}
//					System.out.println("end log");
				}
				else {
					if (player) {
						logO.add(gameStatus + " " + step);
					} else {
						logX.add(gameStatus + " " + step);
					}
					
					// convert step into x,y coordinate location
					int[] location = convertLocation(step);
					// play
					board = playStep(board, location, playerSymbol);
					
					//printBoard(board);
					//System.out.println();
					// check win condition
					ArrayList<int[]> lastlocation = new ArrayList<int[]>();

					// System.out.println(readGameStatus(board));/////////////////////////////////////////////////////////////////

					boolean isWin = isWin(board, location, playerSymbol, lastlocation);
					// change strategy based on W/L result
					if (isWin) {
						if (playerSymbol == 1) {
							OwinGames++;
							
							
						} else if (playerSymbol == 2) {
							XwinGames++;

//							System.out.println("log o");
//							for(String o:logO) {
//								System.out.println(o);
//							}
//							System.out.println("log x");
//							for(String x:logX) {
//								System.out.println(x);
//							}
//							System.out.println("end log");
//							return;
						}
						gameover = true;
					}

					player = !player;
				}
			}
		}
		System.out.println("OwinGames: "+OwinGames);
		System.out.println("XwinGames: "+XwinGames);
		System.out.println("tieGames: "+tieGames);
		System.out.println("totalGames: "+totalGames);
		System.out.println("OffGames: "+OffGames);
		System.out.println("XffGames: "+XffGames);
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

	//reverse game status for player 2
	private static String reverseStatus(String gameStatus) {
		String reverse = "";
		for(int i = 0; i<gameStatus.length();i++) {
			if(gameStatus.charAt(i)=='1')
				reverse+=2;
			else if(gameStatus.charAt(i)=='2')
				reverse+=1;
			else
				reverse+=0;
		}
		return reverse;
	}
	// find strategy in db based on game status
	private static String[] searchStrategy(String gameStatus, ArrayList<String> data) {
		// convert game status from b3 to b10 to find the index of line in ArrayList
		int index = Integer.valueOf(gameStatus, 3);
		String strategyLine = data.get(index);
		int beginIndex = strategyLine.indexOf('S') + 2;
		String[] strategy = strategyLine.substring(beginIndex).split(" ");
		return strategy;
	}

	// choose step randomly based on strategy
	private static int calculateStep(String[] strategy, int[][] board) {
		int result = -999;
		int bound = 0;
		int[] stratInt = new int[strategy.length];
		for (int i = 0; i < strategy.length; i++) {
			stratInt[i] = Integer.parseInt(strategy[i]);
			int[] location = convertLocation(i);
			if (board[location[1]][location[0]] == 0)
				bound += stratInt[i];
		}
		// choose step randomly based on strategy
		int step = -999;
		if (bound > 0) {
			step = randGenerator.nextInt(bound);
			for (int i = 0; i < stratInt.length; i++) {
				int[] location = convertLocation(i);
				if (board[location[1]][location[0]] == 0) {
					if (step <= stratInt[i] && stratInt[i]!=0) {
						result = i;
						i = stratInt.length;
					} else {
						step -= stratInt[i];
					}
				}
			}
		}
		if(bound==0 && !isEnd(board)) {
			return -1;
		}
		return result;
	}
	
	// convert x,y coordinates into index step
	private static int convertStep(int[] location) {
		int step = -999;
		step = location[0]+location[1]*3;
		return step;
	}
	// convert step into x,y coordinates
	private static int[] convertLocation(int step) {
		int[] location = new int[2];
		location[0]=step%3;
		location[1]=step/3;
		return location;
	}

	// play
	private static int[][] playStep(int[][] board, int[] location, int playerSymbol) {
		board[location[1]][location[0]] = playerSymbol;
		return board;
	}

	// check if the game is over
	private static boolean isWin(int[][] board, int[] location, int playerSymbol, ArrayList<int[]> lastlocation) {

		for (int x = -1; x < 2; x++) {
			for (int y = -1; y < 2; y++) {
				int locx = location[0];
				int locy = location[1];
				// x in range
				if (locx + x < 3 && locx + x > -1) {
					// y in range
					if (locy + y < 3 && locy + y > -1) {
						// not it-self
						if (locy + y != locy || locx + x != locx) {
							// if there is a friendly symbol next to location
							if (board[locy + y][locx + x] == playerSymbol) {
								// has a opposite side
								if (locx - x < 3 && locx - x > -1 && locy - y < 3 && locy - y > -1) {
									// if there is a friendly symbol on the opposite side of the location
									if (board[locy - y][locx - x] == playerSymbol) {
										if (lastlocation.size() > 0)
											lastlocation = new ArrayList<int[]>();
										// System.out.println((locx+x)+","+(locy+y)+" "+board[(locy+y)][(locx+x)]);
										// System.out.println((locx)+","+(locy)+" "+board[(locy)][(locx)]);
										// System.out.println((locx-x)+","+(locy-y)+" "+board[(locy-y)][(locx-x)]);
										return true;
									}
								}
								// not last location
								boolean islast = false;
								for (int[] lastloc : lastlocation) {
									if (locx + x == lastloc[0] && locy + y == lastloc[1]) {
										islast = true;
									}
								}
								if (!islast) {
									int[] nextloc = { locx + x, locy + y };
									lastlocation.add(location);
									boolean isWin = isWin(board, nextloc, playerSymbol, lastlocation);
									if (isWin) {
										return isWin;
									}
								}
							}
						}
					}
				}
			}
		}

		if (lastlocation.size() != 0)
			lastlocation.remove(lastlocation.size() - 1);
		return false;
	}
	//check if the board is full
	private static boolean isEnd(int[][] board) {
		boolean isEnd = true;
		for(int x=0;x<3;x++) {
			for(int y=0;y<3;y++) {
				if(board[x][y]==0) {
					isEnd = false;
				}
			}
		}
		return isEnd;
	}
	//print the board
	private static void printBoard(int[][]board) {
		for(int x = 0;x<3;x++) {
			for(int y =0;y<3;y++){
				System.out.print(board[x][y]+" ");
			}
			System.out.println();
		}
	}
}
