&lt;wiki:gadget url="http://www.ohloh.net/p/xmappr/widgets/project\_partner\_badge.xml" height="55" border="0" /&gt;

# Xmappr is the simplest way to handle XML in Java. #

**Elevator pitch:** It's a lightweight library for mapping arbitrary XML to your Java classes, mapped via annotations or external config, understands XML namespaces, can cache unmapped elements, preserves order of XML elements, it's extensible with custom converters, it's thread-safe and is lightweight at under 80kb with no external dependencies. Oh, and it has a permissive license: BSD.

## More formally: ##

  * **Simple configuration.** Object-to-XML mappings are configured via Java annotations or external XML configuration.
  * **Partial mapping.** Map only a part of XML document that you are interested in. Unmapped XML will be preserved on output.
  * **XML namespaces** are fully supported.
  * **Preserves order** of XML elements from input to output.
  * **XML element and text mixing** supported. Can map XHTML.
  * **Extensible.** Custom converters can be written to support custom type conversions.
  * **Lightweight.** Only 80kb in size with no dependencies.
  * **Thread-safe.** Designed for multi-thread use.
  * **Permissive license:** xmappr is released under BSD license.

## Quick example ##

Let’s map some xml:

```
<root a="2.2">
    some text
    <node>123</node>
</root>
```

to this class:

```
@RootElement
public class Root {

    @Attribute
    public float a;

    @Element
    public Integer node;

    @Text
    public String text;
}
```
All you have to do is add `@Elemet, @Attribute and @Text` annotations to your classes and configuration is done.

Mapping is than done in just two lines of code:

```
Xmappr xm = new Xmappr(Root.class);
Root root = (Root) xm.fromXML(reader);
```

If you don't like annotations you can configure mappings via XML configuration. For the given example it's:
```
<root-element name="root" class="package.name.Root">
  <attribute field="a"/>
  <element field="node"/>
  <text field="text"/>
</root-element>
```

Now head on to [documentation](QuickStart.md) and start hacking. You won’t regret it!