/**
 * Created by jakefroeb on 9/27/16.
 */
public class Game {
    int id;
    int userId;
    String game;
    String genre;
    String platform;
    int releaseYear;

    public Game(){}

    public Game(int id,int userId, String game, String genre, String platform, int releaseYear) {

        this.id = id;
        this.userId = userId;
        this.genre = genre;
        this.game = game;
        this.platform = platform;
        this.releaseYear = releaseYear;
    }
}
