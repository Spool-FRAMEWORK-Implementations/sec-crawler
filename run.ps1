mvn clean package
docker stop synthea
docker rm synthea
docker build -t synthea .
docker run -d --name synthea -p :8080 -v "C:/Users/Javito/spool:/host_spool" synthea