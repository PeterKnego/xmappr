# Default values #

Default values of XML elements and XML attributes can be defined in mapping configuration:

```
@RootElement
class Root {

    @Attribute(defaultValue = "1")
    public int one;

    @Element
    public String a;

    @Element(defaultValue = "2")
    public int b;

    @Element(defaultValue = "3")
    public Integer c;
}
```

when consuming this XML:

```
<root>
    <a>some value</a>
    </b>
</root>
```

the following would all be true:

```
root.one == 1 // Attribute one is missing, so default value 1 is used.

root.b == 2 // Element <b> is empty, so default value 2 is used.
root.c == null // Default value is 3, but element <c> does not exist

root.a.equals("some value") // Just a normal element with no default value
```

Rule of thumb is: default attribute values apply when attributes are missing, and default element values apply when elements are empty.

This also holds true when producing XML: if object value equals attribute default value, then attribute will not be produced and if object value equals element default value, then empty element will be produced.

No element will be produced only when object value is `null`.