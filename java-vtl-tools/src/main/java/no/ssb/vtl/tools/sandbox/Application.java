package no.ssb.vtl.tools.sandbox;

import io.termd.core.http.netty.NettyWebsocketTtyBootstrap;
import io.termd.core.telnet.netty.NettyTelnetTtyBootstrap;
import no.ssb.vtl.tools.termd.TtyConsole;

import java.util.concurrent.TimeUnit;

/**
 * A console application
 */
public class Application {

    public synchronized static void main(String[] args) throws Exception {
        NettyTelnetTtyBootstrap bootstrap = new NettyTelnetTtyBootstrap().
                setHost("localhost").
                setPort(4000);
        bootstrap.start(new TtyConsole()).get(10, TimeUnit.SECONDS);
        System.out.println("Telnet server started on localhost:4000");

        NettyWebsocketTtyBootstrap bootstrapWs = new NettyWebsocketTtyBootstrap().setHost("localhost").setPort(8080);
        bootstrapWs.start(new TtyConsole()).get(10, TimeUnit.SECONDS);
        System.out.println("Web server started on localhost:8080");
        Application.class.wait();

        Application.class.wait();


    }

}