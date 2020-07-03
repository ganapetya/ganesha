Sample rest service on spring boot

Build and run on docker:

docker build -t demo .
docker run -p 8081:8081 demo
docker save --output demo-export.tar demo
docker load --input demo-export.tar
