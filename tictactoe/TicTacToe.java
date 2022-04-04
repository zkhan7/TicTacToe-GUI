import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.applet.Applet;
import java.applet.AudioClip;
import java.net.URL;
/**
 * A class modelling a tic-tac-toe (noughts and crosses, Xs and Os) game.
 * "button-pop" Borrowed from: https://www.youtube.com/watch?v=dZeKYWmSZkc
 * "button-highlight" Borrowed from: https://www.youtube.com/watch?v=3hPVv6boahw
 *
 * @author Zakariya Khan (101186641)
 * @version March 25, 2022
 * @author Lynn Marshall
 * @version November 8, 2012
 */

public class TicTacToe extends MouseAdapter implements ActionListener
{
   public static final String PLAYER_X = "X"; // player using "X"
   public static final String PLAYER_O = "O"; // player using "O"
   public static final String EMPTY = " ";  // empty cell
   public static final String TIE = "T"; // game ended in a tie
 
   private String player;   // current player (PLAYER_X or PLAYER_O)
   
   private String firstPlayer;

   private String winner;   // winner: PLAYER_X, PLAYER_O, TIE, EMPTY = in progress
   
   private int statistics[];

   private int numFreeSquares; // number of squares still free
   
   private JButton board[][]; // 3x3 array of JButtons representing the board

   private JLabel gameState; // Area to show the current players turn
   
   private JLabel stats; // Area to show statistics of previous games
   
   /* The reset menu item */
   private JMenuItem newItem;
   
   /* The quit menu item */
   private JMenuItem quitItem;
   
   /* The reset statistics menu item */
   private JMenuItem resetStatsItem;
   
   /* The change first player menu item */
   private JMenuItem changePlayerItem;
   
   /* Blank Image */
   private static ImageIcon blankImage = new ImageIcon("blank.jpg");
   
   /* Image for player x */
   private static ImageIcon xIcon = new ImageIcon("x-icon.jpg");
   
   /* Image for player o*/
   private static ImageIcon oIcon = new ImageIcon("o-icon.jpg");
   
   /* Current players icon*/
   private static ImageIcon playerImage;
   
   private static ImageIcon firstPlayerImage; 
   
   /* The noise made when a button from the button panel is hovered over*/
   private AudioClip highlightNoise;
   /** 
    * Constructs a new Tic-Tac-Toe board.
    */
   public TicTacToe()
   { 
      JFrame frame = new JFrame("Tic-Tac-Toe"); // initialize JFrame called "TicTacToe"
      Container contentPane = frame.getContentPane();
      contentPane.setLayout(new BorderLayout());
      
      JMenuBar menubar = new JMenuBar();
      frame.setJMenuBar(menubar); // add menu bar to our frame

      JMenu fileMenu = new JMenu("Game"); // create a menu
      menubar.add(fileMenu); // and add to our menu bar

      newItem = new JMenuItem("New"); // create a menu item called "New"
      fileMenu.add(newItem); // and add to our menu

      quitItem = new JMenuItem("Quit"); // create a menu item called "Quit"
      fileMenu.add(quitItem); // and add to our menu
      
      resetStatsItem = new JMenuItem("Reset Statistics"); // create a menu item called "Reset Statistics"
      fileMenu.add(resetStatsItem); // and add to our menu
      
      changePlayerItem = new JMenuItem("Change First Player"); // create a menu item called "Change First Player"
      fileMenu.add(changePlayerItem); // and add to our menu
      
      
      // shortcuts (e.g. Ctrl-R and Ctrl-Q)
      final int SHORTCUT_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(); // to save typing
      newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, SHORTCUT_MASK));
      quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, SHORTCUT_MASK));
      resetStatsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, SHORTCUT_MASK));
      
       // listen for menu selections
      newItem.addActionListener(this);
      quitItem.addActionListener(new ActionListener() // create an anonymous inner class
        { // start of anonymous subclass of ActionListener
          // this allows us to put the code for this action here  
            public void actionPerformed(ActionEvent event)
            {
                System.exit(0); // quit
            }
        } // end of anonymous subclass
      ); // end of addActionListener parameter list and statement
      resetStatsItem.addActionListener(this);
      changePlayerItem.addActionListener(this);
      JPanel buttonPanel = new JPanel();
      buttonPanel.setLayout(new GridLayout(3,3));
      board = new JButton[3][3];
      /* Add buttons to grid*/
     for(int i = 0; i < 3; i++){
         for(int j = 0; j < 3; j++){
             board[i][j] = new JButton();
             board[i][j].setPreferredSize(new Dimension(100, 100));
             buttonPanel.add(board[i][j]); // add button to grid
             board[i][j].addActionListener(this); // register ActionListeners
             board[i][j].addMouseListener(this); // register Mouselisteners for audio
         }
     }
      
      contentPane.add(buttonPanel,BorderLayout.CENTER); // add grid to content pane
      
      gameState = new JLabel();  
      gameState.setFont(new Font(null, Font.PLAIN, 12)); 
      contentPane.add(gameState, BorderLayout.SOUTH); // south side
      gameState.setText("Game in Progress: X's Turn");
      
      statistics = new int[]{0,0,0};
      stats = new JLabel();
      stats.setFont(new Font(null, Font.PLAIN, 12));
      contentPane.add(stats, BorderLayout.NORTH);
      stats.setText("Statistics:    X Wins: 0     O Wins: 0     Ties: 0");
      
      firstPlayer = PLAYER_X; 
      firstPlayerImage = xIcon;
      clearBoard(); // initialize fields
      
      // finish setting up the frame
      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE); // exit when we hit the "X"
      frame.pack(); // pack everthing into our frame
      frame.setResizable(false); // we can resize it
      frame.setVisible(true); // it's visible
   }

   /**
    * Sets everything up for a new game.  Marks all squares in the Tic Tac Toe board as empty,
    * and indicates no winner yet, 9 free squares and the current player is player X.
    */
   private void clearBoard()
   {
       for(int i = 0 ; i < 3 ; i++){
         for(int j = 0 ; j < 3; j++){
             board[i][j].setIcon(blankImage);
             board[i][j].setDisabledIcon(blankImage);
             board[i][j].setText("");
             board[i][j].setEnabled(true);
         }
     }
      winner = EMPTY;
      numFreeSquares = 9;
      player = firstPlayer;  
      playerImage = firstPlayerImage;
      updateState();
   }

   /**
    * Returns true if filling the given square gives us a winner, and false
    * otherwise.
    *
    * @param int row of square just set
    * @param int col of square just set
    * 
    * @return true if we have a winner, false otherwise
    */
   private boolean haveWinner(int row, int col) 
   {
       // unless at least 5 squares have been filled, we don't need to go any further
       // (the earliest we can have a winner is after player X's 3rd move).

       if (numFreeSquares>4) return false;

       // Note: We don't need to check all rows, columns, and diagonals, only those
       // that contain the latest filled square.  We know that we have a winner 
       // if all 3 squares are the same, as they can't all be blank (as the latest
       // filled square is one of them).

       // check row "row"
       if ( board[row][0].getText().equals(board[row][1].getText()) &&
            board[row][0].getText().equals(board[row][2].getText()) ) return true;
       
       // check column "col"
       if ( board[0][col].getText().equals(board[1][col].getText()) &&
            board[0][col].getText().equals(board[2][col].getText()) ) return true;

       // if row=col check one diagonal
       if (row==col)
          if ( board[0][0].getText().equals(board[1][1].getText()) &&
               board[0][0].getText().equals(board[2][2].getText()) ) return true;

       // if row=2-col check other diagonal
       if (row==2-col)
          if ( board[0][2].getText().equals(board[1][1].getText()) &&
               board[0][2].getText().equals(board[2][0].getText()) ) return true;

       // no winner yet
       return false;
   }
  
   /**
    * Returns a string representing the current state of the game.  This should look like
    * a regular tic tac toe board, and be followed by a message if the game is over that says
    * who won (or indicates a tie).
    *
    * @return String representing the tic tac toe game state
    */
    public void updateState() 
    {
        if(winner.equals(PLAYER_X)){
            gameState.setText("Game Over: " + winner + " wins");
            statistics[0] += 1;
        } else if (winner.equals(PLAYER_O)){
            gameState.setText("Game Over: " + winner + " wins");
            statistics[1] += 1;
        } else if(winner.equals(TIE)){
            gameState.setText("Game Over: The game is a tie!");
            statistics[2] += 1;
        } else{
            gameState.setText("Game in Progress: " + player + "'s Turn");
        }
        stats.setText("Statistics:    X Wins: " + statistics[0] +"   O Wins: " + statistics[1] +"    Ties: "+ statistics[2]);
    }
    
   /** This action listener is called when the user clicks on 
    * any of the GUI's buttons. 
    */
    public void actionPerformed(ActionEvent e)
    {
        Object o = e.getSource(); // get the action 
        
        // see if it's a JButton
        if (o instanceof JButton) {
    
            JButton button = (JButton)o;
            int row = 0;
            int col = 0;
            
            URL urlClick = TicTacToe.class.getResource("button-pop.wav"); // *pop*
            highlightNoise = Applet.newAudioClip(urlClick);
            highlightNoise.play();

            if (button == board[0][0]) { 
                board[0][0].setDisabledIcon(playerImage);
                board[0][0].setText(player);
                board[0][0].setEnabled(false);
            } else if (button == board[0][1]) { 
                board[0][1].setDisabledIcon(playerImage);
                board[0][1].setText(player);
                board[0][1].setEnabled(false);
                row = 0;
                col = 1;
            }else if (button == board[0][2]) { 
                board[0][2].setDisabledIcon(playerImage);
                board[0][2].setText(player);
                board[0][2].setEnabled(false);
                row = 0;
                col = 2;
            }else if (button == board[1][0]) { 
                board[1][0].setDisabledIcon(playerImage);
                board[1][0].setText(player);
                board[1][0].setEnabled(false);
                row = 1;
                col = 0;
            }else if (button == board[1][1]) { 
                board[1][1].setDisabledIcon(playerImage);
                board[1][1].setText(player);
                board[1][1].setEnabled(false);
                row = 1;
                col = 1;
            }else if (button == board[1][2]) { 
                board[1][2].setDisabledIcon(playerImage);
                board[1][2].setText(player);
                board[1][2].setEnabled(false);
                row = 1;
                col = 2;
            }else if (button == board[2][0]) { 
                board[2][0].setDisabledIcon(playerImage);
                board[2][0].setText(player);
                board[2][0].setEnabled(false);
                row = 2;
                col = 0;
            }else if (button == board[2][1]) { 
                board[2][1].setDisabledIcon(playerImage);
                board[2][1].setText(player);
                board[2][1].setEnabled(false);
                row = 2;
                col = 1;
            }else if (button == board[2][2]) { 
                board[2][2].setDisabledIcon(playerImage);
                board[2][2].setText(player);
                board[2][2].setEnabled(false);
                row = 2;
                col = 2;
            }
            
             numFreeSquares--; // decrement number of free square
             
            // see if the game is over
            if (haveWinner(row,col)){
                winner = player; // must be the player who just went
                for(int i = 0 ; i < 3 ; i++){
                     for(int j = 0 ; j < 3; j++){
                         if (board[i][j].getText().equals(EMPTY)){
                             board[i][j].setDisabledIcon(blankImage);
                         }
                         board[i][j].setEnabled(false);
                     }
                 }
            }
                
            else if (numFreeSquares==0){
                winner = TIE;  // board is full so it's a tie// print current board
                for(int i = 0 ; i < 3 ; i++){
                     for(int j = 0 ; j < 3; j++){
                        if (board[i][j].getText().equals(EMPTY)){
                             board[i][j].setDisabledIcon(blankImage);
                            }
                         board[i][j].setEnabled(false);
                     }
                 }
            }
             // change to other player (this won't do anything if game has ended)
            if (player==PLAYER_X){
                player=PLAYER_O;
                playerImage = oIcon;
            }
            else {
                player=PLAYER_X;
                playerImage = xIcon;
            }
            updateState();
        } else { // it's a JMenuItem
            
            JMenuItem item = (JMenuItem)o;
            
            if (item == newItem) { // reset
                clearBoard();
            } else if (item == resetStatsItem) { // reset statistics
                statistics[0] = 0;
                statistics[1] = 0;
                statistics[2] = 0;
                stats.setText("Statistics:    X Wins: " + statistics[0] +"   O Wins: " + statistics[1] +"    Ties: "+ statistics[2]);
            } else if (item == changePlayerItem) { // change first player and start next round
                 if (firstPlayer==PLAYER_X){
                    firstPlayer=PLAYER_O;
                    firstPlayerImage = oIcon;
                }
                else {
                    firstPlayer=PLAYER_X;
                    firstPlayerImage = xIcon;
                }
                clearBoard();
            }
        }
    }
    
    /**
    * Detects when the mouse enters the component.  We are only "listening" to the
    * JMenu.  We highlight the menu name when the mouse goes into that component.
    * 
    * @param e The mouse event triggered when the mouse was moved into the component
    */
   public void mouseEntered(MouseEvent e) {
        Object o = e.getSource();
        if (o instanceof JButton){
            JButton btn = (JButton) o;
            if (!(btn.getText().equals(PLAYER_X) || btn.getText().equals(PLAYER_O) || btn.getText().equals(EMPTY)) && winner.equals(EMPTY)){
                URL urlClick = TicTacToe.class.getResource("button-highlight.wav"); // *tap*
                highlightNoise = Applet.newAudioClip(urlClick);
                highlightNoise.play();
            }   
        } else {
            JMenu item = (JMenu) o;
            item.setSelected(true); // highlight the menu name
        }
   }

   /**
    * Detects when the mouse exits the component.  We are only "listening" to the
    * JMenu.  We stop highlighting the menu name when the mouse exits  that component.
    * 
    * @param e The mouse event triggered when the mouse was moved out of the component
    */
   public void mouseExited(MouseEvent e) {
        Object o = e.getSource();
        if (o instanceof JButton){
            highlightNoise.stop();
        } else {
            JMenu item = (JMenu) o;
            item.setSelected(false); // highlight the menu name
        }
   }
}

