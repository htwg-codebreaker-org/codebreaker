# Offizielles Scala/SBT Image mit Java 21
FROM sbtscala/scala-sbt:eclipse-temurin-21.0.6_7_1.10.11_3.6.4

# JavaFX und X11 Libraries installieren
RUN apt-get update && apt-get install -y \
    openjfx \
    libopenjfx-java \
    libgl1 \
    libgtk-3-0 \
    && rm -rf /var/lib/apt/lists/*

# Arbeitsverzeichnis
WORKDIR /app

# Projekt kopieren
COPY . .

# Dependencies laden und kompilieren
RUN sbt compile

# Startbefehl
CMD ["sbt", "run"]