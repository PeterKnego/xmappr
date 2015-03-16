

## I have a question. Where can I submit it? ##

Please use [mailing list](http://groups.google.com/group/xmappr) to ask questions.

## Which JDK is required to use Xmappr? ##

Xmappr uses Java annotations for configuration, so the minimum supported JDK version is 1.5.

## What are Xmappr’s dependencies? ##

Xmappr only needs StAX compliant XML parser (javax.xml.stream). While Java 1.6 already includes it, you have to supply your own when using Java 5. Xmappr has been tested with [Woodstox](http://woodstox.codehaus.org/) and recommends it.