#!/bin/bash

# S'assurer que buildx est activé et créer un nouveau builder si nécessaire
docker buildx create --name multiarch --use || true

# Construction de l'image JVM pour multi-architectures
echo "Building JVM image for multiple architectures..."
docker buildx build --platform linux/amd64,linux/arm64 \
  -f src/main/docker/Dockerfile.jvm \
  -t quarkus/krabotnotif-jvm:latest \
  --push .

# Construction de l'image native pour multi-architectures
echo "Building native image for multiple architectures..."
docker buildx build --platform linux/amd64,linux/arm64 \
  -f src/main/docker/Dockerfile.native \
  -t quarkus/krabotnotif-native:latest \
  --push .

echo "Multi-architecture build complete!"
