public enum Command {
    CD,
    LS,
    MKDIR,
    UPLOAD,
    DOWNLOAD,
    DELETE,
    EXIT;

    @Override
    public String toString() {
        return name().toLowerCase();
    }

    public static Command fromString(String input) {
        for (Command c : Command.values()) {
            if (c.toString().equalsIgnoreCase(input)) {
                return c;
            }
        }

        throw new IllegalArgumentException("Unknown command: " + input);
    }
}
