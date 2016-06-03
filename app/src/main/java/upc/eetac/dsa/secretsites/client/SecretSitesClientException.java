package upc.eetac.dsa.secretsites.client;

/**
 * Created by Marti on 11/05/2016.
 */
public class SecretSitesClientException extends Exception {

    private int status;
    private String reason;

    public SecretSitesClientException(String detailMessage) {
        super(detailMessage);
        this.reason = detailMessage;
    }

    public int getStatus() {
        return this.status;
    }

    public String getReason() {
        return this.reason;
    }
}
