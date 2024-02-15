package tools.jackson.jr.annotationsupport;

import junit.framework.TestCase;

import java.util.Arrays;

import tools.jackson.jr.ob.JSON;

public abstract class ASTestBase extends TestCase
{
    protected static class NameBean {
        protected String first, last;

        public NameBean() { }
        public NameBean(String f, String l) {
            first = f;
            last = l;
        }

        public String getFirst() { return first; }
        public String getLast() { return last; }

        public void setFirst(String n) { first = n; }
        public void setLast(String n) { last = n; }
    }

    protected void verifyException(Throwable e, String... matches)
    {
        String msg = e.getMessage();
        String lmsg = (msg == null) ? "" : msg.toLowerCase();
        for (String match : matches) {
            String lmatch = match.toLowerCase();
            if (lmsg.indexOf(lmatch) >= 0) {
                return;
            }
        }
        fail("Expected an exception with one of substrings ("+Arrays.asList(matches)+"): got one with message \""+msg+"\"");
    }

    protected JSON jsonWithAnnotationSupport() {
        return JSON.builder().register(JacksonAnnotationExtension.std).build();
    }

    /*
    protected JsonParser parserFor(String source) {
        return parserFor(JSON.std, source);
    }

    protected JsonParser parserFor(JSON json, String source) {
        return json.getStreamingFactory().createParser(source.toCharArray());
    }
    */

    protected String quote(String str) {
        return "\"" + str + "\"";
    }

    protected String a2q(String json) {
        return json.replace("'", "\"");
    }
}
