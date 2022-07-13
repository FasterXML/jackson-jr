package tools.jackson.jr.stree;

import java.io.StringWriter;
import java.util.*;

import tools.jackson.core.JsonGenerator;
import tools.jackson.core.ObjectWriteContext;
import tools.jackson.core.TreeCodec;
import tools.jackson.core.TreeNode;

public class WriteViaCodecTest extends JacksonJrTreeTestBase
{
    public void testSimpleList()
    {
        List<JrsValue> stuff = new LinkedList<JrsValue>();
        stuff.add(new JrsString("x"));
        stuff.add(JrsBoolean.TRUE);
        stuff.add(new JrsNumber(123));
        assertEquals("[\"x\",true,123]", writeTree(new JacksonJrsTreeCodec(), new JrsArray(stuff)));
    }

    public void testSimpleMap()
    {
        Map<String,JrsValue> stuff = new LinkedHashMap<String,JrsValue>();
        stuff.put("a", new JrsNumber(15));
        stuff.put("b", JrsBoolean.TRUE);
        stuff.put("c", new JrsString("foobar"));

        assertEquals("{\"a\":15,\"b\":true,\"c\":\"foobar\"}",
                writeTree(new JacksonJrsTreeCodec(), new JrsObject(stuff)));
    }

    public void testNest()
    {
        Map<String,JrsValue> stuff = new LinkedHashMap<String,JrsValue>();
        List<JrsValue> list = new ArrayList<JrsValue>();
        list.add(new JrsNumber(123));
        list.add(new JrsNumber(456));
        stuff.put("first", new JrsArray(list));
        Map<String,JrsValue> second = new LinkedHashMap<String,JrsValue>();
        stuff.put("second", new JrsObject(second));
        second.put("foo", new JrsString("bar"));
        second.put("bar", new JrsArray());

        assertEquals("{\"first\":[123,456],\"second\":{\"foo\":\"bar\",\"bar\":[]}}",
                writeTree(new JacksonJrsTreeCodec(), new JrsObject(stuff)));
    }

    protected String writeTree(TreeCodec treeCodec, TreeNode treeNode)
    {
         StringWriter writer = new StringWriter();
         JsonGenerator g = _factory.createGenerator(ObjectWriteContext.empty(), writer);
         treeCodec.writeTree(g, treeNode);
         g.close();
         return writer.toString();
     }

}
