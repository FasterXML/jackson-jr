module com.fasterxml.jackson.jr.retrofit2 {
    requires transitive com.fasterxml.jackson.jr.ob;

    // 02-Feb-2025, tatu: Automatic-Module-Names:
    requires transitive okhttp3;
    requires transitive retrofit2;

    exports com.fasterxml.jackson.jr.retrofit2;
}
