import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class A_Star extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static int CELL_COUNT = 20;

	public int CELLS_HORIZONTAL, CELLS_VERTICAL;

//	public static final int DIAGONAL_COST = 20;
	public static final int DIAGONAL_COST = 14;
	public static final int V_H_COST = 10;

	JFrame frame;
	JPanel panel;

	int cell_width = -1;
	int cell_height = -1;

	Comparator<Cell> comparator = new Comparator<A_Star.Cell>() {

		@Override
		public int compare(Cell c1, Cell c2) {
			return c1.finalCost < c2.finalCost ? -1:
				c1.finalCost > c2.finalCost ? 1 : 0;
		}
	};

	public class Cell extends JPanel {
		private int x;
		private int y;
		private int heuristicCost = 0;
		private int finalCost = 0;
		private float vel_factor = 1;
		private boolean isPartOfPath = false;

		private Cell parent;

		private Cell(int ind_x, int ind_y) {
			x = ind_x;
			y = ind_y;

			addMouseListener(new MouseListener() {

				@Override
				public void mouseReleased(MouseEvent e) {
					// TODO Auto-generated method stub

				}

				@Override
				public void mousePressed(MouseEvent e) {
					// TODO Auto-generated method stub

				}

				@Override
				public void mouseExited(MouseEvent e) {
					// TODO Auto-generated method stub

				}

				@Override
				public void mouseEntered(MouseEvent e) {
					// TODO Auto-generated method stub

				}

				@Override
				public void mouseClicked(MouseEvent e) {
//					if ()
					vel_factor = 0.5f;
					setBackground(Color.decode("#D2691E"));
					repaint();
					
//					panel.remove(Cell.this);
//					setBlocked(x, y);
					
					panel.revalidate();
					panel.repaint();
				}
			});

			//			heuristicCost = Math.abs(x - endX) + Math.abs(y - endY);
		}

		void drawOnCanvas(int width, int height) {
			setSize(width, height);
			setLocation(width * x, height * y);
			//			System.out.println("Set location to " + getLocation());
			if (vel_factor == 0.5f) setBackground(Color.decode("#D2691E"));
			setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
			repaint();
		}

		@Override
		public String toString() {
			return "[" + x + ", " + y + "]";
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			g2d.setFont(new Font("Helvetica", Font.BOLD, 20));
			g2d.drawString("" + finalCost, 5, 15);
			g2d.setColor(Color.orange);
			g2d.drawString("" + heuristicCost, 5, 37);
		}
	}

	//	int[][] blocked;

	//	Cell[] cells;
	//	ArrayList<Cell> cells;
	Cell[][] mCells;
	Cell startCell;
	Cell endCell;

	PriorityQueue<Cell> open;
	boolean[][] closed;
	boolean[][] blocked;

	int startX, startY;

	int endX = -1, endY = -1;

	long t_start, t_end;



	public void checkAndUpdateCost(Cell currentCell, Cell t, int cost) {
		//		if (blocked[t.x][t.y] || closed[t.x][t.y]) return;
		if (t == null || closed[t.x][t.y]) return;

		int t_final_cost = (currentCell.finalCost - currentCell.heuristicCost) + t.heuristicCost + (int) (cost * (1 / t.vel_factor));

		boolean tIsInOpen = open.contains(t);
		if (!tIsInOpen || t_final_cost < t.finalCost) {
			t.finalCost = t_final_cost;
			t.parent = currentCell;
			if (!tIsInOpen) {
				open.add(t);
				if (mCells[t.x][t.y].vel_factor != 0.5) mCells[t.x][t.y].setBackground(Color.GREEN);
			}
		}
	}

	//	private int expandCellsArray() {
	//		int index = cells.length;
	//		cells = Arrays.copyOf(cells, cells.length + 1);
	//		return index;
	//	}

	boolean finished = false;
	int i = 0;

	public void findPath() throws IllegalArgumentException {
		if (endCell == null)
			throw new IllegalArgumentException("You have to set a final end cell!", new Throwable("No end cell set"));
		if (startCell == null)
			throw new IllegalArgumentException("You have to set a starting cell!", new Throwable("No starting cell set"));

		buildGUI();

		long start = System.currentTimeMillis();

		endCell.finalCost = 0;

		open.add(startCell);
		Cell current = null;

		//		int i = 0;

		frame.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {}
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER && !finished) {
					System.out.println("Finding path...");
					
//					whileLoop(); 												// De-comment this line to make a step each time you press ENTER

					long start = System.currentTimeMillis();																// De-Comment this
																															// to let the algorithm
					while (!finished)																						// find the path
						whileLoop();																						// automatically and
																															// record
					System.out.println("Algorithm itself took " + (System.currentTimeMillis() - start) + " Miliseconds");	// the time
				}
			}
		});
	}

	private void finished(boolean success) {
		this.finished = true;
		if (success) {
			System.out.println("Success!");
			//			System.out.println("Algorithm finished!");
			//			System.out.println("Scores for cells: " + System.lineSeparator());
			//			System.out.println("Algorithm took " + (System.currentTimeMillis() - t_start) + " Miliseconds");

			Cell[] partOfPath = new Cell[0];
			// Trace back path
			System.out.println("Path: ");
			Cell current = mCells[endX][endY];
			//			System.out.print(current.toString());
			//			Cell current = getCellByCoords(endX, endY);
			System.out.println(current);
			int i = 0;
			mCells[current.x][current.y].setBackground(Color.CYAN);
			//			getCellByCoords(current.x, current.y).setBackground(Color.CYAN);
			while (current.parent != null) {
				mCells[current.parent.x][current.parent.y].setBackground(Color.CYAN);
				//				getCellByCoords(current.parent.x, current.parent.y).setBackground(Color.CYAN);

				mCells[current.parent.x][current.parent.y].isPartOfPath = true;
				//				getCellByCoords(current.parent.x, current.parent.y).isPartOfPath = true;
				System.out.print(" -> " + current.parent.toString());
				current = current.parent;
				i++;
			}
			System.out.println();
		} else {
//			System.out.println("Fail!");
			System.out.println();
			System.out.println("No possible path. I am so sorry.");
			System.out.println();
		}
	}

	private void whileLoop() {
		Cell current = open.poll();
		//		System.out.println("Current has f-cost of '" + current.finalCost + "'");
		if (current == null) {
			finished(false);
			finished = true;
			return;
		}

		closed[current.x][current.y] = true;
		mCells[current.x][current.y].setBackground(Color.RED);
		
		if (current.x == endX && current.y == endY) {
			finished(true);
			finished = true; // We did it!
		}

		Cell t;

		// Neighbor to the left
		if (current.x - 1 >= 0) {
			t = mCells[current.x - 1][current.y];
			checkAndUpdateCost(current, t, V_H_COST);

			// Neighbor north-west (diagonally)
			if (current.y - 1 >= 0) {
				t = mCells[current.x - 1][current.y - 1];
				checkAndUpdateCost(current, t, DIAGONAL_COST);
			}

			// Neighbor south-west (diagonally)
			if (current.y + 1 < mCells[0].length) {
				t = mCells[current.x - 1][current.y + 1];
				checkAndUpdateCost(current, t, DIAGONAL_COST);
			}
		}

		// Neighbor above
		if (current.y - 1 >= 0) {
			t = mCells[current.x][current.y - 1];
			checkAndUpdateCost(current, t, V_H_COST);
		}

		// Neighbor below
		if (current.y + 1 < mCells[0].length) {
			t = mCells[current.x][current.y + 1];
			checkAndUpdateCost(current, t, V_H_COST);
		}

		// Neighbor to the right
		if (current.x + 1 < mCells.length) {
			t = mCells[current.x + 1][current.y];
			checkAndUpdateCost(current, t, V_H_COST);

			// Neighbor north-east (diagonally)
			if (current.y - 1 >= 0) {
				t = mCells[current.x + 1][current.y - 1];
				checkAndUpdateCost(current, t, DIAGONAL_COST);
			}

			// Neighbor south-east (diagonally)
			if (current.y + 1 < mCells[0].length) {
				t = mCells[current.x + 1][current.y + 1];
				checkAndUpdateCost(current, t, DIAGONAL_COST);
			}
		}

	}






	/**
	 * The following methods have to be called in order for the algorithm to work:
	 * 		- {@link #setStartCell(int, int)}
	 * 		- {@link #setEndCell(int, int)}
	 * 
	 * Insert blocked cells via {@link #setBlocked(Cell...)}
	 */
	public A_Star(int cells_horizontal, int cells_vertical) {
		CELLS_HORIZONTAL = cells_horizontal;
		CELLS_VERTICAL = cells_vertical;

		long t_start = System.currentTimeMillis();
		
		mCells = new Cell[cells_horizontal][cells_vertical];
		closed = new boolean[cells_horizontal][cells_vertical];
		blocked = new boolean[cells_horizontal][cells_vertical];
		open = new PriorityQueue<Cell>(comparator);

		frame = new JFrame("A* Pathfinding algorithm");
		frame.setLayout(null);
		frame.setSize(1300, 1300);
		frame.setLocation(400, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		cell_width = (frame.getWidth()) / cells_horizontal;
		cell_height = (frame.getHeight()) / cells_vertical;

		panel = new JPanel();
		panel.setLayout(null);
		panel.setSize(frame.getWidth(), frame.getHeight());
		panel.setBackground(Color.BLACK);

		frame.add(panel);
		frame.requestFocus();

		System.out.println("Setting up UI took " + (System.currentTimeMillis() - t_start) + " Miliseconds"	);
		frame.setSize(frame.getWidth() + 35, frame.getHeight() + 85);
	}

	private void buildGUI() {
		for (int x = 0; x < mCells.length; x++) {
			for (int y = 0; y < mCells[0].length; y++) {
				if (mCells[x][y] != null) {
					mCells[x][y].drawOnCanvas(cell_width, cell_height);
					panel.add(mCells[x][y]);
				}
			}
		}
		panel.repaint();

		mCells[endX][endY].setBackground(Color.BLUE);
		mCells[startX][startY].setBackground(Color.BLUE);
	}

	public A_Star setStartCell(int ind_x, int ind_y) {
		startCell = new Cell(ind_x, ind_y);
		mCells[ind_x][ind_y] = new Cell(ind_x, ind_y);
		
		startX = ind_x;
		startY = ind_y;

		return this;
	}

	public A_Star setEndCell(int ind_x, int ind_y) {
		endCell = new Cell(ind_x, ind_y);
		mCells[ind_x][ind_y] = new Cell(ind_x, ind_y);
		endX = ind_x;
		endY = ind_y;
		
		return this;
	}

	public A_Star setupGrid() {
		System.out.println("Setting up grid");
		for (int x = 0; x < mCells.length; x++) {
			for (int y = 0; y < mCells[0].length; y++) {
				mCells[x][y] = new Cell(x, y);
				mCells[x][y].heuristicCost = (Math.abs(x - endCell.x) + Math.abs(y - endCell.y)) * 10;
			}
		}
		System.out.println("Finished setting up grid.");
		return this;
	}

	public A_Star setBlocked(int x, int y) {
		mCells[x][y] = null;
		closed[x][y] = true;
		return this;
	}

	public static void main(String[] args) {
		A_Star algorithm = new A_Star(CELL_COUNT, CELL_COUNT)
				.setStartCell(0, 0)
				.setEndCell(CELL_COUNT - 1, CELL_COUNT - 1)
				.setupGrid();

		for (int i = 0; i < 500; i++) {
			int x = (int) (Math.random() * CELL_COUNT);
			int y = (int) (Math.random() * CELL_COUNT);
			
			if (x == algorithm.startX && y == algorithm.startY) continue;
			if (x == algorithm.endX && y == algorithm.endY) continue;
			
			if (Math.random() < 0.4)
				algorithm.setBlocked(x, y);
			else {
			if (!algorithm.closed[x][y]) 
				algorithm.mCells[x][y].vel_factor = 0.5f;
			}
		}

		algorithm.t_start = System.currentTimeMillis();
		algorithm.findPath();

		//		System.out.println();
		//		for (int y = 0; y < CELL_COUNT; y++) {
		//			for (int x = 0; x < CELL_COUNT; x++) {
		//				if (algorithm.mCells[x][y] != null) {
		//					if (x == algorithm.startX && y == algorithm.startY) System.out.print("-A- ");
		//					else if (x == algorithm.endX && y == algorithm.endY) System.out.print("-E- ");
		//					else if (algorithm.mCells[x][y].isPartOfPath) {
		//						System.out.print("x   ");
		//						algorithm.mCells[x][y].setBackground(Color.CYAN);
		//					}
		//					else System.out.printf("%-3d ", algorithm.mCells[x][y].finalCost);
		//				}
		//				else System.out.print("BL  ");
		//			}
		//			System.out.println();
		//		}
	}

	//	public Cell getCellByCoords(int x, int y) {
	//		for (int i = 0; i < cells.size(); i++) 
	//			if (cells.get(i).x == x && cells.get(i).y == y) return cells.get(i);
	//
	//		return null;
	//	}

	//	@Override
	//	public void paintComponent(Graphics g) {
	//		super.paintComponent(g);
	//		Graphics2D g2d = (Graphics2D) g;
	//
	//
	//	}
}
