# Preserving order of XML elements #

XML elements are ordered and this order may be significant. An example is XHTML. Unfortunately Java classes, and the fields within them, are not ordered. One way to retain order is to use `java.util.Collection`.

When XML elements and text are mapped to a `java.util.Collection` the order will be preserved:

```
<root>
  <one>1</one>
  first text
  <two>2</two>
  second text
</root>
```

mapped to:

```
@RootElement
class Root{

  @Element(name="*", targetType=Integer.class)
  @Text
  List elements;
}
```

When the XML is written out, XML elements and text would have the same order is in input document.
