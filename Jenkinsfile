pipeline {
    agent any
    tools {
            jdk 'JAVA_LOCAL'
            maven 'MAVEN_LOCAL'
        }
    stages {
        stage ('Build Backend') {
            steps {
                bat 'mvn clean package'
            }
        }
        stage ('Deploy Prod') {
            steps {
                bat 'docker-compose build'
                bat 'docker-compose up -d'
            }
        }
    }

}