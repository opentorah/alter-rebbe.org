See the project's website: [https://www.alter-rebbe.org/note/about.html](https://www.alter-rebbe.org/note/about.html).

```
./gradlew generateSite
./gradlew serveSite
```

`generateSite` writes `_site` (GitHub Pages). A local site-publisher checkout at
`../../Podval/site-publisher` is used when present (`-PsitePublisherDir=`); CI
resolves `org.podval.tools:org.podval.tools.publisher` from Maven Central.

