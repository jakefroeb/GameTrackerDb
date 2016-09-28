import org.h2.tools.Server;
import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by jakefroeb on 9/27/16.
 */
public class Main {
    static HashMap<String, User> users = new HashMap<>();


    public static void main(String[] args) throws SQLException {
        Server.createWebServer().start();
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS games (id IDENTITY, userId Int, gameName VARCHAR, genre VARCHAR, platform VARCHAR, releaseYear INT)");


        Spark.init();

        Spark.get("/",
                ((request, response) -> {
                    Session session = request.session();
                    String name = session.attribute("userName");
                    User user = users.get(name);

                    HashMap m = new HashMap();
                    if (user == null) {
                        return new ModelAndView(m, "login.html");
                    } else {
                        ArrayList<Game> games = selectGames(conn, user);
                        m.put("games",games);
                        return new ModelAndView(m, "home.html");
                    }
                }),
                new MustacheTemplateEngine());

        Spark.get("/editGame.html",
                ((request, response) -> {
                    HashMap m = new HashMap();
                    Session session = request.session();
                    Game game = session.attribute("game");
                    // int index = session.attribute("index");
                    String userName = session.attribute("userName");
                    m.put("game", game);
                    return new ModelAndView(m, "/editGame.html");
                }), new MustacheTemplateEngine());

        Spark.post("/login",
                ((request, response) -> {
                    String name = request.queryParams("loginName");
                    User user = users.get(name);
                    if (user == null) {
                        users.put(name, new User(name));
                    }
                    Session session = request.session();
                    session.attribute("userName", name);
                    response.redirect("/");
                    return new ModelAndView(user, "home.html");
                }));

        Spark.post("/logout",
                ((request, response) -> {
                    Session session = request.session();
                    session.invalidate();
                    response.redirect("/");
                    return "";
                }));

        Spark.post("/create-game",
                ((request, response) -> {
                    Session session = request.session();
                    String name = session.attribute("userName");
                    User user = users.get(name);
                    if (user == null) {
                        throw new Exception("user is not logged in");
                    }
                    String gameGenre = request.queryParams("gameGenre");
                    String gameName = request.queryParams("gameName");
                    String gamePlatform = request.queryParams("gamePlatform");
                    int gameYear = Integer.parseInt(request.queryParams("gameYear"));
                    insertGame(conn, user.userId, gameName, gameGenre, gamePlatform, gameYear);
                    response.redirect("/");
                    return "";
                }));
        Spark.post("/delete-game",
                ((request, response) -> {
                    String id = request.queryParams("id");
                    if (id == null) {

                    } else {
                        deleteGame(conn, Integer.parseInt(id));
                    }
                    response.redirect("/");
                    return "";
                }));
        Spark.post("/edit-game",
                ((request, response) -> {
                    Session session = request.session();
                    int id = Integer.parseInt(request.queryParams("id"));
                    Game game = selectGame(conn, id);
                    session.attribute("id", id);
                    session.attribute("game", game);
                    response.redirect("/editGame.html");
                    return "";
                }));
        Spark.post("/editGame",
                ((request, response) -> {
                    Session session = request.session();
                    Game game = session.attribute("game");
                    String gameName = request.queryParams("gameName");
                    String genre = request.queryParams("genre");
                    String platform = request.queryParams("platform");
                    String releaseYear = request.queryParams("gameYear");
                    if (gameName != null && gameName.length() > 0) {
                        game.game = gameName;
                    }
                    if (genre != null && genre.length() > 0) {
                        game.genre = genre;
                    }
                    if (platform != null && platform.length() > 0) {
                        game.platform = platform;
                    }
                    if (releaseYear != null && releaseYear.length() > 0) {
                        game.releaseYear = Integer.parseInt(releaseYear);
                    }
                    updateGame(conn, game);
                    response.redirect("/");
                    return "";
                }));

    }

    public static void insertGame(Connection conn, int userId, String gameName, String genre, String platform, int releaseYear) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO games VALUES(null, ?,?,?,?,?)");
        stmt.setInt(1, userId);
        stmt.setString(2, gameName);
        stmt.setString(3, genre);
        stmt.setString(4, platform);
        stmt.setInt(5, releaseYear);
        stmt.execute();
    }

    public static Game selectGame(Connection conn, int idNum) throws SQLException {
        Game game = new Game();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM games WHERE id = ?");
        stmt.setInt(1, idNum);
        ResultSet results = stmt.executeQuery();
        while(results.next()) {
            int id = results.getInt("id");
            int userId = results.getInt("userId");
            String gameName = results.getString("gameName");
            String genre = results.getString("genre");
            String platform = results.getString("platform");
            int releaseYear = results.getInt("releaseYear");
            game = new Game(id, userId, gameName, genre, platform, releaseYear);
        }
        return game;
    }


    public static ArrayList<Game> selectGames(Connection conn, User user) throws SQLException {
        ArrayList<Game> games = new ArrayList<>();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM games WHERE userId = ?");
        stmt.setInt(1, user.userId);
        ResultSet results = stmt.executeQuery();
        while (results.next()) {
            int id = results.getInt("id");
            String gameName = results.getString("gameName");
            String genre = results.getString("genre");
            String platform = results.getString("platform");
            int releaseYear = results.getInt("releaseYear");
            games.add(new Game(id, user.userId, gameName, genre, platform, releaseYear));
        }
        return games;
    }

    public static void deleteGame(Connection conn, int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM games WHERE id=?");
        stmt.setInt(1, id);
        stmt.execute();
    }

    public static void updateGame(Connection conn, Game game) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("UPDATE games SET gameName=?, genre=?, platform=?, releaseYear=? WHERE id = ?");
        stmt.setString(1, game.game);
        stmt.setString(2, game.genre);
        stmt.setString(3, game.platform);
        stmt.setInt(4, game.releaseYear);
        stmt.setInt(5, game.id);
        stmt.execute();
    }
}
