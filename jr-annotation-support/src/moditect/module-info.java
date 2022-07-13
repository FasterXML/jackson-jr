module com.fasterxml.jackson.jr.annotationsupport {
    requires tools.jackson.core;

    requires com.fasterxml.jackson.jr.ob;
    requires com.fasterxml.jackson.jr.ob.api;
    requires com.fasterxml.jackson.jr.ob.impl;

    exports com.fasterxml.jackson.jr.annotationsupport;
}
