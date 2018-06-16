FROM java:8-jdk
MAINTAINER Neville Dipale <neville@data-engine.co.za>

RUN mkdir /geofancy
ADD / /geofancy
RUN mkdir /jars

RUN  chmod +x /geofancy/gradlew
RUN ./geofancy/gradlew assemble
RUN ./geofancy/gradlew fatJar

ADD /geofancy/build/libs/*.jar /jars
WORKDIR /jars

CMD ["java", "-jar", "/jars/geofancy-0.0.1.jar"]

