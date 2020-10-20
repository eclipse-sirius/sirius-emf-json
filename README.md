## Eclipse Sirius EMF JSON

This project is part of [Eclipse Sirius](http://www.eclipse.org/sirius). It provides a JSON-based implementation of EMF resources which can be used as a drop-in replacement for the default XMI-based format.

Although it is part of Sirius, this new EMF resource type can actually be used by any EMF-based application as it does not depend on the rest of the Sirius platform (neither Sirius Desktop nor Sirius Web).

### Building

The build uses Maven and [Tycho](http://www.eclipse.org/tycho/). To launch a complete build, issue:

```
mvn clean package -f releng/org.eclipse.sirius.emfjson.releng/pom.xml
```

from the top-level directory.

Note that you will need to use Java 11 to run the build. Java 12 and later are currently not supported.

If you are behind a proxy, you may get Maven errors about checkstyle.org not being available. In this case you need to explicitly disable CheckStyle from the build:

```
mvn clean package -f releng/org.eclipse.sirius.emfjson.releng/pom.xml -P\!checkstyle
```

The resulting update-site (p2 repository) can be found in `packaging/org.eclipse.sirius.update/target/repository`.

### License

Eclipse Public License - v 2.0
