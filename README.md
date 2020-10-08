## Eclipse Sirius EMF JSON

This project is part of [Eclipse Sirius](http://www.eclipse.org/sirius). It provides a JSON-based implementation of EMF resources which can be used as a drop-in replacement for the default XMI-based format.

Although it is part of Sirius, this new EMF resource type can actually be used by any EMF-based application as it does not depend on the rest of the Sirius platform (neither Sirius Desktop nor Sirius Web).

### Building

The build uses [Tycho](http://www.eclipse.org/tycho/). To launch a complete build, issue:

```
mvn clean package -f releng/org.eclipse.sirius.emfjson.releng/pom.xml
```

from the top-level directory. The resulting update-site (p2 repository) can be found in `packaging/org.eclipse.sirius.update/target/repository`.

### License

Eclipse Public License - v 2.0
