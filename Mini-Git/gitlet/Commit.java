package gitlet;


import java.util.Date;
import java.io.Serializable;
import java.util.HashMap;



/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Varun Nair and Agrima Sharma
 */
public class Commit implements Serializable{
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    private Date timestamp;
    private HashMap<String, String> fileBlob;
    private String parent;

    public Commit(String message, Date timestamp) {
        this.message = message;
        this.timestamp = timestamp;
        this.fileBlob = new HashMap<String, String>();
        this.parent = null;

    }

    public Commit(String message, Date timestamp, HashMap fileBlob, String parent) {
        this.message = message;
        this.timestamp = timestamp;
        this.fileBlob = fileBlob;
        this.parent = parent;
    }

    public String getMessage() {
        return message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public HashMap getFileBlob() {
        return fileBlob;
    }

    public String getParent() {
        return parent;
    }
}
