package files;

import authorization.NetAuthService;
import constants.NetConstants;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class NetFile implements Externalizable{
	private boolean isFile;
	private boolean isDirectory;

	private SimpleStringProperty name;
	private SimpleStringProperty path;
	private SimpleStringProperty sizeString;

	private long size;

	private String MD5;

	private String folder;

	public NetFile() {
		this.name = new SimpleStringProperty();
		this.path = new SimpleStringProperty();
		this.sizeString = new SimpleStringProperty();
	}

    public NetFile(File file, String folder) {
        this();
        this.isFile = file.isFile();
        this.isDirectory = file.isDirectory();
        this.name.set(file.getName());
        this.folder = folder;
        this.path.set(folder + "\\" +file.getName());
        this.MD5 = NetAuthService.getMD5(path.get());
        this.size = file.length();

        if (isDirectory) this.sizeString.set("");
        else{
            long sizeKB = size/1000;
            long sizeMB = sizeKB/1000;
            long sizeGB = sizeMB/1000;
            long sizeTB = sizeGB/1000;
            if (sizeTB > 0) sizeString.set(Long.toString(sizeTB) + " TByte");
            else if (sizeGB > 0) sizeString.set(Long.toString(sizeGB) + " GByte");
            else if (sizeMB > 0) sizeString.set(Long.toString(sizeMB) + " MByte");
            else if (sizeKB > 0) sizeString.set(Long.toString(sizeKB) + " KByte");
            else sizeString.set(Long.toString(size) + " Byte");
        }
    }

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(isFile);
		out.writeObject(isDirectory);
		out.writeObject(name.get());
		out.writeObject(path.get());
		out.writeObject(sizeString.get());
		out.writeObject(size);
		out.writeObject(MD5);
		out.writeObject(folder);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		isFile = (boolean) in.readObject();
		isDirectory = (boolean) in.readObject();
		name.set((String) in.readObject());
		path.set((String) in.readObject());
		sizeString.set((String) in.readObject());
		size = (long) in.readObject();
		MD5 = (String) in.readObject();
		folder = (String) in.readObject();
	}

	@Override
	public String toString() {
		return "isFile = " + isFile + "\n" +
				"isDirectory = " + isDirectory + "\n" +
				"name = " + name.get() + "\n" +
				"path = " + path.get() + "\n" +
				"sizeString = " + sizeString.get() + "\n" +
				"size = " + size + "\n" +
                "folder = " + folder;
	}

	public void setFile(boolean file) {
		this.isFile = file;
	}

	public void setDirectory(boolean directory) {
		this.isDirectory = directory;
	}

	public void setName(String name) {
		this.name.set(name);
	}

	public void setPath(String path) {
		this.path.set(path);
	}

	public void setSize(long size) {
		this.size = size;
	}

    public void setSizeString(String sizeString) {
        this.sizeString.set(sizeString);
    }

    public SimpleStringProperty nameProperty() {
		return name;
	}

	public SimpleStringProperty pathProperty() {
		return path;
	}

	public SimpleStringProperty sizeStringProperty() {
		return sizeString;
	}


	public boolean isFileRet() {
		return isFile;
	}

	public boolean isDirectory() {
		return isDirectory;
	}

	public String getName() {
		return name.get();
	}

	public String getPath() {
		return path.get();
	}

	public long getSize() {
		return size;
	}

	public String getMD5() {
		return MD5;
	}

	public String getFolder() {
		return folder;
	}
}
