This is a Linked Data server that adds an HTML interface and
dereferenceable URLs on top of RDF data that sits in a SPARQL
store.

See [the Pubby website](http://www4.wiwiss.fu-berlin.de/pubby/)
for details and instructions.


VICTOR: 

Para compilar: mvn clean install -DskipTests -Dmaven.javadoc.skip=true
Para ejecutar: mvn jetty:run

Para que no nos pida el server tenemos que añadir: https://netbeans.org/bugzilla/show_bug.cgi?id=168960