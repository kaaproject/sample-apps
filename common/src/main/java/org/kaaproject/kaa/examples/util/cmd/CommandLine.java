package org.kaaproject.kaa.examples.util.cmd;

public enum CommandLine {
    TAR("tar "),
    CP("cp ");

    private String command;

    CommandLine(String command) {
        this.command = command;
    }

    public String createCommand(String params) {
        return command + " " + params;
    }
}