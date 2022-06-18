package org.example.server;

import com.google.common.primitives.Bytes;
import lombok.Setter;
import org.example.server.exception.BadRequestException;
import org.example.server.exception.DeadLineExceedEcxeption;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Setter
public class Server {

    public static final byte[] CRLFCRLF = {'\r', '\n', '\r', '\n'};
    public static final byte[] CRLF = {'\r', '\n'};
    private  int port = 9999;
    private  int soTimeout = 1000;
    private  int readTimeout = 60 * 1000;
    private  int bufferSize = 4096;

    private final Map<String, Handler> routes = new HashMap<>();

    public void start() {
        try (
                final ServerSocket serverSocket = new ServerSocket(port)
        ) {
            while (true) {
                // блокирующий вызов
                try {
                    final Socket socket = serverSocket.accept();
                    handleClient(socket);

                    //  socket.getInputStream().read();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (
                IOException e) {
            //если не удалось запустить сервер
            e.printStackTrace();
        }

    }

    private void handleClient(final Socket socket) throws Exception {
        try (
                socket; // закрывает ресурс
                final OutputStream out = socket.getOutputStream();
                final InputStream in = socket.getInputStream()
        ) {
            socket.setSoTimeout(30 * soTimeout);
            System.out.println(socket.getInetAddress());


            //внутренный цикл
            final Request request = readRequest(in);
            System.out.println("request = " + request);

            final Handler handler = routes.get(request.getPath());

            if (handler == null) {
                final String response = "HTTP/1.1 404 Not Found\r\n" +
                        "Connection: close\r\n" +
                        "Content-Length: 9\r\n" +
                        "\r\n" +
                        "Not Found";
                out.write(response.getBytes(StandardCharsets.UTF_8));
                return;
            }
            handler.handle(request, out);
        }
    }

    private Request readRequest(final InputStream in) throws IOException {
        final byte[] buffer = new byte[bufferSize];
        int offset = 0;
        int length = buffer.length;

        //
        final Instant deadline = Instant.now().plusMillis(readTimeout);


        while (true) {
            if (Instant.now().isAfter(deadline)) {
                throw new DeadLineExceedEcxeption();
            }
            final int read = in.read(buffer, offset, length); // read - сколько байт было прочитано
            offset += read;
            length = buffer.length - offset;

            final int headersEndIndex = Bytes.indexOf(buffer, CRLFCRLF);
            if (headersEndIndex != -1) {
                break;

            }
            if (read == -1) {
                throw new BadRequestException("CRLFCRLF not found, more data");
            }

            if (length == 0) {
                throw new BadRequestException("CRLFCRLF not found");
            }
        }
        final Request request = new Request();
        final int requestLineEndIndex = Bytes.indexOf(buffer, CRLF);
        if (requestLineEndIndex == -1){
            throw  new BadRequestException("Request Line not found");
        }
        final String requestLine = new String(buffer, 0, requestLineEndIndex, StandardCharsets.UTF_8);
        System.out.println("requestLine = " + requestLine);

          final String [] parts  = requestLine.split(" ");
          request.setMethod(parts[0]);
          request.setPath(parts[1]);

        return request;

    }


    public void register(String path, Handler handler) {
        routes.put(path, handler);

    }
}
