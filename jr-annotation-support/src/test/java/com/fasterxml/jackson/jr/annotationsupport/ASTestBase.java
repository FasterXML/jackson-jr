package com.fasterxml.jackson.jr.annotationsupport;

import java.util.Arrays;

import com.fasterxml.jackson.jr.ob.JSON;

import junit.framework.TestCase;

public abstract class ASTestBase extends TestCase
{
    protected enum ABC { A, B, C; }

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
        return JSON.builder().register(JacksonAnnotationExtension.builder()
                .build()
        ).build();
    }

    /*
    protected JsonParser parserFor(String source) throws IOException {
        return parserFor(JSON.std, source);
    }

    protected JsonParser parserFor(JSON json, String source) throws IOException {
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
