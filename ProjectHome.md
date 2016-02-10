GIN (GWT INjection) brings automatic dependency injection to [Google Web Toolkit](http://code.google.com/webtoolkit) client-side code. GIN is built on top of [Guice](http://code.google.com/p/google-guice) and uses (a subset of) Guice's binding language. (See GuiceCompatibility for details.) By using GWT's compile-time `Generator` support, GIN has little-to-no runtime overhead compared to manual DI.

GIN 2.1.2 requires GWT 2.5 or higher and Guice 3.0.

## Status ##
GIN is substantially feature-complete, well-tested, and in production use by many teams at Google. GIN 2.1.2 was released in November 2013. You can [download](http://code.google.com/p/google-gin/downloads/detail?name=gin-2.1.2.zip) the release, check out the [source](http://code.google.com/p/google-gin/source/checkout) and build (`ant dist`) or add a [dependency](http://code.google.com/p/google-gin/issues/detail?id=45#c35) if you use maven.

## Getting started ##
Want to get started using GIN? Take a look at the GinTutorial and see the [code samples](http://code.google.com/p/google-gin/source/browse/#svn%2Ftags%2F2.1.2%2Fsamples).
