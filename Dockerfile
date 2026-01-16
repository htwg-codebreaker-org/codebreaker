# Offizielles Scala/SBT Image mit Java 21
FROM sbtscala/scala-sbt:eclipse-temurin-21.0.6_7_1.10.11_3.6.4

# JavaFX und alle nötigen X11/Grafik Libraries
RUN apt-get update && apt-get install -y \
    openjfx \
    libopenjfx-java \
    libgl1 \
    libgtk-3-0 \
    libxrender1 \
    libxtst6 \
    libxi6 \
    libxrandr2 \
    libfreetype6 \
    libfontconfig1 \
    fonts-dejavu \
    && rm -rf /var/lib/apt/lists/*

# Arbeitsverzeichnis
WORKDIR /app

# Projekt kopieren
COPY . .

# Dependencies laden und kompilieren
RUN sbt compile

# JavaFX Performance-Tuning für Docker
ENV JAVA_OPTS="-Dprism.order=sw -Dprism.verbose=true"

# Startbefehl
CMD ["sbt", "run"]