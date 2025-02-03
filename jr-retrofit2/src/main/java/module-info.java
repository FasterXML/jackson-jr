module tools.jackson.jr.retrofit2
{
    requires transitive tools.jackson.jr.ob;
    // 11-Mar-2019, tatu: These are probably not right...
    requires transitive okhttp3;
    requires transitive retrofit2;

    exports tools.jackson.jr.retrofit2;
}
