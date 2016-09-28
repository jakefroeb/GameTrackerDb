import java.util.ArrayList;

/**
 * Created by jakefroeb on 9/27/16.
 */
public class User {
    static int baseId = 1;
    String name;
    int userId;

    public User(String name) {
        this.name = name;
        userId = baseId++;
    }
}
