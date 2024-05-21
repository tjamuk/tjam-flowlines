package uk.ac.aber.dcs.cs39440.tjamflowlines.model;

public class Colour {
    private static final String[] colourToBackground = {
            "\033[48;5;0m",
            "\033[48;5;1m",
            "\033[48;5;2m",
            "\033[48;5;3m",
            "\033[48;5;4m",
            "\033[48;5;5m",
            "\033[48;5;6m",
            "\033[48;5;7m",
            "\033[48;5;8m",
            "\033[48;5;9m",
            "\033[48;5;10m",
            "\033[48;5;11m",
            "\033[48;5;12m",
            "\033[48;5;13m",
            "\033[48;5;14m",
            "\033[48;5;15m",
            "\033[48;5;47m",
            "\033[48;5;208m",
            "\033[48;5;181m",
            "\033[48;5;228m",
            "\033[48;5;218m",
    };

    public static String getBackgroundFromOrdinal(int ordinal)
    {
        return colourToBackground[ordinal];
    }
}
