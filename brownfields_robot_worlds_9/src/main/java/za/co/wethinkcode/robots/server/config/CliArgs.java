package za.co.wethinkcode.robots.server.config;

import picocli.CommandLine;

public class CliArgs {

    @CommandLine.Option(names = "-p")
    private Integer port;

    @CommandLine.Option(names = "-s")
    private Integer worldSize;

    @CommandLine.Option(names = "-o")
    private String obstacle;

    public Integer getPort() {
        return port;
    }

    public Integer getWorldSize() {
        return worldSize;
    }

    public String getObstacle() {
        return obstacle;
    }
}
