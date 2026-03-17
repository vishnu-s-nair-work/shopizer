# ── Stage 1: build ──────────────────────────────────────────────
FROM eclipse-temurin:11-jdk-alpine AS builder
WORKDIR /build

# Copy Maven wrapper and POMs first for layer caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY sm-core-model/pom.xml sm-core-model/
COPY sm-core-modules/pom.xml sm-core-modules/
COPY sm-core/pom.xml sm-core/
COPY sm-shop-model/pom.xml sm-shop-model/
COPY sm-shop/pom.xml sm-shop/

# Download dependencies (cached unless POMs change)
RUN ./mvnw -B dependency:go-offline -q

# Copy source and build
COPY sm-core-model/src sm-core-model/src
COPY sm-core-modules/src sm-core-modules/src
COPY sm-core/src sm-core/src
COPY sm-shop-model/src sm-shop-model/src
COPY sm-shop/src sm-shop/src

RUN ./mvnw -B package -DskipTests -q

# ── Stage 2: run ────────────────────────────────────────────────
FROM eclipse-temurin:11-jre-alpine
WORKDIR /app

COPY --from=builder /build/sm-shop/target/shopizer.jar app.jar

# Runtime file storage directories
RUN mkdir -p /files/store /files/repos

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
