package messages.enums;

public enum NetMessageType {
	REQUEST_AUTHORIZATION,
	RESPONSE_AUTHORIZATION,

	REQUEST_REGISTRATION,
	RESPONSE_REGISTRATION,

	DISCONNECT_CLIENT,
	DISCONNECT_SERVER,

	UPLOADING_FILE,
	UPLOAD_FILE_IS_OK,

    DOWNLOADING_FILE,

    USER_LOGOUT,
    DELETE_FILE
}
