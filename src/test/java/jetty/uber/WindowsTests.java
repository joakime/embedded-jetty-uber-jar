package jetty.uber;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.eclipse.jetty.http.HttpTester;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.LifeCycle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WindowsTests
{
    private Server server;

    @BeforeEach
    public void startServer() throws Exception
    {
        server = new ServerMain().createServer(0);
        server.start();
    }

    @AfterEach
    public void endServer()
    {
        LifeCycle.stop(server);
    }

    private static Socket newSocket(String host, int port) throws Exception
    {
        Socket socket = new Socket(host, port);
        socket.setSoTimeout(10000);
        socket.setTcpNoDelay(true);
        return socket;
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "/..\\..\\..\\..\\..\\..\\..\\..\\..\\..\\..\\..\\Windows\\win.ini",
        "/..\\..\\..\\..\\..\\..\\..\\..\\..\\..\\..\\Windows\\win.ini",
        "/..\\..\\..\\..\\..\\..\\..\\..\\..\\..\\Windows\\win.ini",
        "/..\\..\\..\\..\\..\\..\\..\\..\\..\\Windows\\win.ini",
        "/..\\..\\..\\..\\..\\..\\..\\..\\Windows\\win.ini",
        "/..\\..\\..\\..\\..\\..\\..\\Windows\\win.ini",
        "/..\\..\\..\\..\\..\\..\\Windows\\win.ini",
        "/..\\..\\..\\..\\..\\Windows\\win.ini",
        "/..\\..\\..\\..\\Windows\\win.ini",
        "/..\\..\\..\\Windows\\win.ini",
        "/..\\..\\Windows\\win.ini",
        "/..\\Windows\\win.ini",
        "/\\Windows\\win.ini",
    })
    public void testWindowsPathTraversal(String requestPath) throws Exception
    {
        URI serverURI = server.getURI();

        try (Socket client = newSocket(serverURI.getHost(), serverURI.getPort()))
        {
            client.setSoTimeout(10000);
            try (OutputStream os = client.getOutputStream();
                 InputStream is = client.getInputStream())
            {
                os.write(("GET " + requestPath + " HTTP/1.1\r\n" +
                    "Host: " + serverURI.getHost() + ":" + serverURI.getPort() + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n").getBytes(StandardCharsets.UTF_8));
                os.flush();

                HttpTester.Response response = HttpTester.parseResponse(is);
                assertEquals(404, response.getStatus(), () -> response.toString() + "\n" + response.getContent());
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "/index.html",
        "/INDEX.HTML",
        "/css/main.css",
        "/CSS/main.css",
        "/css/MAIN.css",
    })
    public void testWindowsCaseInsensitive(String requestPath) throws Exception
    {
        URI serverURI = server.getURI();

        try (Socket client = newSocket(serverURI.getHost(), serverURI.getPort()))
        {
            client.setSoTimeout(10000);
            try (OutputStream os = client.getOutputStream();
                 InputStream is = client.getInputStream())
            {
                os.write(("GET " + requestPath + " HTTP/1.1\r\n" +
                    "Host: " + serverURI.getHost() + ":" + serverURI.getPort() + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n").getBytes(StandardCharsets.UTF_8));
                os.flush();

                HttpTester.Response response = HttpTester.parseResponse(is);
                assertEquals(200, response.getStatus(), () -> response.toString() + "\n" + response.getContent());
            }
        }
    }
}

