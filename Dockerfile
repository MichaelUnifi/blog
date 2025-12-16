FROM eclipse-temurin:17

RUN apt-get update && apt-get install -y \
    libxext6 \
    libxrender1 \
    libxtst6 \
    libxi6 \
    libxrandr2 \
    fontconfig \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY target/blog-0.0.1-SNAPSHOT-jar-with-dependencies.jar /app/blog.jar

ENV DISPLAY=:0

ENTRYPOINT ["java", "-jar", "/app/blog.jar"]