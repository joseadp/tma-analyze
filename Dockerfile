FROM    tma-utils:0.1

ENV     analyze      /atmosphere/tma/analyze

#       Adding Monitor Client
WORKDIR ${analyze}/tma-analyze

#       Prepare by downloading dependencies
COPY    pom.xml     ${analyze}/tma-analyze/pom.xml

#       Adding source, compile and package into a fat jar
COPY    src ${analyze}/tma-analyze/src
RUN     ["mvn", "install"]

RUN     ["cp", "-r", "bin", "/atmosphere/tma/analyze/bin"]

CMD ["java", "-jar", "/atmosphere/tma/analyze/bin/tma-analyze-0.0.1-SNAPSHOT.jar"]
