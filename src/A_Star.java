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
	/** The two possible modes for clicking on a cell */
	private static final int CELL_CLICK_SET_WOODS = 0;
	private static final int CELL_CLICK_SET_BLOCKED = 1;

	/** The two possible modes for pressing ENTER */
	private static final int ENTER_MAKE_ONE_STEP = 2;
	private static final int ENTER_COMPLETE_AUTOMATICALLY = 3;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static int CELL_COUNT = 20;
	private static final int DIAGONAL_COST = 14;
	private static final int V_H_COST = 10;

	/** The frame/window containing the main panel */
	JFrame frame;
	
	/** The main panel containing the grid */
	JPanel panel;

	/** Storing the scaled cell width and height */
	int cell_width = -1;
	int cell_height = -1;

	/** The modes for clicking on a cell or pressing enter */
	int cell_click;
	int enter_pressed;

	/** The comparator for sorting the PriorityQueue -> open set
	 *	Polling the queue grabs the Cell object with the lowest f-cost  
	 */
	Comparator<Cell> comparator = new Comparator<A_Star.Cell>() {
		@Override
		public int compare(Cell c1, Cell c2) {
			return c1.finalCost < c2.finalCost ? -1:
				c1.finalCost > c2.finalCost ? 1 : 0;
		}
	};

	public class Cell extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 6829405847730608406L;
		
		// X- and Y- coordinate
		private int x;
		private int y;
		
		// The heuristic cost of the cell object
		private int heuristicCost = 0;
		
		// The final cost of this cell object
		private int finalCost = 0;
		
		// The velocity factor of this cell object
		private float vel_factor = 1;

		// If this cell object is part of the path, this stores the parent cell -> cell before this one
		private Cell parent;

		/**
		 * 
		 * @param ind_x
		 * @param ind_y
		 */
		private Cell(int ind_x, int ind_y) {
			x = ind_x;
			y = ind_y;

			addMouseListener(new MouseListener() {
				@Override
				public void mouseReleased(MouseEvent e) { }

				@Override
				public void mousePressed(MouseEvent e) { }

				@Override
				public void mouseExited(MouseEvent e) { }

				@Override
				public void mouseEntered(MouseEvent e) { }

				@Override
				public void mouseClicked(MouseEvent e) {
					if (cell_click == CELL_CLICK_SET_WOODS) {
						vel_factor = 0.5f;
						setBackground(Color.decode("#D2691E"));
						repaint();
					} else if (cell_click == CELL_CLICK_SET_BLOCKED) {
						panel.remove(Cell.this);
						setBlocked(x, y);
						panel.repaint();
					}
				}
			});
		}

		/**
		 * Prepares the cell to be drawn by scaling the size to the appropriate width and height,
		 * moving the cell and drawing the border.
		 * If the cell is marked as slow (only half the speed) then set the background to brown.
		 * @param width
		 * @param height
		 */
		void drawOnCanvas(int width, int height) {
			// Scale width, height and location
			setSize(width, height);
			setLocation(width * x, height * y);

			// Set background to brown if cell is part of woods
			if (vel_factor == 0.5f) setBackground(Color.decode("#D2691E"));

			// Draw border in grid
			setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
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

	Cell[][] mCells;
	Cell startCell;
	Cell endCell;

	PriorityQueue<Cell> open;
	boolean[][] closed;
	boolean[][] blocked;

	int startX, startY;

	int endX = -1, endY = -1;

	long t_start, t_end;

	/**
	 * 
	 * @param currentCell The cell in the grid currently being checked
	 * @param t One of the neighboring cells of @currentCell
	 * @param cost The cost of the new cell
	 */
	public void checkAndUpdateCost(Cell currentCell, Cell t, int cost) {
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

	boolean finished = false;

	/**
	 * The last method to be called on the algorithm object.
	 * Sets the KeyListener for the frame to start the algorithm once ENTER is pressed.
	 * @return The new algorithm object.
	 * @throws IllegalArgumentException if no start or final cell has been set.
	 */
	public A_Star prepare() throws IllegalArgumentException {
		if (endCell == null)
			throw new IllegalArgumentException("You have to set a final end cell!", new Throwable("No end cell set"));
		if (startCell == null)
			throw new IllegalArgumentException("You have to set a starting cell!", new Throwable("No starting cell set"));

		open.add(startCell);

		frame.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {}
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER && !finished) {
					System.out.println("Finding path...");
					if (enter_pressed == ENTER_MAKE_ONE_STEP) {
						whileLoop();
					} else if (enter_pressed == ENTER_COMPLETE_AUTOMATICALLY) {
						long start = System.currentTimeMillis();
						while (!finished)
							whileLoop();
						System.out.println("Algorithm itself took " + (System.currentTimeMillis() - start) + " Miliseconds");
					}
				}
			}
		});
		return this;
	}

	/**
	 * Called once the algorithm has finished.
	 * Prints the path to the console and draws the tiles on the panel.
	 * @param success
	 */
	private void finished(boolean success) {
		this.finished = true;
		
		if (success) {
			System.out.println(System.lineSeparator() + "Success!");

			// Print the path cells to console
			System.out.println("Path: ");
			Cell current = mCells[endX][endY];
			System.out.println(current);
			mCells[current.x][current.y].setBackground(Color.CYAN);
			
			while (current.parent != null) {
				// Color the path cyan and mark as part of that
				mCells[current.parent.x][current.parent.y].setBackground(Color.CYAN);
				
				// Print current path node to console
				System.out.print(" -> " + current.parent.toString());
				
				// Go through all nodes that are part of the path
				current = current.parent;
			}
		} else {
			System.out.println(System.lineSeparator() + "No possible path. I am so sorry.");
		}
	}

	/**
	 * The algorithm loop
	 */
	private void whileLoop() {
		// Get node with lowest f-cost
		Cell current = open.poll();
		
		// If no nodes are in the open set there is no possible path
		if (current == null) {
			finished(false);
			return;
		}

		// If the current node is the end node we are finished
		if (current.x == endX && current.y == endY)
			finished(true);

		// Move the current node from the open set to the closed set since we are checking it now
		closed[current.x][current.y] = true;
		
		// Mark the current node red since it is now in the closed set
		mCells[current.x][current.y].setBackground(Color.RED);

		// The current cell's neighbor
		// We now check every neighbor of @current
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
	 * The constructor for a new algorithm object.
	 * Sets up the window, main panel.
	 * Initializes the algorithm arrays.
	 * initializes the scaled cell width and cell height
	 */
	public A_Star(int cells_horizontal, int cells_vertical) {
		long t_start = System.currentTimeMillis();

		// Initialize the cell grid, the closed set, the blocked set and the open set.
		mCells = new Cell[cells_horizontal][cells_vertical];
		closed = new boolean[cells_horizontal][cells_vertical];
		blocked = new boolean[cells_horizontal][cells_vertical];
		open = new PriorityQueue<Cell>(comparator);

		// Setup the window
		frame = new JFrame("A* Pathfinding algorithm");
		frame.setLayout(null);
		frame.setSize(1000, 1000);
		frame.setLocation(400, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		
		// The cell width and height. Used for scaling the cells onto the main panel-
		cell_width = (frame.getWidth()) / cells_horizontal;
		cell_height = (frame.getHeight()) / cells_vertical;

		// The main panel
		panel = new JPanel();
		panel.setLayout(null);
		panel.setSize(frame.getWidth(), frame.getHeight());
		panel.setBackground(Color.BLACK);

		// Add panel to frame and request focus to work with the ENTER keys
		frame.add(panel);
		frame.requestFocus();

		// Debugging
		System.out.println("Setting up UI took " + (System.currentTimeMillis() - t_start) + " Miliseconds"	);
		frame.setSize(frame.getWidth() + 35, frame.getHeight() + 85);
	}

	/**
	 * Scales the cells to the appropriate width and height
	 * and adds them to the main panel.
	 * Paints the start node and end node blue.
	 * @return
	 */
	private A_Star scaleAndDrawGrid() {
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
		
		return this;
	}

	/**
	 * Sets the start node for this algorithm object.
	 * @param ind_x The x coordinate in the grid
	 * @param ind_y The y coordinate in the grid
	 * @return The new algorithm object
	 */
	public A_Star setStartCell(int ind_x, int ind_y) {
		startCell = new Cell(ind_x, ind_y);
		mCells[ind_x][ind_y] = new Cell(ind_x, ind_y);

		startX = ind_x;
		startY = ind_y;

		return this;
	}

	/**
	 * Sets the end node for this algorithm object.
	 * @param ind_x The x coordinate in the grid
	 * @param ind_y The y coordinate in the grid
	 * @return The new algorithm object
	 */
	public A_Star setEndCell(int ind_x, int ind_y) {
		endCell = new Cell(ind_x, ind_y);
		mCells[ind_x][ind_y] = new Cell(ind_x, ind_y);
		endX = ind_x;
		endY = ind_y;

		return this;
	}

	/**
	 * Initializes the grid and sets the heuristic cost for every node in it.
	 * @return The new algorithm object
	 */
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

	/**
	 * Marks a node/cell as blocked.
	 * Removes the cell from the grid and marks its spot in the closed set as true.
	 * @param x The x coordinate in the grid
	 * @param y The y coordinate in the grid
	 * @return
	 */
	public A_Star setBlocked(int x, int y) {
		mCells[x][y] = null;
		closed[x][y] = true;
		return this;
	}
	
	/**
	 * Specifies which action to to when ENTER is pressed
	 * Two options are available:
	 * 		@ENTER_MAKE_ONE_STEP sets the algorithm to make one step each time the ENTER key is pressed
	 * 		@ENTER_COMPLETE_AUTOMATICALLY sets the algorithm to start once the ENTER key is pressed and then completes on its own.
	 * @param mode The mode what happens when ENTER is pressed
	 * @return
	 */
	public A_Star setEnterMode(int mode) {
		this.enter_pressed = mode;
		return this;
	}
	
	/**
	 * Specifies which action to do when a cell is clicked on
	 * Two options are available:
	 * 		@CELL_CLICK_SET_WOODS make the cell "woods". Simulates moving through at only half the speed.
	 * 		@CELL_CLICK_SET_BLOCKED marks the cell as blocked and makes it an obstacle
	 * @param mode The mode what happens when a cell is clicked on
	 * @return
	 */
	public A_Star setCellClickMode(int mode) {
		this.cell_click = mode;
		return this;
	}


	/**
	 *  Inserts random blocked and "slow" cells into the grid
	 */
	public A_Star insertRandomObstacles() {
		for (int i = 0; i < 500; i++) {
			int x = (int) (Math.random() * CELL_COUNT);
			int y = (int) (Math.random() * CELL_COUNT);

			if (x == startX && y == startY) continue;
			if (x == endX && y == endY) continue;

			if (Math.random() < 0.4)
				setBlocked(x, y);
			else {
				if (!closed[x][y]) 
					mCells[x][y].vel_factor = 0.5f;
			}
		}
		return this;
	}

	/**
	 * Creates a new algorithm object and starts the algorithm.
	 * @param args
	 */
	public static void main(String[] args) {
		A_Star algorithm = new A_Star(CELL_COUNT, CELL_COUNT)
				.setEnterMode(ENTER_MAKE_ONE_STEP)
				.setCellClickMode(CELL_CLICK_SET_WOODS)
				.setStartCell(0, 0)
				.setEndCell(CELL_COUNT - 1, CELL_COUNT - 1)
				.setupGrid();
		
		algorithm.insertRandomObstacles().scaleAndDrawGrid();

		algorithm.t_start = System.currentTimeMillis();
		algorithm.prepare();
	}
}
