package users;

import files.NetTreeFiles;

import java.io.Serializable;

public class NetUser implements Serializable {
	private int id;
	private String name;
	private String login;
	private String password;
	private String checkWord;
	private int space;
	private String salt;
	private String folder;
	private NetTreeFiles treeFiles;

	public NetUser() {
	}

	public NetUser(String name, String login, String checkWord, String password, int space){
		this.name = name;
		this.login = login;
		this.checkWord = checkWord;
		this.password = password;
		this.space = space;
	}

	public NetUser(int id, String name, String login, String password, String checkWord, int space, String salt, String folder, NetTreeFiles treeFiles) {
		this.id = id;
		this.name = name;
		this.login = login;
		this.password = password;
		this.checkWord = checkWord;
		this.space = space;
		this.salt = salt;
		this.folder = folder;
		this.treeFiles = treeFiles;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getLogin() {
		return login;
	}

	public String getPassword() {
		return password;
	}

	public String getCheckWord() {
		return checkWord;
	}

	public int getSpace() {
		return space;
	}

	public String getSalt() {
		return salt;
	}

	public String getFolder() {
		return folder;
	}

	public NetTreeFiles getTreeFiles() {
		return treeFiles;
	}

	public void setTreeFiles(NetTreeFiles treeFiles) {
		this.treeFiles = treeFiles;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setCheckWord(String checkWord) {
		this.checkWord = checkWord;
	}

	public void setSpace(int space) {
		this.space = space;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}
}
