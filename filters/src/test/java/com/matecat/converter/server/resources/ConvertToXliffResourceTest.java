package com.matecat.converter.server.resources;

import com.matecat.converter.server.JSONResponseFactory;
import com.matecat.converter.server.MatecatConverterServer;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.Path;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Base64;

import static org.junit.Assert.*;


public class ConvertToXliffResourceTest {

    private static MatecatConverterServer server;
    private static final int PORT = 8090;
    private static final String url = "http://localhost:" + PORT + ConvertToXliffResource.class.getAnnotation(Path.class).value();

    @Before
    public void setUp() throws Exception {
        server = new MatecatConverterServer(PORT);
        while ( !server.isStarted() )
            Thread.sleep(100);
    }

    @Test
    public void testConvertSuccess() throws Exception {

        File fileToUpload = new File(getClass().getResource("/server/test.docx").getPath());

        // Send request
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);
        FileBody uploadFilePart = new FileBody(fileToUpload);
        MultipartEntity reqEntity = new MultipartEntity();
        reqEntity.addPart("documentContent", uploadFilePart);
        reqEntity.addPart("sourceLocale", new StringBody("en-US"));
        reqEntity.addPart("targetLocale", new StringBody("fr-FR"));
        httpPost.setEntity(reqEntity);
        HttpResponse response = httpclient.execute(httpPost);

        // Check OK status code
        assertEquals(200, response.getStatusLine().getStatusCode());

        // Check body
        String body = new BufferedReader(new InputStreamReader(response.getEntity().getContent())).readLine();
        JSONObject json = (JSONObject) new JSONParser().parse(body);

        // Is success
        boolean isSuccess = (boolean) json.get(JSONResponseFactory.IS_SUCCESS);
        assertTrue(isSuccess);

        // No error message
        String error = (String) json.getOrDefault(JSONResponseFactory.ERROR_MESSAGE, "");
        assertEquals("", error);

        // Encoded document
        String doc = (String) json.get(JSONResponseFactory.XLIFF_CONTENT);
        assertNotSame("", doc);

        File out = new File(fileToUpload.getPath() + ".xlf");
        FileUtils.writeStringToFile(out, doc);

    }

    @After
    public void tearDown() throws Exception {
        server.stop();
        while (!server.isStopped())
            Thread.sleep(100);
    }
}