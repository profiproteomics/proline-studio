title Proline Studio
java -Xmx2G -XX:+UseG1GC -XX:+UseStringDeduplication -XX:MinHeapFreeRatio=10 -XX:MaxHeapFreeRatio=30 -cp "prolinestudio-resultexplorer-${pom.version}.jar;lib/*"  -Djava.library.path=./sqlite4java -Dlogback.configurationFile=config/logback.xml fr.proline.studio.main.Main %*
