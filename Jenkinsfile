pipeline {
    agent any
    tools {
            maven 'MAVEN_LOCAL'
        }
    stages {
        stage ('Build Backend') {
            steps {
                bat 'mvn clean package'
            }
        }
    }
}