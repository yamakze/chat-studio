docker-compose-environment up -d

mvn package
cd chat-studio-app
java -jar target/chat-studio-app.jar