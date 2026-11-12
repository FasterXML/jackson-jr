package tools.jackson.jr.ob;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class CyclicBean75Test extends TestBase
{
    static class SelfRefBean {
        public SelfRefBean child;

        public SelfRefBean getChild() {
            return child;
        }

        public void setChild(SelfRefBean c) {
            child = c;
        }
    }

    @Test
    public void testDirectSelfReference() throws Exception
    {
        SelfRefBean bean = new SelfRefBean();
        bean.child = bean;

        JSONObjectException e = assertThrows(JSONObjectException.class,
                () -> JSON.std.asString(bean));
        verifyException(e, "cycle");
    }

    @Test
    public void testIndirectCycle() throws Exception
    {
        SelfRefBean a = new SelfRefBean();
        SelfRefBean b = new SelfRefBean();
        a.child = b;
        b.child = a;

        JSONObjectException e = assertThrows(JSONObjectException.class,
                () -> JSON.std.asString(a));
        verifyException(e, "cycle");
    }
}
