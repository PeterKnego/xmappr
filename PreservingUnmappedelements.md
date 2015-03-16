

# Preserving unmapped XML elements #

Xmappr supports two basic strategies for preserving unmapped nodes: first is using element storage and the second is mapping to `DomElements`.

## Using element storage ##

When handling complex XML data, especially coming from 3rd parties, it's sometimes easier to map just a part of the XML, while still retaining all other (unmapped) XML elements when writing XML. This is one of the Xmappr's strong points.
For example, given this XML:

```
<customer>
  <contact>
    <firstname>Joe</firstname>
    <lastname>Somebody</lastname>
  </contact>
  <address>
    <street>Streetname</street>
    <city>Big City</city>
  </address>
</customer>
```

you just want to map a part of it to this classes:

```
@RootElement
class Customer{
  @Element 
  Contact contact;
}

class Contact{
  @Element
  String firstName;
  @Element
  String lastName;
}
```

Normally `<address>` would not be mapped and would be skipped when reading XML and would not appear on output. This can be avoided by using `xmapper.fromXMLwithUnmapped(reader)`:

```
// reading XML while storing unmapped nodes
Xmappr xmapper = new Xmappr(Customer.class);
Result result = xmapper.fromXMLwithUnmapped(reader);
Customer customer = result.getObject();

// writing back with unmapped nodes
xmapper.toXML(customer, result.getStore(), writer)
```

The `Result` will contain an `ObjectStore` which is a compact binary storage for unmapped elements. When writing out the XML you have to provide the `Result` in order for unmapped elements to be retrieved and written out.

`ObjectStore` stores whole element trees including subelements, attributes and text. In this case the element would be `<address>` and all it's subnodes.

**LIMITATIONS:** This technique has two limitations:
  1. `ObjectStore` contains weak references to your object (customer) in order to know where to restore unmapped XML elements. This means that you must not manipulate your object such that object identity would be changed (new instance creation/ replacing with other instance). This limitation only applies to those objects where elements would be restored. You can still change content/references in all objects that do not "contain" unmapped elements. This automatically applies to all leaf objects (at the edge of object tree).
  1. `ObjectStore` currently only stores whole XML element trees with all XML subelements, their XML attributes and XML text. However it does not store XML attributes or XML text in elements that are explicitly mapped: This means that you must explicitly map all such XML attibutes and text in order to be reserved from input to output.

An example to demonstrate point 2.:
```
<customer isactive="true">
  <contact>
    <firstname>Joe</firstname>
    <lastname>Somebody</lastname>
  </contact>
  <address pobox="1234">
    <street>Streetname</street>
    <city>Big City</city>
  </address>
</customer>
```

mapped to the classes:

```
@RootElement
class Customer{
  @Element 
  Contact contact;
}

class Contact{
  @Element
  String firstName;
  @Element
  String lastName;
}
```

In this case whole `<address>` element would be stored, including attribute `pobox="1234"` and subelements `<street>` and `<city>`.
However, since class `Customer` is explicitly mapped, attribute `isactive="true"` must be also explicitly mapped in order to be preserved. In the given case it's not mapped and would be hence lost.

## Mapping to `DomElement` ##

`DomElement` is a special class that requires you to map it to one of your fields. `DomElement` uses a special converter that will convert whole element subtree and store it in `DomElement` instance. For example, mapping the XML:

```
<customer>
  <contact>
    <firstname>Joe</firstname>
    <lastname>Somebody</lastname>
  </contact>
  <address>
    <street>Streetname</street>
    <city>Big City</city>
  </address>
</customer>
```

using `DomElement`:

```
@RootElement
class Customer{
  @Element 
  Contact contact;

  @Element
  DomElement address;
}

class Contact{
  @Element
  String firstName;
  @Element
  String lastName;
}
```

The `address` field references a `DomElement` instance which stores the whole `<address>` tree.

As such `DomElement` is similar to DOM parsers. You can inspect and change it's content. However it does not support XPath style searching.

```
customer.address.getElements().size() == 2; // true, <address> has two subelements
```

Read more about `DomElement` in it's [javadoc](http://xmappr.googlecode.com/svn/tags/0.9.0/apidocs/org/xmappr/DOMelement.html)

# Gradual development #

When you stand before a task to parse a complex XML, usually generated externally, it's hard to do the mapping of 100's of XML elements all in one step. At the same time if you do it gradually, a few XML elements at the time, it's hard to test if the mapping is right, because the mapping will not produce the same XML that you have on the input.

This is where `DomElement` comes to rescue. It let's you begin with mapping just a few elements to your classes, while preserving the rest by mapping them to `DomElement`.
A simple example to show the point:

```
<root>
  <one>1</one>
  <two>2</two>
  <three>3</three>
</root>
```

If you'd like to start by mapping only `<one>` and preserving the rest:

```
@RootElement
class Root{
  Element
  One one;

  @Element("*")
  List preservedElements;
}
```

`@Element` mapping with a wildcard name `"*"` will catch all subelements that are not otherwise mapped and will map them to a `DomElement` instance. So `preservedElements` list will contain two `DomElement` instances: one for `<two>` and the other for `<three>`.

As a next step you could add a new field named `two` to the `Root` class:

```
@RootElement
class Root{
  @Element
  One one;

  @Element
  Two two; 

  @Element("*")
  List preservedElements;
}
```

Here both `<one>` and `<two>` are already mapped to your classes, while `<three>` will be handled by the wildcard handler and mapped to the `DomElement` instance.

This way you can map the XML gradually, adding and mapping your own classes as your code grows and at the same time preserving other unmapped elements.

**NOTE:** If you need elements to retain their original order then follow instructions in [PreservingElementOrder](PreservingElementOrder.md).