/*
 * Protocol.java
 *
 * @author Kajal Nagrani, Winston Chang, Alex Kramer, Caleb Maynard, Aiden Lin
 * @since 2017-12-08
 * @version 1.0
 */

/**
 * A list of protocol codes to
 * communicate with the srever
 */
class Protocol {
	public static final int NAME = 0;
	public static final int BOARD_NUMBERS = 1;
	public static final int JOIN_LOBBY = 2;

	public static final int MESSAGE = 4;

	public static final int SCORES = 5;
	public static final int WON = 6;
	public static final int MY_TURN = 7;

	public static final int MOVE_PAIR = 10;
	public static final int SHOW_PAIR = 11;
	public static final int HIDE_PAIR = 12;
	public static final int STATE = 13;

	public static final int ERROR = 100;
}