// Jackson-jr Objects module Main artifact Module descriptor
module tools.jackson.jr.ob
{
    requires transitive tools.jackson.core;

    exports tools.jackson.jr.ob;
    exports tools.jackson.jr.ob.api;
    exports tools.jackson.jr.ob.comp;
    // 11-Mar-2019, tatu: Need to refactor and then close this one
    exports tools.jackson.jr.ob.impl;
    exports tools.jackson.jr.type;
}
