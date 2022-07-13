module tools.jackson.jr.stree {
    requires transitive tools.jackson.core;
    requires tools.jackson.jr.ob;
    requires tools.jackson.jr.ob.api;

    exports tools.jackson.jr.stree;
    exports tools.jackson.jr.stree.util;
}
