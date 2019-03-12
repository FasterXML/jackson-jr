// Generated 11-Mar-2019 using Moditect maven plugin
module com.fasterxml.jackson.jr.retrofit2 {
    requires transitive com.fasterxml.jackson.jr.ob;
    // 11-Mar-2019, tatu: These are probably not right...
    requires transitive okhttp;
    requires transitive retrofit;

    exports com.fasterxml.jackson.jr.retrofit2;
}
