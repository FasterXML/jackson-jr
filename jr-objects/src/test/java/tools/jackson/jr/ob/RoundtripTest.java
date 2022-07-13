package tools.jackson.jr.ob;

import java.io.*;

import tools.jackson.jr.ob.JSON;

public class RoundtripTest extends TestBase
{
    public void testSimple() throws Exception
    {
        MediaItem input = buildItem();
        String json = JSON.std.asString(input);

        // Let's exercise multiple styles of input sources
        
        assertEquals(json, _readWrite(json));
        byte[] b = json.getBytes("UTF-8");
        assertEquals(json, _readWrite(b));
        assertEquals(json, _readWrite(new ByteArrayInputStream(b)));
        assertEquals(json, _readWrite(new StringReader(json)));
        assertEquals(json, _readWrite(json.toCharArray()));
    }

    public void testWithNull() throws Exception
    {
        MediaItem input = new MediaItem(null);
        String json = JSON.std.asString(input);
        assertEquals(json, _readWrite(json));
    }

    private String _readWrite(Object json) throws Exception
    {
        MediaItem item = JSON.std.beanFrom(MediaItem.class, json);
        return JSON.std.asString(item);
    }

    private MediaItem buildItem() {
        MediaItem.Content content = new MediaItem.Content();
        content.setUri("http://javaone.com/keynote.mpg");
        content.setTitle("Javaone Keynote");
        content.setWidth(640);
        content.setHeight(480);
        content.setFormat("video/mpg4");
        content.setDuration(18000000);
        content.setSize(58982400L);
        content.setBitrate(262144);
        content.setPlayer(MediaItem.Player.JAVA);
        content.setCopyright("None");
        content.addPerson("Bill Gates");
        content.addPerson("Steve Jobs");

        MediaItem item = new MediaItem(content);
        item.addPhoto(new MediaItem.Photo("http://javaone.com/keynote_large.jpg", "Javaone Keynote",
                1024, 768, MediaItem.Size.LARGE));
        item.addPhoto(new MediaItem.Photo("http://javaone.com/keynote_small.jpg", "Javaone Keynote",
                320, 240, MediaItem.Size.SMALL));
        return item;
    }
}
