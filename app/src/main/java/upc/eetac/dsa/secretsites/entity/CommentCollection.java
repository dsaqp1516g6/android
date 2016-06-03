package upc.eetac.dsa.secretsites.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marti on 28/05/2016.
 */
public class CommentCollection {

    private List<Link> links;
    private long newestTimestamp;
    private long oldestTimestamp;
    private List<Comment> comments = new ArrayList<>();

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public long getNewestTimestamp() {
        return newestTimestamp;
    }

    public void setNewestTimestamp(long newestTimestamp) {
        this.newestTimestamp = newestTimestamp;
    }

    public long getOldestTimestamp() {
        return oldestTimestamp;
    }

    public void setOldestTimestamp(long oldestTimestamp) {
        this.oldestTimestamp = oldestTimestamp;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
}
