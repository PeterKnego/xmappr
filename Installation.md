# Installation #

Xmappr is a simple java jar so there is no need for installation.

It's best to use Xmappr via maven. Add this snippet to your pom.xml file:

```
<repository>
  <id>xmappr-repo-googlecode</id>
  <name>Xmappr Repository</name>
  <url>http://xmappr.googlecode.com/svn/repo/</url>
</repository>
```
Then also add the dependency:
```
<dependency>
  <groupId>org.xmappr</groupId>
  <artifactId>xmappr</artifactId>
  <version>0.9.0</version>
</dependency>
```

If you don't use maven, then just [download](Downloads.md) the jar amd put it on your classpath.