package upc.eetac.dsa.secretsites.entity;

import java.util.List;

/**
 * Created by Marti on 11/05/2016.
 */
public class AuthToken {

    private List<Link> links;
    private String userid;
    private String token;

    public AuthToken(String token) {
        this.token = token;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
