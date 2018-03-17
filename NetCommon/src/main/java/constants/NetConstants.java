package constants;

public class NetConstants {
	public static final String PASSWORD = "admin";
	public static final String USERNAME = "postgres";
	private static final String HOST = "localhost";
	private static final String DB_PORT = "5432";
	private static final String DB_NAME = "NetStorage";

	public static final String URL = "jdbc:postgresql://"+
			HOST +":"+
			DB_PORT +"/"+
			DB_NAME;

	public static final int SERVER_PORT = 8189;

	public static final String CLIENT_FOLDER_PATH = "NetClient/clientFolder/";
	public static final String SERVER_FOLDER_PATH = "NetServer/serverFolder/";

	public static final int SIZE_OF_PART_FILE = 1048576;

	public static final int MAX_FILES = 3;
}
