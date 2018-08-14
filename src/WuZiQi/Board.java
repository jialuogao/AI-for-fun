package WuZiQi;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;

public class Board extends Canvas{
	
	public final int SQUARELENGTH = 37;
	private final int PIECELENGTH = 30;
	public Image boardTexture;
	public int XoffSet;
	public int YoffSet;

	final private int ROWS = 15;
	public int[][] board = new int[ROWS][ROWS];
	public int playerColor = PieceColor.BLACK;
	
	public Board() {
		boardTexture = Toolkit.getDefaultToolkit().getImage("src/WuZiQi/chessboard2.png");
		if(boardTexture == null) {
			System.err.println("No texture exist");
		}
		int imgWidth = boardTexture.getHeight(this);
		int imgHeight = boardTexture.getWidth(this);
		int FWidth = getWidth();
		int FHeight= getHeight();
		int x=(FWidth-imgWidth)/2;
		int y=(FHeight-imgHeight)/2;
		XoffSet = 28+x;
		YoffSet = 30+y;
	}
	
	// x, y: 0 => 14
	public void drawPiece(int color, int x, int y, Graphics g) {
		
		if(color == PieceColor.BLACK) {
			g.setColor(Color.BLACK);
		}
		else if(color == PieceColor.WHITE) {
			g.setColor(Color.WHITE);
		}
		else {
			return;
		}
		g.fillArc(x, y, PIECELENGTH, PIECELENGTH, 0, 360);//arc start from degree 0 to 360
	}
	
	public void paint(Graphics g) {
		int x = XoffSet - 28;
		int y = YoffSet - 30;
		g.drawImage(boardTexture, x, y, null);

		g.setColor(Color.BLACK);
		//Counter-clockwise
		//top
		g.fillRect(x+14, y+16, 562-16, 5);
		g.fillRect(x+14, y+16, 5, 562-16);
		g.fillRect(x+14, y+562-3, 564-16, 5);
		g.fillRect(x+562-5, y+16, 5, 564-16);
        
		
		g.fillArc(XoffSet+7*SQUARELENGTH-6, YoffSet+7*SQUARELENGTH-6, 12, 12, 0, 360);
        g.fillArc(XoffSet+3*SQUARELENGTH-6, YoffSet+3*SQUARELENGTH-6, 12, 12, 0, 360);
        g.fillArc(XoffSet+3*SQUARELENGTH-6, YoffSet+11*SQUARELENGTH-6, 12, 12, 0, 360);
        g.fillArc(XoffSet+11*SQUARELENGTH-6, YoffSet+3*SQUARELENGTH-6, 12, 12, 0, 360);
        g.fillArc(XoffSet+11*SQUARELENGTH-6, YoffSet+11*SQUARELENGTH-6, 12, 12, 0, 360);

        for(int i = 0; i < ROWS; i++) {
            g.drawLine(i*SQUARELENGTH+XoffSet, YoffSet, i*SQUARELENGTH+XoffSet, (ROWS-1)*SQUARELENGTH+YoffSet);
            g.drawLine(XoffSet, i*SQUARELENGTH+YoffSet, (ROWS-1)*SQUARELENGTH+XoffSet, i*SQUARELENGTH+YoffSet);
        }

        for(int i=0; i<ROWS; i++) {
            for(int j=0; j<ROWS; j++) {
                drawPiece(board[i][j], XoffSet+i*SQUARELENGTH-PIECELENGTH/2, YoffSet+j*SQUARELENGTH-PIECELENGTH/2, g);
            }
        }
	}
	
	
	
	//Board manipulation
	public void switchPlayer() {
		if(playerColor == PieceColor.BLACK) {
			playerColor = PieceColor.WHITE;
		}
		else {
			playerColor = PieceColor.BLACK;
		}
	}
	
	public boolean isEmpty(int x,int y) {
		return PieceColor.EMPTY==board[x][y];
	}
	
	public void addPiece(int x, int y, int color) {
		board[x][y] = color;
	}
	
	public void removePiece(int x, int y) {
		board[x][y] = PieceColor.EMPTY;
	}
	
	// 0 not end; 1 white win; 2 black win; 3 tie
	public int isEnd(int x, int y, int color) {
		for(int i = -1; i<2; i++) {
			for(int j =-1;j<2;j++) {
				int tempx = x+i;
				int tempy = y+j;
				int sum = 1;
				if(tempx!=x || tempy!=y) {
					boolean inLine = isColor(tempx,tempy,color);
					while(inLine) {
						sum+=1;
						tempx += i;
						tempy += j;
						inLine = isColor(tempx,tempy,color);
					}
				}
				tempx = x-i;
				tempy = y-j;
				if(tempx!=x || tempy!=y) {
					boolean inLine = isColor(tempx,tempy,color);
					while(inLine) {
						sum+=1;
						tempx -= i;
						tempy -= j;
						inLine = isColor(tempx,tempy,color);
					}
				}
				if(sum>=5) {
					return color;
				}
			}
		}
		boolean isTie = true;
		for(int[] row : board) {
			for(int val : row) {
				if(val==0) {
					isTie = false;
				}
			}
		}
		if(isTie) {
			return 3;
		}
		return 0;
	}
	
	public boolean isColor(int x, int y, int color) {
		if(x>-1 && x<ROWS
				&& y>-1 && y<ROWS) {
			if(color==board[x][y]) {
				return true;
			}
		}
		return false;
	}
}