package authorization;

import db.NetConnectionDB;
import files.NetTreeFiles;
import users.NetUser;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class NetAuthService {
	private NetConnectionDB connectionDB;

	synchronized public static String getMD5(String value){
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(value.getBytes());
			byte[] tmpMD5 = md5.digest();

			StringBuilder sb = new StringBuilder();
			for (byte aMd5 : tmpMD5) {
				String s = Integer.toHexString(0xff & aMd5);
				s = (s.length() == 1) ? "0" + s : s;
				sb.append(s);
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}



	public NetAuthService() {
		connectionDB = new NetConnectionDB();
	}

	synchronized public boolean requestAuth(String login, String pass){
		return connectionDB.requestAuth(login, pass);
	}

	synchronized public NetUser getUser(String login){
		return connectionDB.getUser(login);
	}

	synchronized public boolean requestReg(NetUser newUser){
		return connectionDB.requestReg(newUser);
	}

	synchronized public void saveTreeFiles(NetTreeFiles treeFiles, String login){
	    connectionDB.saveTreeFiles(treeFiles, login);
    }
}
