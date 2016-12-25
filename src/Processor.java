import java.io.*;
import java.net.Socket;

/**
 * Created by Sónia Alves on 10/11/2016
 */
public class Processor implements Runnable {

    Socket clientSocket;

    public Processor(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override

    /**
     * Runs the server.
     */
    public void run() {

        BufferedReader input = null;

        try {

            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            BufferedOutputStream output = new BufferedOutputStream(clientSocket.getOutputStream());
            String request = input.readLine();
            System.out.println("Request received:" + request);

            //Defensive programming.
            if (request == null) {
                clientSocket.close();
                return;
            }

            //Get the verb from the request.
            String verb = request.split(" ")[0];

            //Server only supports Get's.
            if (!verb.equals("GET")) {
                System.out.println("Unsupported Verb: " + verb);
                output.write("HTTP/1.0 405 Method Not Allowed\r\n".getBytes());
                output.write("Allow: GET\r\n".getBytes());
                System.out.println("Closing due to unsupported verb");
                clientSocket.close();
                return;
            }

            //Get the fileName from the request.
            String fileName = request.split(" ")[1];
            File file = getFile(fileName);

            //Check if the file exists and write the corresponding output.
            if (file.exists()) {
                String header = build200Header(file);
                System.out.println(header);
                output.write(header.getBytes());
                output.write(readFile(file));
                output.flush();

            } else {
                file = new File("resources/404.html");
                String header = build404Header(file);
                System.out.println(header);
                output.write(header.getBytes());
                output.write(readFile(file));
                output.flush();
            }

            clientSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Return the file to read.
     */
    private File getFile(String resource) {

        //if the request is "/" subs it for the index.
        if (resource.equals("/")) {
            resource = "/index.html";
        }

        File file = new File("resources" + resource);
        return file;
    }

    /**
     * Reads the file
     * @param file file to read
     * @return the byte array with the content of said file
     */
    private byte[] readFile(File file) {

        byte[] fileInBytes = new byte[(int) file.length()];

        try {
            BufferedInputStream stream = new BufferedInputStream(new FileInputStream(file));
            stream.read(fileInBytes);
            stream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileInBytes;
    }

    /**
     * Builds the Header according to the type and the size.
     * @param file file to read.
     * @return the assembled header.
     */
    private String build200Header(File file) {

        String statusCode = "200";
        String contentType = getMimeType(file);
        long size = file.length();

        return "HTTP/1.0 " + statusCode + " Document Follows" + "\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "Content-Length: " + size + " \r\n" +
                "\r\n";
    }

    /**
     * Builds the Header of the page not found.
     * @param file file to read.
     * @return the assembled header.
     */
    private String build404Header(File file) {

        String statusCode = "404";
        String contentType = getMimeType(file);
        long size = file.length();

        return "HTTP/1.0 " + statusCode + " Not Found" + "\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "Content-Length: " + size + " \r\n" +
                "\r\n";
    }

    /**
     * Get the MIME type, currently supported are the html, image types and the CSS.
     * @param file file to read
     * @return MimeType
     */
    private String getMimeType(File file) {

        String extension = file.getName().substring(file.getName().lastIndexOf(".") + 1);
        String mime = "";

        if (extension.equals("html")) {
            mime = "text/html; charset=UTF-8";
        } else if (extension.equals("png") || extension.equals("gif") || extension.equals("jpeg") || extension.equals("bmp")) {
            mime = "image/" + extension;
        } else if (extension.equals("jpg")) {
            mime = "image/pjpeg";
        } else if(extension.equals("css")) {
            mime = "text/css";
        } else {
            mime = "text/html; charset=UTF-8";
        }
        return mime;
    }
}
