package controllers.interfaces;

public interface MainForManagers {
	void addNewProgressBar(String path, String md5, String type);
	void updateProgressBar(String md5, double progress);
    void removeProgressBar(String md5);
}
