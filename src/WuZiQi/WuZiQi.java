package WuZiQi;

import java.awt.*;
import java.awt.event.*;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class WuZiQi extends JPanel{
	
	public Image boardTexture;
	
	Board drawBoard = new Board();
	boolean isEnd = false;
	public void init() {
		drawBoard.setPreferredSize(new Dimension(720, 720));
		
		drawBoard.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				int x = (int) Math.round((e.getX()-drawBoard.XoffSet)/(double)drawBoard.SQUARELENGTH);
				int y = (int) Math.round((e.getY()-drawBoard.YoffSet)/(double)drawBoard.SQUARELENGTH);
				makeMove(x, y);
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseReleased(MouseEvent e) {
			}
		});
		
		JFrame frame = new JFrame("五子棋");
		frame.add(drawBoard);
		//TODO;
		//frame.add(undo button);
		//frame.add(player turn reminder)
		frame.setSize(760,760);
		frame.setVisible(true);
	}
	
	public void makeMove(int x, int y) {
		if(isEnd) {
			
		}
		else {
			if(drawBoard.isEmpty(x,y)) {
				System.out.println(x+"    "+y);
				drawBoard.addPiece(x,y,drawBoard.playerColor);
				drawBoard.repaint();
				if(drawBoard.playerColor==drawBoard.isEnd(x,y,drawBoard.playerColor)) {
					isEnd = true;
					System.out.println("玩家"+drawBoard.playerColor+"获胜！");
				}
				drawBoard.switchPlayer();
			}					
		}
	}
}
