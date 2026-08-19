package factory;

import model.ImaxMovie;
import model.Movie;
import model.ThreeDMovie;
import model.TwoDMovie;

public class MovieFactory {

    public static Movie createMovie(String type, String title) {

        switch (type.toUpperCase()) {

            case "2D":
                return new TwoDMovie(title);

            case "3D":
                return new ThreeDMovie(title);

            case "IMAX":
                return new ImaxMovie(title);

            default:
                throw new IllegalArgumentException(
                        "Invalid movie format.");
        }
    }
}