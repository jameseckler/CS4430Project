import java.sql.*;
import java.util.Scanner;
import java.util.Random;
import java.util.List;
import java.io.Console;

public class dnd {
	public static int accountID = 0;
	public static int campaignID = 0;
	public static String[] terms1 = {"str", "con", "dex", "wis", "int", "cha"};
	public static String[] terms2 = {"strength", "constitution", "dexterity", "wisdom", "intelligence", "charisma"};
	public static void main(String args[]) {
		Boolean inputBoolean;
		Boolean exitBool = false;
		Scanner scanner = new Scanner(System.in);
		String input = "";
		int admin = 0;

		
		while (!exitBool && accountID >= 0) {
			try {
				Class.forName("com.mysql.jdbc.Driver");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				accountID = -1;

			}
			while (accountID >= 0) {
				try {
					Connection mycon = DriverManager.getConnection("jdbc:mysql://localhost/dnd?useSSL=false", "root", "your_password");
			
		
					login();
					Statement stat = mycon.createStatement();
					ResultSet rs = stat.executeQuery("SELECT admin FROM account WHERE accountID = " + accountID);
					while (rs.next())
						admin = rs.getInt(1);
					if (admin > 0) {
						inputBoolean = false;
						while (!inputBoolean) {
							System.out.println("Go into Admin View? (y/n)");
							input = scanner.nextLine().toLowerCase();
					
							switch (input) {
								case "y":
									admin(admin);
									inputBoolean = true;
									break;
								case "n":
									campaignView();
									inputBoolean = true;
									break;
								default:
									System.out.println("--INVALID INPUT--");
									break;
							}
						}
					}
					else if (admin == 0 || input == "n")
						campaignView();
					exitBool = true;
					mycon.close();
				}
				catch (SQLException e) {
					e.printStackTrace();
					accountID = -1;
				}
			}
		}
	}

	public static void login() {
		String[] help = {"\n",
			"CLOSE - Exit the program",
			"CHANGEPW - Change password",
			"FORGOTPW - Forgot password",
			"FORGOTUSER - Forgot username",
			"REGISTER - Create new account",
			"\n"};
		Boolean inputBool = false;
		Boolean existingAccount = false;
		Boolean exitBool = false;
		Scanner scanner = new Scanner(System.in);
		String username = "";
		ResultSet rs;		
		Statement stat;

		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		try {
			Connection mycon = DriverManager.getConnection("jdbc:mysql://localhost/dnd?useSSL=false", "root", "your_password");
			stat = mycon.createStatement();

			while (!existingAccount && !exitBool) {
				inputBool = false;
				while (!inputBool) {
					System.out.println("\nUsername: (Or HELP for other options)");
					String input = scanner.nextLine();
					switch (input) {
						case "HELP":
							for (int n = 0; n < help.length; n++)
								System.out.println(help[n]);
							break;
						case "CLOSE":
							exitBool = true;
							inputBool = true;
							accountID = -1;
							break;
						case "CHANGEPW":
							changepw();
							break;
						case "FORGOTPW":
							forgotPass();
							break;
						case "FORGOTUSER":
							forgotUser();
							break;
						case "REGISTER":
							register();
							break;
						default:
							username = input;
							rs = stat.executeQuery("SELECT * FROM account WHERE username = '" + username + "'");
							if (rs.next())						
								inputBool = true;
							else
								System.out.println("--NO ACCOUNT BY THAT NAME/INVALID COMMAND--");
							break;
					}
				}
				inputBool = false;
				while (!exitBool && !inputBool) {
					System.out.println("Password:");
					Console console = System.console();
					char[] pass = console.readPassword();
					inputBool = true;
					rs = stat.executeQuery("SELECT accountID FROM account WHERE username = '" + username + "' AND password = '" + convertPass(pass) + "'");
					if (rs.next()) {
						accountID = rs.getInt(1);
						existingAccount = true;
					} else {
						System.out.println("--INCORRECT PASSWORD--");
						inputBool = true;
					}
				}

			}
			mycon.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
				
	}

	private static String convertPass(char[] pass) {
		String ret = "";
		for (int n = 0; n < pass.length; n++)
			ret += pass[n];
		return ret;
	}

	//Gets user email, prompts for old pw, then asks for new pw and repeats for confirmation
	private static void changepw() {
		String email;
		char[] oldPass;
		char[] newPass;
		Boolean exitBool = false;
		Scanner scanner = new Scanner(System.in);
		Console console = System.console();

		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		try {
			Connection mycon = DriverManager.getConnection("jdbc:mysql://localhost/dnd?useSSL=false", "root", "your_password");
			Statement stat = mycon.createStatement();

			System.out.println("Input email:");
			email = scanner.nextLine();
			ResultSet rs = stat.executeQuery("SELECT * FROM account WHERE email = '" + email + "'");
			if (rs.next()) {
				System.out.print("Enter old password:");
				oldPass = console.readPassword();
				rs = stat.executeQuery("SELECT * FROM account WHERE email = '" + email + "' AND password = '" + convertPass(oldPass) + "'");
				if (rs.next()) {
					Boolean inputBool = false;
					while (!inputBool) {
						System.out.print("Enter new password:");
						newPass = console.readPassword();
						System.out.print("Re-enter password:");
						char[] array = console.readPassword();
						if (convertPass(newPass).equals(convertPass(array))) {
							stat.executeUpdate("UPDATE account SET password = '" + convertPass(newPass) + "' WHERE email = '" + email + "'");
							System.out.println("\n--PASSWORD CHANGED--\n");
							inputBool = true;
						}
						else
							System.out.println("\n--PASSWORDS DO NOT MATCH--\n");
					}
				}
				else {
					System.out.println("\n--INCORRECT PASSWORD--\n");
					exitBool = true;
				}
			}
			else 
				System.out.println("\n--INVALID EMAIL--\n");
		
			
			
			mycon.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

	//Retrieve username and password via hint and email
	private static void forgotUser() {
		String email;
		String answer;
		int hintID = 0;
		Boolean exitBool = false;
		Scanner scanner = new Scanner(System.in);

		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		try {
			Connection mycon = DriverManager.getConnection("jdbc:mysql://localhost/dnd?useSSL=false", "root", "your_password");
			Statement stat = mycon.createStatement();

			System.out.println("Input email:");
			email = scanner.nextLine();
			ResultSet rs = stat.executeQuery("SELECT hintID FROM account WHERE email = '" + email + "'");
			if (rs.next()) {
				rs = stat.executeQuery("SELECT username, password, hintText, hintAnswer FROM account, hint WHERE account.hintID = hint.hintID AND email = '" + email + "'");
				rs.next();
				System.out.println("Hint: " + rs.getString(3) + "\nAnswer:");
				answer = scanner.nextLine();
				if (answer.equals(rs.getString(4))) {
					System.out.println("Username: " + rs.getString(1) + "\n");
				}
				else
					System.out.println("\n--INCORRECT ANSWER--\n");
			}
			else
				System.out.println("\n--INVALID EMAIL--\n");
			mycon.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

	private static void forgotPass() {
		String email;
		String answer;
		int hintID = 0;
		Boolean exitBool = false;
		Scanner scanner = new Scanner(System.in);
		Console console = System.console();

		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		try {
			Connection mycon = DriverManager.getConnection("jdbc:mysql://localhost/dnd?useSSL=false", "root", "your_password");
			Statement stat = mycon.createStatement();

			System.out.println("Input email:");
			email = scanner.nextLine();
			ResultSet rs = stat.executeQuery("SELECT hintID FROM account WHERE email = '" + email + "'");
			if (rs.next()) {
				rs = stat.executeQuery("SELECT username, password, hintText, hintAnswer FROM account, hint WHERE account.hintID = hint.hintID AND email = '" + email + "'");
				rs.next();
				System.out.println("Hint: " + rs.getString(3) + "\nAnswer:");
				answer = scanner.nextLine();
				if (answer.equals(rs.getString(4))) {
					Boolean inputBool = false;
					while (!inputBool) {
						System.out.print("Enter new password:");
						char[] newPass = console.readPassword();
						System.out.print("Re-enter new password:");
						char[] confirm = console.readPassword();
						if (convertPass(newPass).equals(convertPass(confirm))) {
							stat.executeUpdate("UPDATE account SET password = '" + convertPass(newPass) + "' WHERE email = '" + email + "'");
							inputBool = true;
							System.out.println("\n--PASSWORD CHANGED--\n");
						}
						else
							System.out.println("\n--PASSWORDS DO NOT MATCH--\n");
					}

				}
				else
					System.out.println("\n--INCORRECT ANSWER--\n");
			}
			else
				System.out.println("\n--INVALID EMAIL--\n");
			mycon.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

	//Add person to accounts database
	private static void register() {
		Scanner scanner = new Scanner(System.in);
		String email = null;
		String username = null;
		String password = "";
		int hintID = 0;
		String answer;
		Boolean inputBool = false;
		Console console = System.console();

		
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		try {
			Connection mycon = DriverManager.getConnection("jdbc:mysql://localhost/dnd?useSSL=false", "root", "your_password");
			Statement stat = mycon.createStatement();

			while(!inputBool) {
				System.out.println("Enter email:");
				email = scanner.nextLine();
				ResultSet rs = stat.executeQuery("SELECT email FROM account WHERE email = '" + email + "'");
				if (rs.next())
					System.out.println("\n--EMAIL ALREADY IN USE--\n");
				else
					inputBool = true;
			}

			inputBool = false;
			while (!inputBool) {
				System.out.println("Enter username:");
				username = scanner.nextLine();
				if (username.equals("CHANGEPW") || username.equals("CLOSE") || username.equals("FORGOTUSER") || username.equals("FORGOTPASS") || username.equals("HELP")) 
					System.out.println("\n--ILLEGAL USERNAME--\n");
				else {
					ResultSet rs = stat.executeQuery("SELECT username FROM account WHERE username = '" + username + "'");
					if (rs.next())
						System.out.println("\n--USERNAME ALREADY IN USE--\n");
					else
						inputBool = true;
				}
			}
			inputBool = false;
			while (!inputBool) {
				System.out.println("Enter password:");
				password = convertPass(console.readPassword());
				System.out.println("Confirm password:");
				if (convertPass(console.readPassword()).equals(password))
					inputBool = true;
				else
					System.out.println("\n--PASSWORDS DO NOT MATCH--\n");
			}
			
			ResultSet rs = stat.executeQuery("SELECT * FROM hint");
			while (rs.next()) 
				System.out.println(rs.getInt(1) + ". " + rs.getString(2));
			inputBool = false;
			while (!inputBool) {
				System.out.println("Enter number for desired account hint:");
				try {
					hintID = Integer.parseInt(scanner.nextLine());
					rs = stat.executeQuery("SELECT * FROM hint WHERE hintID = " + hintID);
					if (rs.next())
						inputBool = true;
					else
						System.out.println("\n--INVALID HINTID--\n");
				} catch (Exception e) {
					System.out.println("\n--INVALID HINTID--\n");
				}		
			}
	
			System.out.println(rs.getString(2) + "\nAnswer:");
			answer = scanner.nextLine();
			
			stat.executeUpdate("INSERT INTO `account` (`email`, `username`, `password`, `hintID`, `hintAnswer`, `accountID`, `admin`) VALUES ('" + email + "', '" + username + "', '" + password + "', '" + hintID + "', '" + answer + "', NULL, '0')");	
			System.out.println("\n--ADDED TO ACCOUNTS--\n");
			mycon.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void admin(int level) {
		String[]superadminHelp = { "\n",
			"LEVEL [accountID] - Change user's priviliges" };
		String[] help = { "\n",
			"ADD [database] - Add tuple to specified database",
			"ADD HOMEBREW - Add homebrew item to universal list",
			"CHANGE [database] - Change value in specified database",
			"DELETE [database] - Delete tuple from specified database",
			"VIEW [database] - View tuples of specified database",
			"VIEW HOMEBREW - View all homebrew monsters or items",
			"LIST - View list of databases",
			"LOGOUT - Return to login screen",
			"CLOSE - Exit program",
			"\n" };
		String[] list = { "\n",
			"ACCOUNT", "CAMPAIGN", "CAMPAIGNPLAYERS", "HINT",
			"INVENTORY", "ITEMS", "MODIFIERS", "MONSTERS", "NPC", "PC", "SKILLS", "\n" };
		Scanner scanner = new Scanner(System.in);
		Boolean exitBool = false;
		
		while (!exitBool) {
			System.out.println("Enter Command:");
			String[] input = scanner.nextLine().split(" ");
		
			if (input.length > 1) {
				input[1] = input[1].toLowerCase();
				if (input[1] == "campaignplayers")
					input[1] = "campainPlayers";	
			}	
	
			switch (input[0]) {
				case "HELP":
					if (level >= 2) {
						for (int n = 0; n < superadminHelp.length; n++)
							System.out.println(superadminHelp[n]);
					}
					for (int n = 0; n < help.length; n++)
						System.out.println(help[n]);
					break;
				case "LEVEL":
					if (level >= 2) {
						try {
							if (input.length > 1)
								appoint(Integer.valueOf(input[1]));
							else
								System.out.println("\n--DATABASE MISSING FROM COMMAND--\n");
						} catch (Exception e) {
							System.out.println("\n--INVALID ACCOUNT ID--\n");
						}
					}
					else
						System.out.println("\n--ONLY SUPERADMINS MAY CHANGE USER LEVEL--\n");
					break;
				case "ADD":
					if (input.length > 1) {
						if (input[1].equals("homebrew"))
							addHomebrew();
						else
							adminAdd(input[1]);
					}
					else
						System.out.println("\n--INVALID ADD COMMAND--\n");
					break;
				case "CHANGE":
					if (input.length > 1)
						adminChange(input[1]);
					else
						System.out.println("\n--DATABASE MISSING FROM COMMAND--\n");
					break;
				case "DELETE":
					if (input.length > 1)
						adminDelete(input[1]);
					else
						System.out.println("\n--DATABASE MISSING FROM COMMAND--\n");
					break;
				case "VIEW":
					if (input.length > 1) {
						if (input[1].equals("homebrew"))
							viewHomebrew();
						else
							adminView(input[1]);
					}
					else
						System.out.println("\n--INVALID VIEW COMMAND--\n");
					break;
				case "LIST":
					for (int n = 0; n < list.length; n++)
						System.out.println(list[n]);
					break;
				case "CLOSE":
					exitBool = true;
					accountID = -1;
					break;
				case "LOGOUT":
					exitBool = true;
					accountID = 0;
					break;
				default:
					System.out.println("\n--INVALID COMMAND--\n");
					break;
			}
		}
	}

	private static void viewHomebrew() {
		Scanner scanner = new Scanner(System.in);
		System.out.println("View \"MONSTERS\" or \"ITEMS\"? (\"BACK\" to go back to admin menu)");
		String input = scanner.nextLine().toLowerCase();
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		if (input.equals("monsters") || input.equals("items")) {
			try {
				Connection mycon = DriverManager.getConnection("jdbc:mysql://localhost/dnd?useSSL=false", "root", "your_password");
				Statement stat = mycon.createStatement();
				ResultSet rs = stat.executeQuery("SELECT * FROM " + input + " WHERE homebrew = 1");
				ResultSetMetaData rsmd = rs.getMetaData();
				int columns = rsmd.getColumnCount();
				for (int i = 1; i <= columns; i++) {
					String value = rsmd.getColumnName(i);
					if (i > 1)
						System.out.print(", ");
					System.out.print(value);
				}
				System.out.println("\n");
				while (rs.next()) {
					for (int i = 1; i <= columns; i++) {
						String value = rs.getString(i);	
						if (i > 1)
							System.out.print(", ");		
						System.out.print(value);
					}
					System.out.println("\n");
				}
				mycon.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private static void addHomebrew() {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Add to \"MONSTERS\" or \"ITEMS\"? (\"BACK\" to go back to admin menu)");
		String input = scanner.nextLine().toLowerCase();
		
		String pKey = getPrimaryKey(input);
		System.out.print(pKey + ": ");
		int ID = Integer.parseInt(scanner.nextLine());
		try {
				Connection mycon = DriverManager.getConnection("jdbc:mysql://localhost/dnd?useSSL=false", "root", "your_password");
				Statement stat = mycon.createStatement();
				stat.executeUpdate("UPDATE " + input + " SET homebrew = 0, campaignID = null WHERE " + pKey + " = " + ID);
				System.out.println("\n--HOMEBREW ADDED TO UNIVERSAL LIST--\n");
				mycon.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
			System.out.println("\n--ADD FAILED--\n");
		}
	}

	private static void appoint(int appointID) {
		int superadmin = 0;
		Scanner scanner = new Scanner(System.in);
		String input;
		int level = 0;
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		try {
			Connection mycon = DriverManager.getConnection("jdbc:mysql://localhost/dnd?useSSL=false", "root", "your_password");
			Statement stat = mycon.createStatement();
			ResultSet rs = stat.executeQuery("SELECT * FROM account WHERE accountID = " + appointID);
			if (rs.next()) {
				
					System.out.println("User " + appointID + ": Enter 0 for user, 1 for admin, or 2 for superadmin");
					try {
						level = Integer.parseInt(scanner.nextLine());
						if (level <= 2 || level >= 0)
							stat.executeUpdate("UPDATE account SET admin = " + level + "WHERE accoundID = " + appointID);
						else
							System.out.println("\n--INVALID INPUT--\n");
					} catch (Exception e) {
						System.out.println("\n--INVALID INPUT--\n");
					}
				
			}
			else
				System.out.println("\n--USER " + appointID + " DOES NOT EXIST--\n");
			mycon.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void adminAdd(String database) {
		int superadmin = 0;
		Scanner scanner = new Scanner(System.in);
		String input;
		int level = 0;
		String columns = "";
		String values = "";
		String pKey = getPrimaryKey(database);
	
			
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		try {
			Connection mycon = DriverManager.getConnection("jdbc:mysql://localhost/dnd?useSSL=false", "root", "your_password");
			Statement stat = mycon.createStatement();
			ResultSet rs = stat.executeQuery("SELECT * FROM " + database);
			ResultSetMetaData rsmd = rs.getMetaData();
			for (int n = 1; n <= rsmd.getColumnCount(); n++) {
				if (!rsmd.getColumnName(n).equals(pKey)) {
					System.out.print(rsmd.getColumnName(n) + ":");
					if (n > 1) {
						values += ", ";	
						columns += ", ";
					}			
					values += "'" + scanner.nextLine() + "'";
					columns += rsmd.getColumnName(n);
				}
			}
			stat.executeUpdate("INSERT INTO " + database + " (" + columns + ") VALUES (" + values + ")"); 
			System.out.println("\n--ROW SUCCESSFULLY ADDED--\n");
			mycon.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("\n--ADD TO " + database + " FAILED--\n");
		}
	}

	private static void adminChange(String database) {
		String pKey = getPrimaryKey(database);
		Scanner scanner = new Scanner(System.in);
		String input = "";
		String column = "";
		String newVal = "";
		

		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		System.out.println(pKey + ": ");
		input = scanner.nextLine();
		try {
			Connection mycon = DriverManager.getConnection("jdbc:mysql://localhost/dnd?useSSL=false", "root", "your_password");
			Statement stat = mycon.createStatement();
			ResultSet rs = stat.executeQuery("SELECT * FROM " + database + " WHERE " + pKey + " = " + input);
			ResultSetMetaData rsmd = rs.getMetaData();
			if (rs.next()) {
				System.out.println("\nCOLUMNS:");
				for (int n = 1; n <= rsmd.getColumnCount(); n++)
					System.out.println(rsmd.getColumnName(n) + " [Current Value: " + rs.getString(n) + "]");
				System.out.print("Select column to be changed: ");
				column = scanner.nextLine();
				if (!column.equals("admin")) {
					System.out.print("New value for " + column + ": ");
					newVal = scanner.nextLine();
					stat.executeUpdate("UPDATE " + database + " SET " + column + " = '" + newVal + "' WHERE " + pKey + " = '" + input + "'");
					System.out.println("\n--UPDATE SUCCESSFUL--\n");
				}
				else
					System.out.println("\n--SUPERADMINS MUST CHANGE USER LEVEL WITH \"LEVEL [ACCOUNTID]\"--\n");
				
			}
			else
				System.out.println("\\n--" + pKey + " " + input + " DOES NOT EXIST IN " + database + "--\n");
			
			mycon.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("\n--UPDATE TO " + database + " FAILED--\n");
		}
	}

	private static String getPrimaryKey(String db) {
		String primaryKey = "";	
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		try {
			Connection mycon = DriverManager.getConnection("jdbc:mysql://localhost/dnd?useSSL=false", "root", "your_password");
			Statement stat = mycon.createStatement();
			DatabaseMetaData dbmd = mycon.getMetaData();
			ResultSet rs = dbmd.getExportedKeys("", "", db);
			while (rs.next())
				primaryKey = rs.getString("PKCOLUMN_NAME");
			mycon.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return primaryKey;
	}
	
	private static void adminDelete(String database) {
		String pKey = getPrimaryKey(database);
		String input = "";
		String column = "";
		Scanner scanner = new Scanner(System.in);

		if (!pKey.equals("")) {
			System.out.print(pKey + ": ");
			input = scanner.nextLine();
			try {
				Class.forName("com.mysql.jdbc.Driver");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

			try {
				Connection mycon = DriverManager.getConnection("jdbc:mysql://localhost/dnd?useSSL=false", "root", "your_password");
				Statement stat = mycon.createStatement();
				ResultSet rs = stat.executeQuery("SELECT * FROM " + database + " WHERE " + pKey + " = " + input);
				if (rs.next()) {
					stat.executeUpdate("DELETE FROM " + database + " WHERE " + pKey + " = " + input);
					System.out.println("\n--" + pKey + " " + input + " IN " + database + " SUCCESSFULLY DELETED--\n");
				}
				else
					System.out.println("\n--" + pKey + " " + input + " IN " + database + " DOES NOT EXIST--\n");
				mycon.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else
			System.out.println("\n--DELETE NOT ALLOWED FOR DATABASE " + database + "--\n");
	}

	private static void adminView(String database) {
		String sql = "SELECT * FROM " + database;
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		try {
			Connection mycon = DriverManager.getConnection("jdbc:mysql://localhost/dnd?useSSL=false", "root", "your_password");
			Statement stat = mycon.createStatement();
			ResultSet rs = stat.executeQuery(sql);
			ResultSetMetaData rsmd = rs.getMetaData();
			int columns = rsmd.getColumnCount();
			for (int i = 1; i <= columns; i++) {
				String value = rsmd.getColumnName(i);
				if (i > 1)
					System.out.print(", ");
				System.out.print(value);
			}
			System.out.println("\n");
			while (rs.next()) {
				for (int i = 1; i <= columns; i++) {
					String value = rs.getString(i);	
					if (i > 1)
						System.out.print(", ");		
					System.out.print(value);
				}
				System.out.println("\n");
			}
		mycon.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void campaignView() {
		String[] help = {"\n",
			"NEW - Start a new campaign as a DM",
			"CHANGE [CAMPAIGN NAME] - [DM ONLY] Change name of campaign",
			"DELETE [CAMPAIGN NAME] - [DM ONLY] Completely deletes campaign and all its information",
			"REMOVE [CAMPAIGN NAME] - [PLAYER ONLY] Removes you and your character from a campaign",
			"LOGOUT - Return to login screen",
			"CLOSE - Exit program",
			 "\n" };
		Scanner scanner = new Scanner(System.in);
		String[] input;
		int dm = 0;
		Boolean exitBool = false;
		int level = 0;
		String campaignName = "";

		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		while (accountID > 0) {
			try {
				Connection mycon = DriverManager.getConnection("jdbc:mysql://localhost/dnd?useSSL=false", "root", "your_password");
				Statement stat = mycon.createStatement();
				ResultSet rs = stat.executeQuery("SELECT campaign.campaignID, campaignName, campaignRole FROM campaign, campaignPlayers WHERE accountID = " + accountID + " AND campaign.campaignID = campaignPlayers.campaignID");
				System.out.println("\nCAMPAIGN LIST:");				
				if (rs.next()) {
					rs.beforeFirst();
					while (rs.next()) {
						System.out.print("\n" + rs.getString(2));
						if (rs.getInt(3) > 0)
							System.out.print("(DM)");
						System.out.println();
					}
				}
				else {
					System.out.println("\n--NO CAMPAIGNS FOUND--");
				}
				System.out.println("\n(Enter \"NEW\" to start new campaign or ask DM of campaign to \"ADDPLAYER " + accountID + "\" to join campaign, or \"HELP\" for more options)\n");
				System.out.print("SELECT CAMPAIGN: ");
				input = scanner.nextLine().split(" ");
				campaignName = "";
				if (input.length > 1) {
					for (int n = 1; n < input.length; n++) {
						campaignName += input[n];
						if (n < input.length - 1)
							campaignName += " ";
					}
				}
				switch(input[0]) {
					case "NEW":
						newCampaign();
						break;
					case "LOGOUT":
						accountID = 0;
						break;
					case "CLOSE":
						accountID = -1;
						break;
					case "CHANGE":
						if (input.length > 1)
							changeCampaign(campaignName);
						else
							System.out.println("\n--PLEASE INDICATE CAMPAIGN NAME IN COMMAND--\n");
						break;
					case "DELETE":
						if (input.length > 1)
							deleteCampaign(campaignName);
						else
							System.out.println("\n--PLEASE INDICATE CAMPAIGN NAME IN COMMAND--\n");
						break;
					case "REMOVE":
						if (input.length > 1)
							removeCampaign(campaignName);
						else
							System.out.println("\n--PLEASE INDICATE CAMPAIGN NAME IN COMMAND--\n");
						break;
					case "HELP":
						for (int n = 0; n < help.length; n++)
							System.out.println(help[n]);
						break;
					default:
						campaignName = "";
						for (int n = 0; n < input.length; n++) {
							campaignName += input[n];
							if (n < input.length - 1)
								campaignName += " ";
						}
						rs = stat.executeQuery("SELECT campaign.campaignID, campaignRole FROM campaign, campaignPlayers WHERE accountID = " + accountID + " AND campaign.campaignID = campaignPlayers.campaignID AND campaignName = '" + campaignName + "'");
						if (rs.next()) {
							campaignID = rs.getInt(1);
							level = rs.getInt(2);
							System.out.println("\n--ENTERING CAMPAIGN " + campaignName + "--\n");
							if (level == 0)
								playerView();
							else if (level == 1)
								dmView();
							else
								System.out.println("\n--ERROR, INVALID PLAYER LEVEL ON CAMPAIGN " + campaignID + ", PLEASE CONTACT ADMIN--\n");
						}
						else
							System.out.println("\n--INVALID CAMPAIGN NAME/COMMAND " + campaignName + "--\n");
						break;
				}
				mycon.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void newCampaign() {
		String campaignName = "";
		Scanner scanner = new Scanner(System.in);
		Boolean inputBool = false;
		Boolean cancelBool = false;

		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		try {
			Connection mycon = DriverManager.getConnection("jdbc:mysql://localhost/dnd?useSSL=false", "root", "your_password");
			Statement stat = mycon.createStatement();
			while (!inputBool) {
			Boolean existingCampaign = true;
				while (existingCampaign) {
					System.out.print("Enter name for new campaign: ");
					campaignName = scanner.nextLine();
					ResultSet rs = stat.executeQuery("SELECT * FROM campaign WHERE campaignName = '" + campaignName + "'");
					if (rs.next())
						System.out.println("\n--CAMPAIGN " + campaignName + " ALREADY EXISTS--\n");
					else
						existingCampaign = false;
				}
				if (!campaignName.equals("")) {
					System.out.println("Start campaign " + campaignName + " as a DM? (y/n)\n(To join campaign as player, tell DM to \"ADDPLAYER " + accountID + "\")");
					while (!inputBool) {
						switch (scanner.nextLine().toLowerCase()) {
							case "y":
								inputBool = true;
								break;
							case "n":
								cancelBool = true;
								inputBool = true;
								break;
							default:
								System.out.println("\n--INVALID INPUT--\n");
								break;
						}
					}
						
				}
			}
		
			if (!cancelBool) {
				stat.executeUpdate("INSERT INTO campaign (campaignName) VALUES ('" + campaignName + "')");
				ResultSet rs = stat.executeQuery("SELECT campaignID FROM campaign WHERE campaignName = '" + campaignName + "'");
				rs.next();
				stat.executeUpdate("INSERT INTO campaignPlayers VALUES (" + rs.getInt(1) + ", " + accountID + ", 1)");
			}
			

			System.out.println("\n--CAMPAIGN CREATED SUCCESSFULLY--\n");
			mycon.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("\n--CAMPAIGN CREATION FAILED--\n");
		}
	}

	private static void changeCampaign(String campaign) {
		Scanner scanner = new Scanner(System.in);
		int ID = 0;
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		try {
			Connection mycon = DriverManager.getConnection("jdbc:mysql://localhost/dnd?useSSL=false", "root", "your_password");
			Statement stat = mycon.createStatement();
			ResultSet rs = stat.executeQuery("SELECT campaign.campaignID, campaignRole FROM campaign, campaignPlayers WHERE campaignName = '" + campaign + "' AND accountID = " + accountID);
			if (rs.next()) {
				ID = rs.getInt(1);
				if (rs.getInt(2) == 0)
					System.out.println("\n--PERMISSION DENIED, ONLY DMS MAY CHANGE CAMPAIGN NAME--\n");
				else {
					System.out.print("New name for " + campaign + ": ");
					String newName = scanner.nextLine();
					rs = stat.executeQuery("SELECT * FROM campaign WHERE campaignName = '" + newName + "'");
					if (rs.next())
						System.out.println("\n--CAMPAIGN " + newName + " ALREADY EXISTS--\n");
					else
						stat.executeUpdate("UPDATE campaign SET campaignName = '" + newName + "' WHERE campaignID = " + ID);
				}
			}
			else
				System.out.println("\n--NO CAMPAIGN " + campaign + " ATTACHED TO YOUR ACCOUNT--\n");
			mycon.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("\n--CAMPAIGN NAME CHANGE FAILED--\n");
		}
	}

	private static void deleteCampaign(String campaign) {
		String input = "";
		Scanner scanner = new Scanner(System.in);

		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		try {
			Connection mycon = DriverManager.getConnection("jdbc:mysql://localhost/dnd?useSSL=false", "root", "your_password");
			Statement stat = mycon.createStatement();

			ResultSet rs = stat.executeQuery("SELECT campaignRole FROM campaign, campaignPlayers WHERE campaignName = '" + campaign + "' AND accountID = " + accountID + " AND campaign.campaignID = campaignRole.campaignID");
			if (rs.next()) {
				if (rs.getInt(1) == 0)
					System.out.println("\n--ONLY DMS HAVE PERMISSION TO DELETE CAMPAIGNS--\n");
				else {
					Boolean inputBool = false;
					while (!inputBool) {
						System.out.println("Delete campaign " + campaign + "? All data related to campaign will be lost! (y/n)");
						input = scanner.nextLine();
						switch (input) {
							case "y":
								stat.executeUpdate("DELETE FROM campaign WHERE campaignName = '" + campaign + "'");
								System.out.println("\n--CAMPAIGN DELETION SUCCESSFUL--\n");
								inputBool = true;
								break;
							case "n":
								System.out.println("\n--DELETION CANCELLED--\n");
								inputBool = true;
								break;
							default:
								System.out.println("\n--INVALID INPUT--\n");
								break;
						}
					}
				}
			}
			else
				System.out.println("\n--NO CAMPAIGN " + campaign + " FOUND LINKED TO YOUR ACCOUNT--\n");
			
			mycon.close();			
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("\n--CAMPAIGN DELETION FAILED--\n");
		}
	}

	private static void removeCampaign (String campaign) {
		String input = "";
		Scanner scanner = new Scanner(System.in);
		int ID = 0;

		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		try {
			Connection mycon = DriverManager.getConnection("jdbc:mysql://localhost/dnd?useSSL=false", "root", "your_password");
			Statement stat = mycon.createStatement();

			ResultSet rs = stat.executeQuery("SELECT campaignRole, campaign.campaignID FROM campaign, campaignPlayers WHERE campaignName = '" + campaign + "' AND accountID = " + accountID + " AND campaign.campaignID = campaignPlayers.campaignID");
			if (rs.next()) {
				ID = rs.getInt(2);
				if (rs.getInt(1) == 1)
					System.out.println("\n--DM CAN NOT LEAVE CAMPAIGN--\n");
				else {
					Boolean inputBool = false;
					while (!inputBool) {
						System.out.println("Leave campaign " + campaign + "? All data related to you and your character will be lost! (y/n)");
						input = scanner.nextLine();
						switch (input) {
							case "y":
								stat.executeUpdate("DELETE FROM campaignPlayers WHERE campaignID = '" + ID + "' AND accoundID = " + accountID);
								System.out.println("\n--REMOVED FROM CAMPAIGN--\n");
								inputBool = true;
								break;
							case "n":
								System.out.println("\n--REMOVAL CANCELLED--\n");
								inputBool = true;
								break;
							default:
								System.out.println("\n--INVALID INPUT--\n");
								break;
						}
					}
				}
			}
			else
				System.out.println("\n--NO CAMPAIGN " + campaign + " FOUND LINKED TO YOUR ACCOUNT--\n");
			
			mycon.close();			
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("\n--CAMPAIGN REMOVAL FAILED--\n");
		}
	}

	private static void playerView() {
		String[] help = {"\n",
			"CREATE - Make new character",
			"DELETE - Delete current character",
			"ROLL - Roll 1d20",
			"CHANGE - Change value of character sheet",
			"\tROLL [SKILL/STAT] - Roll 1d20 based on skill/stat",
			"\tROLL [DIE] [NUMBER] - Rolls [NUMBER] of [DIE]-sided dice",
			"VIEW - Shows your character's stats",
			"\tVIEW ITEMS - Show all items",
			"\tVIEW INVENTORY - Show items in character inventory",
			"CAMPAIGN - Return to campaign screen",
			"LOGOUT - Return to login screen",
			"CLOSE - Exit program",
			"\n" };
		Scanner scanner = new Scanner(System.in);
		Boolean campaignScreen = false;

		while (accountID > 0 && !campaignScreen) {
			System.out.println("Enter command:");
			String[] input = scanner.nextLine().split(" ");
			switch (input[0]) {
				case "ROLL":
					if (input.length == 1)
						System.out.println("Result: " + roll(20));
					else if (input.length == 2)
						rollStat(input[1]);
					else if (input.length == 3) {
						int result = 0;
						for (int n = Integer.parseInt(input[2]); n > 0; n--)
							result += roll(Integer.parseInt(input[1]));
						System.out.println("Result: " + result);
					}
					break;
				case "CREATE":
					addCharacter();
					break;
				case "DELETE":
					removeCharacter();
					break;
				case "VIEW":
					if (input.length == 1)
						viewPlayer("character");
					if (input.length == 2) {
						String db = input[1].toLowerCase();
						viewPlayer(db);	
					}
					break;
				case "CHANGE":
					change("character");
					break;
				case "HELP":
					for (int n = 0; n < help.length; n++)
						System.out.println(help[n]);
					break;
				case "CAMPAIGN":
					campaignScreen = true;
					campaignID = 0;
					break;
				case "LOGOUT":
					accountID = 0;
					campaignID = 0;
					break;
				case "CLOSE":
					accountID = -1;
					break;
				default:
					System.out.println("\n--INVALID COMMAND--\n");
					break;
			}
		}
		
	}

	private static void viewPlayer(String database) {
		String where = " campaignID = " + campaignID + " AND accountID = " + accountID;
		String select = "*";
		if (database.equals("character") || database.equals("inventory")) {
			if (database.equals("character"))
				database = "pc";

			if (database.equals("inventory")) {
				select = "name";
				database = "inventory, items";
				where += " AND inventory.itemID = items.itemID";
				
			}
			try {
				Connection mycon = DriverManager.getConnection("jdbc:mysql://localhost/dnd?useSSL=false", "root", "your_password");
				Statement stat = mycon.createStatement();
				ResultSet rs = stat.executeQuery("SELECT " + select + " FROM " + database + " WHERE " + where);
				ResultSetMetaData rsmd = rs.getMetaData();
				int columns = rsmd.getColumnCount();
				for (int i = 1; i <= columns; i++) {
					String value = rsmd.getColumnName(i);
					if (i > 1)
						System.out.print(", ");
					System.out.print(value);
				}
				System.out.println("\n");
				while (rs.next()) {
					for (int i = 1; i <= columns; i++) {
						String value = rs.getString(i);	
						if (i > 1)
							System.out.print(", ");		
						System.out.print(value);
					}
					System.out.println("\n");
				}
				mycon.close();
			} catch(SQLException e) {
				e.printStackTrace();
			}
		}
		else		
			view(database);
	}

	private static void addCharacter() {
		Scanner scanner = new Scanner(System.in);
		String input;
		int level = 0;
		String columns = "";
		String values = "";
		String pKey = getPrimaryKey("pc");
		String where = "";
	
			
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		try {
			Connection mycon = DriverManager.getConnection("jdbc:mysql://localhost/dnd?useSSL=false", "root", "your_password");
			Statement stat = mycon.createStatement();
			ResultSet rs = stat.executeQuery("SELECT * FROM pc WHERE accountID = " + accountID + " AND campaignID = " + campaignID);
			if (!rs.next()) {
				ResultSetMetaData rsmd = rs.getMetaData();
				for (int n = 1; n <= rsmd.getColumnCount(); n++) {
					if (!rsmd.getColumnName(n).equals(pKey)) {
							if (!rsmd.getColumnName(n).equals("accountID") && !rsmd.getColumnName(n).equals("campaignID"))
								System.out.print(rsmd.getColumnName(n) + ":");
							if (n > 1) {
								values += ", ";	
								columns += ", ";
							}
							if (rsmd.getColumnName(n).equals("accountID"))
								values += accountID;
							else if (rsmd.getColumnName(n).equals("campaignID"))
								values += campaignID;
							else			
								values += "'" + scanner.nextLine() + "'";
							columns += rsmd.getColumnName(n);
					}
				}
				stat.executeUpdate("INSERT INTO pc (" + columns + ") VALUES (" + values + ")"); 
				System.out.println("\n--ROW SUCCESSFULLY ADDED--\n");
			}
			else
				System.out.println("\n--YOU MAY ONLY CONTROL ONE CHARACTER PER CAMPAIGN--\n");
			mycon.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("\n--CHARACTER CREATION FAILED--\n");
		}
	}
	
	private static void removeCharacter() {
		Scanner scanner = new Scanner(System.in);
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		try {
			Connection mycon = DriverManager.getConnection("jdbc:mysql://localhost/dnd?useSSL=false", "root", "your_password");
			Statement stat = mycon.createStatement();
			System.out.println("Delete character from campaign?");
			switch (scanner.nextLine()) {
				case "y":
					stat.executeUpdate("DELETE FROM pc WHERE accountID = " + accountID + " AND  campaignID = " + campaignID);
					break;
				case "n":
					break;
			}
			mycon.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void rollStat(String stat) {
		stat = terms(stat);
		int modifier = 0;
		String attribute = "";
		int result = 0;

		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		try {
			Connection mycon = DriverManager.getConnection("jdbc:mysql://localhost/dnd?useSSL=false", "root", "your_password");
			Statement state = mycon.createStatement();

			ResultSet rs = state.executeQuery("SELECT attribute FROM skills WHERE name = '" + stat + "'");
			if (rs.next()) {
				attribute = rs.getString(1);
				rs = state.executeQuery("SELECT modifier FROM modifiers, pc WHERE accountID = " + accountID + " AND campaignID = " + campaignID + " AND " + attribute + " =  abilityScore");
				rs.next();
				modifier = rs.getInt(1);
			}
			else
				System.out.println("\n--INVALID STAT/SKILL--\n");
			result = roll(20) + modifier;
			System.out.println("Result: " + result);
			mycon.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

	private static void dmView() {
		String[] help = {"\n",
			"ADDPLAYER [ACCOUNTID] - Add player w/ corresponding [ACCOUNTID] to campaign",
			"REMOVEPLAYER [ACCOUNTID] - Remove player [ACCOUNTID] from campaign",
			"VIEW ... - View info (Type \"VIEW HELP\" to see all options)",
			"ADD [MONSTERS OR ITEMS] - Create homebrew monster or item",
			"CHANGE ... - Change value",
			"ROLL - Roll 1d20",
			"\tROLL [DIE] [NUMBER] - Roll [DIE] [NUMBER] times",
			"LOGOUT - Return to login screen",
			"CLOSE - Exit program",
			"\n"};
		String[] viewHelp = {"\n",
			"VIEW CAMPAIGNPLAYERS - List IDs of everyone in campaign",
			"VIEW MONSTERS - List stats for all monsters",
			"VIEW PC - List stats for all player characters",
			"VIEW NPC - List stats of all NPCs",
			"VIEW ITEMS - List all items",
			"\n"};
		Scanner scanner = new Scanner(System.in);
		Boolean campaignScreen = false;	

		while (!campaignScreen && accountID > 0) {
			System.out.println("Enter command:");
			String[] input = scanner.nextLine().split(" ");
			if (input.length > 1)
				input[1] = input[1].toLowerCase();
			switch (input[0]) {
				case "ADDPLAYER":
					addPlayer(Integer.parseInt(input[1]));
					break;
				case "REMOVEPLAYER":
					removePlayer(Integer.parseInt(input[1]));
					break;
				case "ROLL":
					if (input.length == 1)
						System.out.println("Result: " + roll(20));
					else if (input.length == 3) {
						int result = 0;
						for (int n = Integer.parseInt(input[2]); n > 0; n--)
							result += roll(Integer.parseInt(input[1]));
						System.out.println("Result: " + result);
					}
					break;
				case "ADD":
					add(input[1]);
					break;
				case "VIEW":
					if (input[1].equals("help")) {
						for (int n = 0; n < viewHelp.length; n++)
						System.out.println(viewHelp[n]);
					}
					else
						view(input[1]);
					break;
				case "CLOSE":
					accountID = -1;
					break;
				case "LOGOUT":
					accountID = 0;
					campaignID = 0;
					break;
				case "CAMPAIGN":
					campaignID = 0;
					campaignScreen = true;
					break;
				case "HELP":
					for (int n = 0; n < help.length; n++)
						System.out.println(help[n]);
					break;
				case "CHANGE":
					change(input[1]);
					break;
				default:
					System.out.println("\n--INVALID COMMAND--\n");
					break;
				
			}
		}

	}

	private static void change(String database) {
		String pKey = getPrimaryKey(database);
		Scanner scanner = new Scanner(System.in);
		String input = "";
		String column = "";
		String newVal = "";
		String where = "";
		if (database.equals("character")) {
			database = "pc";
			where = " AND campaignID = " + campaignID;
		}
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		System.out.println(pKey + ": ");
		if (where.equals(""))
			input = scanner.nextLine();
		else
			input += accountID;
			pKey = "accountID";
		try {
			Connection mycon = DriverManager.getConnection("jdbc:mysql://localhost/dnd?useSSL=false", "root", "your_password");
			Statement stat = mycon.createStatement();
			ResultSet rs = stat.executeQuery("SELECT * FROM " + database + " WHERE " + pKey + " = " + input + where);
			ResultSetMetaData rsmd = rs.getMetaData();
			if (rs.next()) {
				System.out.println("\nCOLUMNS:");
				for (int n = 1; n <= rsmd.getColumnCount(); n++)
					System.out.println(rsmd.getColumnName(n) + " [Current Value: " + rs.getString(n) + "]");
				System.out.print("Select column to be changed: ");
				column = scanner.nextLine();
				if (!column.equals("admin")) {
					System.out.print("New value for " + column + ": ");
					newVal = scanner.nextLine();
					stat.executeUpdate("UPDATE " + database + " SET " + column + " = '" + newVal + "' WHERE " + pKey + " = '" + input + "'" + where);
					System.out.println("\n--UPDATE SUCCESSFUL--\n");
				}
				else
					System.out.println("\n--SUPERADMINS MUST CHANGE USER LEVEL WITH \"LEVEL [ACCOUNTID]\"--\n");
				
			}
			else
				System.out.println("\\n--" + pKey + " " + input + " DOES NOT EXIST IN " + database + "--\n");
			
			mycon.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("\n--UPDATE TO " + database + " FAILED--\n");
		}
	}

	private static void view(String db) {
		String input;
		int level = 0;
		String columns = "";
		String values = "";
		String pKey = getPrimaryKey(db);
		String where = "";
		String select = " *";
		
		
		if (db.equals("items") || db.equals("monsters"))
			where += " (homebrew = 1 AND campaignID = " + campaignID + ") OR homebrew = 0";
		else if (db.equals("campaignplayers") || db.equals("npc") || db.equals("pc")) {
			where += " campaignID = " + campaignID;
		}

		if (db.equals("campaignplayers")) {
			db = "campaignPlayers, account";
			select = "username, campaignPlayers.accountID, campaignRole";
			where += " AND campaignPlayers.accountID = account.accountID";
		}

		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		try {
			Connection mycon = DriverManager.getConnection("jdbc:mysql://localhost/dnd?useSSL=false", "root", "your_password");
			Statement stat = mycon.createStatement();
			ResultSet rs = stat.executeQuery("SELECT " + select + " FROM " + db);
			ResultSetMetaData rsmd = rs.getMetaData();
			for (int i = 1; i <= rsmd.getColumnCount(); i++) {
				if (!rsmd.getColumnName(i).equals("homebrew") && !rsmd.getColumnName(i).equals("campaignID")) {
					if (i > 1)
						System.out.print(", ");				
					System.out.print(rsmd.getColumnName(i));
				}
			}
			System.out.println("\n");
			rs = stat.executeQuery("SELECT " + select + " FROM " + db + " WHERE " + where);
			while (rs.next()) {
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					if (!rsmd.getColumnName(i).equals("homebrew") && !rsmd.getColumnName(i).equals("campaignID")) {
						String value = rs.getString(i);	
						if (i > 1)
							System.out.print(", ");		
						System.out.print(value);
					}
				}
				System.out.println("\n");
			}
			
			mycon.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("\n--ADD FAILED--\n");
		}
	}

	private static void add(String db) {
		Scanner scanner = new Scanner(System.in);
		String input;
		int level = 0;
		String columns = "";
		String values = "";
		String pKey = getPrimaryKey(db);
		int i = 1;

		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		try {
			Connection mycon = DriverManager.getConnection("jdbc:mysql://localhost/dnd?useSSL=false", "root", "your_password");
			Statement stat = mycon.createStatement();
			ResultSet rs = stat.executeQuery("SELECT * FROM " + db);
			ResultSetMetaData rsmd = rs.getMetaData();
			for (int n = 1; n <= rsmd.getColumnCount(); n++) {
				if (!rsmd.getColumnName(n).equals(pKey)) {
					if (!rsmd.getColumnName(n).equals("campaignID") && !rsmd.getColumnName(n).equals("homebrew"))
						System.out.print(rsmd.getColumnName(n) + ":");
					if (i > 1) {
						values += ", ";	
						columns += ", ";
					}
					if (rsmd.getColumnName(n).equals("campaignID"))
						values += campaignID;
					else if (rsmd.getColumnName(n).equals("homebrew"))
						values += "1";
					else			
						values += "'" + scanner.nextLine() + "'";
					columns += rsmd.getColumnName(n);
					i++;
				}
			}
			stat.executeUpdate("INSERT INTO " + db + "(" + columns + ") VALUES (" + values + ")"); 
			System.out.println("\n--ROW SUCCESSFULLY ADDED--\n");
			mycon.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("\n--ADD FAILED--\n");
		}
	}

	private static void addPlayer(int ID) {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		try {
			Connection mycon = DriverManager.getConnection("jdbc:mysql://localhost/dnd?useSSL=false", "root", "your_password");
			Statement stat = mycon.createStatement();
			ResultSet rs = stat.executeQuery("SELECT * FROM account WHERE accountID = " + ID);
			if (rs.next()) {
				stat.executeUpdate("INSERT INTO campaignPlayers VALUES (" + campaignID + ", " + ID + ", 0)");
				System.out.println("\n--PLAYER ADDED TO CAMPAIGN--\n");
			}
			else
				System.out.println("\n--ACCOUNT ID DOES NOT EXIST--\n");
			mycon.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void removePlayer(int ID) {
		if (!(ID == accountID)) {
			try {
				Class.forName("com.mysql.jdbc.Driver");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

			try {
				Connection mycon = DriverManager.getConnection("jdbc:mysql://localhost/dnd?useSSL=false", "root", "your_password");
				Statement stat = mycon.createStatement();
				ResultSet rs = stat.executeQuery("SELECT * FROM campaignPlayers WHERE accountID = " + ID + " AND campaignID = " + campaignID);
				if (rs.next()) {
					stat.executeUpdate("DELETE FROM campaignPlayers WHERE accountID = " + ID + " AND campaignID = " + campaignID);
					System.out.println("\n--PLAYER REMOVED FROM CAMPAIGN--\n");
				}
				else
					System.out.println("\n--ACCOUNT ID DOES NOT EXIST IN CAMPAIGN--\n");
				mycon.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		else
			System.out.println("\n--YOU CAN NOT REMOVE YOURSELF FROM YOUR OWN CAMPAIGN--\n");
	}
	
	private static String terms(String input) {
		int n = 0;
		while (n < terms1.length) {
			if (input.toLowerCase().equals(terms1[n]))
				input = terms2[n];
			n++;
		}
		return input;
	}

	private static int roll(int die) {
		Random r = new Random();
		int result = 0;

		if (die == 2 || die == 4 || die == 6 || die == 8 || die == 10 || die == 12 || die == 20 || die == 100) {
			while (result < 1)
				result = r.nextInt() % die + 1;
			return result;
		}
		else
			return -1;
	}

}
