mvn clean package
docker stop crawler
docker rm crawler
docker build -t crawler .
docker run -d --name crawler -p :8080 crawler