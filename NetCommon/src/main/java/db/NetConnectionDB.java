package db;

import constants.NetConstants;
import files.NetFile;
import files.NetTreeFiles;
import users.NetUser;

import java.io.*;
import java.sql.*;

public class NetConnectionDB {
	private Connection connection = null;
	private Statement statement = null;

	public NetConnectionDB() {
		try {
			connection = DriverManager.getConnection(NetConstants.URL,
					NetConstants.USERNAME,
					NetConstants.PASSWORD);
			statement = connection.createStatement();
		} catch (SQLException e) {
			for (Throwable throwable : e) {
				e.printStackTrace();
			}
		}

		createTable();
	}

	private void createTable(){
		try {

			connection.setAutoCommit(false);

			String sqlQuery = "CREATE TABLE IF NOT EXISTS users " +
					"(id            SERIAL PRIMARY KEY," +
					" name          TEXT, " +
					" login         TEXT, " +
					" password      TEXT, " +
					" check_word    TEXT, " +
					" space         INTEGER, " +
					" salt          TEXT, " +
					" folder        TEXT, " +
					" tree_files    BYTEA)";

			statement.executeUpdate(sqlQuery);
			connection.commit();
            connection.setAutoCommit(true);

			/*String sql1 = "INSERT INTO users(name, login, password, check_word, space, salt, folder, tree_files)" +
					"                  VALUES('anton', 'anton', '1', 'anton_1', 5, 'salt1', 'anton_folder', NULL)";

			String sql2 = "INSERT INTO users(name, login, password, check_word, space, salt, folder, tree_files)" +
					"                  VALUES('andrey', 'andrey', '2', 'andrey_2', 3, 'salt2', 'andrey_folder', NULL)";

			String sql3 = "INSERT INTO users(name, login, password, check_word, space, salt, folder, tree_files)" +
					"                  VALUES('maxim', 'maxim', '3', 'maxim_3', 7, 'salt3', 'maxim_folder', NULL)";

			String sql4 = "INSERT INTO users(name, login, password, check_word, space, salt, folder, tree_files)" +
					"                  VALUES('oleg', 'oleg', '4', 'oleg_4', 8, 'salt4', 'oleg_folder', NULL)";

			String sql5 = "INSERT INTO users(name, login, password, check_word, space, salt, folder, tree_files)" +
					"                  VALUES('kirill', 'kirill', '5', 'kirill_5', 2, 'salt5', 'kirill_folder', NULL)";
			statement.executeUpdate(sql1);
			statement.executeUpdate(sql2);
			statement.executeUpdate(sql3);
			statement.executeUpdate(sql4);
			statement.executeUpdate(sql5);
			connection.commit();
			connection.setAutoCommit(true);*/
		} catch (SQLException e) {
			for (Throwable throwable : e) {
				e.printStackTrace();
			}
		}
	}

	private boolean isFindLogin(String login){
		try {
			ResultSet resultSet = statement.executeQuery("SELECT login FROM users WHERE login = '"+login+"'");
			return resultSet.next();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	private boolean addNewUser(NetUser newUser){
		try {
			connection.setAutoCommit(false);
			String sqlQuery = "INSERT INTO users(name, login, password, check_word, space, salt, folder, tree_files)" +
											"VALUES('"+newUser.getName()+"'," +
													"'"+newUser.getLogin()+"', " +
													"'"+newUser.getPassword()+"', " +
													"'"+newUser.getCheckWord()+"', " +
													""+newUser.getSpace()+", " +
													"'salt_"+newUser.getLogin()+"', " +
													"'"+newUser.getLogin()+"_folder', " +
													" ? );";
			PreparedStatement ps = connection.prepareStatement(sqlQuery);

			byte[] byteArrOfTreeFiles = createNewTreeFiles(newUser.getLogin() + "_folder");
			if (byteArrOfTreeFiles != null) ps.setBytes(1, byteArrOfTreeFiles);
			else return false;

			ps.executeUpdate();
			connection.commit();
			connection.setAutoCommit(true);
			ps.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	private byte[] createNewTreeFiles(String path){
		File newFile = new File(NetConstants.SERVER_FOLDER_PATH + path);
		if (!newFile.exists()){
			try {
				if(newFile.mkdir()){
					NetTreeFiles newTreeFiles = new NetTreeFiles(path);

					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					ObjectOutputStream ous = new ObjectOutputStream(baos);
					ous.writeObject(newTreeFiles);
					ous.flush();

					byte[] result = baos.toByteArray();

					ous.close();
					baos.close();

					return result;
				} else return null;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		} else return null;
	}
	private NetTreeFiles returnTreeFilesFromBytes(byte[] src){
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(src);
			ObjectInputStream ois = new ObjectInputStream(bais);

			NetTreeFiles netTreeFiles = (NetTreeFiles) ois.readObject();

			ois.close();
			return netTreeFiles;
		} catch (IOException | ClassNotFoundException | ClassCastException e) {
			e.printStackTrace();
			return null;
		}
	}

	public boolean requestReg(NetUser newUser) {
		if (!isFindLogin(newUser.getLogin())) return addNewUser(newUser);
		else return false;
	}

	public boolean requestAuth(String login, String pass){
		if (isFindLogin(login)){
			try {
				ResultSet resultSet =  statement.executeQuery("SELECT login,password FROM users WHERE login = '"+login+"'");
				resultSet.next();
				String newPass = resultSet.getString("password");
				if (newPass.equals(pass)) return true;
				else return false;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		}else return false;
	}

	public NetUser getUser(String login){
		NetUser netUser;
		try {
			ResultSet resultSet = statement.executeQuery("SELECT id,name,login,password,check_word,space,salt,folder,tree_files FROM users WHERE login = '"+login+"'");
			if(resultSet.next()){
				NetTreeFiles treeFiles = returnTreeFilesFromBytes(resultSet.getBytes("tree_files"));
				if (treeFiles != null){
					netUser = new NetUser(resultSet.getInt("id"),
							resultSet.getString("name"),
							resultSet.getString("login"),
							resultSet.getString("password"),
							resultSet.getString("check_word"),
							resultSet.getInt("space"),
							resultSet.getString("salt"),
							resultSet.getString("folder"),
							treeFiles);
					return netUser;
				} else {
					return null;
				}
			} else return null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

    public void saveTreeFiles(NetTreeFiles treeFiles, String login){
        try {
            connection.setAutoCommit(false);
            String sql = "UPDATE users SET tree_files = ? WHERE login = '"+login+"'";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setBytes(1, getByteFromTreeFiles(treeFiles));
            ps.executeUpdate();
            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private byte[] getByteFromTreeFiles(NetTreeFiles treeFiles){
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream ous = new ObjectOutputStream(baos);

            ous.writeObject(treeFiles);
            ous.flush();

            byte[] result = baos.toByteArray();

            ous.close();
            baos.close();

            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


}

