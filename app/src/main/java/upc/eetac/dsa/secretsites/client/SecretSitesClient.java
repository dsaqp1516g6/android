package upc.eetac.dsa.secretsites.client;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.glassfish.jersey.client.ClientConfig;

import java.net.ConnectException;
import java.util.List;
import java.util.Properties;

import javax.annotation.Resource;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import upc.eetac.dsa.secretsites.MainActivity;
import upc.eetac.dsa.secretsites.R;
import upc.eetac.dsa.secretsites.entity.AuthToken;
import upc.eetac.dsa.secretsites.entity.Link;
import upc.eetac.dsa.secretsites.entity.Root;
import upc.eetac.dsa.secretsites.entity.User;

/**
 * Created by Marti on 11/05/2016.
 */
public class SecretSitesClient {
    //private final static String BASE_URI = "http://192.168.1.133:8080/secretsites";
    private final static String BASE_URI = "http://10.83.32.224:8080/secretsites";
    private static SecretSitesClient instance;
    private Root root;
    private ClientConfig clientConfig = null;
    private Client client = null;
    private AuthToken authToken = null;
    private final static String TAG = SecretSitesClient.class.toString();

    public AuthToken getAuthToken() {
        return this.authToken;
    }

    private SecretSitesClient() {
        clientConfig = new ClientConfig();
        client = ClientBuilder.newClient(clientConfig);
    }

    public static SecretSitesClient getInstance() {
        if (instance == null)
            instance = new SecretSitesClient();
        return instance;
    }

    public boolean loadRoot() {
        try {
            WebTarget target = client.target(BASE_URI);
            Response response = target.request().get();
            String json = response.readEntity(String.class);
            this.root = (new Gson()).fromJson(json, Root.class);
            return true;
        } catch (ProcessingException ex) {
            ex.printStackTrace();
        }
        this.instance = null;
        return false;
    }

    public String loadRoot(String token) throws SecretSitesClientException {
        try {
            WebTarget target = client.target(BASE_URI);
            Response response = target.request().header("X-Auth-Token", token).get();
            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                this.root = (new Gson()).fromJson(response.readEntity(String.class), Root.class);
                this.authToken = new AuthToken(token);
                return getUser(getLink(this.root.getLinks(), "user-profile").getUri().toString());
            }
            else if(response.getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()) {
                if(loadRoot()) throw new SecretSitesClientException(Resources.getSystem().getString(R.string.authorized));
                else throw new SecretSitesClientException(Resources.getSystem().getString(R.string.server_error));
            }
        } catch (ProcessingException ex) {
            ex.printStackTrace();
            throw new SecretSitesClientException(Resources.getSystem().getString(R.string.server_error));
        }
        this.instance = null;
        throw new SecretSitesClientException(Resources.getSystem().getString(R.string.unexpected_error));
    }

    public final static Link getLink(List<Link> links, String rel) {
        for (Link link : links) {
            if (link.getRels().contains(rel)) {
                return link;
            }
        }
        return null;
    }

    public String login(String userid, String password) throws SecretSitesClientException {
        WebTarget target = client.target(getLink(this.root.getLinks(), "login").getUri().toString());
        Form form = new Form();
        form.param("username", userid);
        form.param("password", password);
        Response response = target.request().post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            this.authToken = new Gson().fromJson(response.readEntity(String.class), AuthToken.class);
            return getUser(getLink(this.authToken.getLinks(), "user-profile").getUri().toString());
        }
        else if (response.getStatus() == Response.Status.BAD_REQUEST.getStatusCode()) {
            throw new Gson().fromJson(response.readEntity(String.class), SecretSitesClientException.class);
        }
        throw new SecretSitesClientException(Resources.getSystem().getString(R.string.unexpected_error));
    }

    public String register(String userid, String password, String email, String fullname) throws SecretSitesClientException {
        WebTarget target = client.target(getLink(this.root.getLinks(), "create-user").getUri().toString());
        Form form = new Form();
        form.param("loginid", userid);
        form.param("password", password);
        form.param("email", email);
        form.param("fullname", fullname);
        Response response = target.request().post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
        if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
            this.authToken = new Gson().fromJson(response.readEntity(String.class), AuthToken.class);
            return getUser(response.getLocation().toString());
        }
        else if(response.getStatus() == Response.Status.CONFLICT.getStatusCode()) {
            throw new Gson().fromJson(response.readEntity(String.class), SecretSitesClientException.class);
        }
        else if(response.getStatus() == Response.Status.BAD_REQUEST.getStatusCode()) {
            throw new Gson().fromJson(response.readEntity(String.class), SecretSitesClientException.class);
        }
        throw new SecretSitesClientException(Resources.getSystem().getString(R.string.unexpected_error));
    }

    public String getUser(String uri) throws SecretSitesClientException {
        WebTarget target = client.target(uri);
        Response response = target.request().header("X-Auth-Token", this.authToken.getToken()).get();
        if (response.getStatus() == Response.Status.OK.getStatusCode())
            return response.readEntity(String.class);
        else {
            throw new Gson().fromJson(response.readEntity(String.class), SecretSitesClientException.class);
        }
    }

    public String getInterestPoints(String searchName) throws SecretSitesClientException {
        WebTarget target = client.target(getLink(this.root.getLinks(), "current-points").getUri().toString());
        Response response;
        if(this.authToken != null)
            response = target.request().header("X-Auth-Token", this.authToken.getToken()).get();
        else
            response = target.request().get();
        if (response.getStatus() == Response.Status.OK.getStatusCode())
            return response.readEntity(String.class);
        else {
            throw new Gson().fromJson(response.readEntity(String.class), SecretSitesClientException.class);
        }
    }

    public String getDetailInterestPoint(String uri) throws SecretSitesClientException {
        WebTarget target = client.target(uri);
        Response response;
        if(this.authToken != null)
            response = target.request().header("X-Auth-Token", this.authToken.getToken()).get();
        else
            response = target.request().get();
        if (response.getStatus() == Response.Status.OK.getStatusCode())
            return response.readEntity(String.class);
        else {
            throw new Gson().fromJson(response.readEntity(String.class), SecretSitesClientException.class);
        }
    }

    public String getImage(String uri) throws SecretSitesClientException {
        WebTarget target = client.target("http://192.168.1.133:8080/secretsites/photos/byUrl/wjfhi45");
        Response response = target.request().get();
        if (response.getStatus() == Response.Status.OK.getStatusCode())
            return response.readEntity(String.class);
        else {
            throw new Gson().fromJson(response.readEntity(String.class), SecretSitesClientException.class);
        }
    }
}
