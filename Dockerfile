# Build from repository root: docker build -t mini-payroll .

FROM node:20-alpine AS frontend-build
WORKDIR /ui
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build -- --configuration=production

FROM maven:3.9.9-eclipse-temurin-17 AS backend-build
WORKDIR /app
COPY backend/ ./
COPY --from=frontend-build /ui/dist/mini-payroll-ui/browser/ ./src/main/resources/static/
RUN chmod +x mvnw && ./mvnw -DskipTests package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=backend-build /app/target/*.jar app.jar
USER app
EXPOSE 8080
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
