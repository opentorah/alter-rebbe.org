See the project's website: [http://www.alter-rebbe.org](http://www.alter-rebbe.org).

The site can be served locally by running the following command in `docs`:
```
  $ bundle exec jekyll serve
```

To rebuild collection indices and verify name references:
```
  $ ./gradlew clean build
```

To cut facsimiles:
```
  $ convert xxx.tif -crop 2x1+120@ x-%d.tif
```

To compress facsimiles:
```
  $ mogrify -path out -quality 80% -format jpg *.tif
```
