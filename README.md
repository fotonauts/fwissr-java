Fwissr Java
===========

[![Build Status](https://travis-ci.org/fotonauts/fwissr.png)](https://travis-ci.org/fotonauts/fwissr)

Java port for [fwissr](http://github.com/fotonauts/fwissr), a simple configuration registry tool
by [Fotonauts](http://www.fotopedia.com).

Scope
=====

Fwissr Java focus on the "library" aspect of fwissr : accessing configuration from java code.

The command line query tool is not available, so you may want to setup the ruby or python port to complement it.

[fwissr](http://github.com/fotonauts/fwissr) README is a good place to start to setup your configuration repository.

Use it
======

In your pom.xml...

```XML
  <dependency>
      <groupId>com.fotonauts</groupId>
      <artifactId>fwissr</artifactId>
      <version>0.0.2</version>
  </dependency>
```

and in your code

```Java
import com.fotonauts.fwissr;
[...]
Fwissr fwissr = new Fwissr("/etc/fwissr");
fwissr.get("/stuff/foo");
```

SmaterList and SmarterMap
=========================

They were initially designed to offer some feature than are a bit difficult to use with java maps and lists, 
like deep freeze or deep cloning. But they also come in very handy when instantiating pseudo-litteral 
json-like structure in code (for unit tests...).

```Java
import static com.fotonauts.fwissr.SmarterList.l;
import static com.fotonauts.fwissr.SmarterMap.m;
[...]
SmarterMap jsonObject = m(
        "some_list", l("value1", "value2"), 
        "some_sub_object", m("inner", "field) 
);
System.err.println(jsonObject.toYaml);
System.err.println(jsonObject.toJson);
```


More customization
==================

The java port allows the same extensions than the ruby port:
    - registries can be set up programatically
    - alternative subclasses of com.fotonauts.fwissr.Source can be implemented

Credits
=======

From Fotonauts:

- Aymerick Jéhanne [@aymerick](https://twitter.com/aymerick)
- Pierre Baillet [@octplane](https://twitter.com/octplane)
- Mathieu Poumeyrol (http://gihub.com/kali)

Copyright (c) 2013-2014 Fotonauts released under the ASF license.
