{
    "_links" : {
        "curie" : { "href" : "www.example.com/rels/{rel}", "name" : "test", "templated" : true },
        "self" : { "href" : "/blog-posts/123" },
        "test:author" : { "href" : "/people/alan-watts" }
    },
    "comments" : [ {
                       "time" : 1393818110825,
                       "author" : "Gene",
                       "text" : "Nice!"
                   }, {
                       "time" : 1234567890,
                       "author" : "Roger",
                       "text" : "Whoa!"
                   }, {
                       "time" : null,
                       "author" : null,
                       "text" : null
                   } ],
    "id" : "123",
    "_embedded" : {
        "test:author" : {
            "_links" : {
                "self" : { "href" : "/people/alan-watts" }
            },
            "born" : "January 6, 1915",
            "died" : "November 16, 1973",
            "name" : "Alan Watts"
        }
    }
}