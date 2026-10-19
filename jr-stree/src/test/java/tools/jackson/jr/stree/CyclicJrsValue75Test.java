package tools.jackson.jr.stree;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import tools.jackson.jr.ob.JSON;
import tools.jackson.jr.ob.JSONObjectException;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class CyclicJrsValue75Test extends JacksonJrTreeTestBase
{
    private final JSON treeJSON = jsonWithTreeCodec();

    @Test
    public void testCyclicObjectReference() throws Exception
    {
        Map<String, JrsValue> stuff = new LinkedHashMap<>();
        JrsObject root = new JrsObject(stuff);
        stuff.put("self", root);

        JSONObjectException e = assertThrows(JSONObjectException.class,
                () -> treeJSON.asString(root));
        verifyException(e, "cycle");
    }

    @Test
    public void testCyclicArrayReference() throws Exception
    {
        java.util.ArrayList<JrsValue> values = new java.util.ArrayList<>();
        JrsArray root = new JrsArray(values);
        values.add(root);

        JSONObjectException e = assertThrows(JSONObjectException.class,
                () -> treeJSON.asString(root));
        verifyException(e, "cycle");
    }
}
