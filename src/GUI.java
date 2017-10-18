//import java.awt.Graphics;
//import java.awt.Graphics2D;
//
//import javax.swing.JFrame;
//import javax.swing.JPanel;
//
//public class GUI extends JPanel {
//	
//	JFrame frame;
//	int cells_width;
//	int cells_height;
//
//	public GUI(int width_in_cells, int height_in_cells) {
//		frame = new JFrame("A* Pathfinding algorithm");
//		frame.setSize(650, 650);
//		frame.setLocation(400, 300);
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		frame.setVisible(true);
//		
//		cells_width = (int) frame.getWidth() / width_in_cells;
//		cells_height = (int) frame.getHeight() / height_in_cells;
//		
//		int x = 0;
//		int y = 0;
//		for (A_Star.Cell[] cells_columns : A_Star.algorithm.mCells) {
//			
//			x++;
//		}
//	}
//	
//	private class Cell extends JPanel {
//		Cell() {
//			setSize(cells_width, cells_height);
//		}
//		
//		@Override
//		public void paintComponent(Graphics graphics) {
//			super.paintComponent(graphics);
//			Graphics2D g2d = (Graphics2D) graphics;
//		}
//	}
//
//}
