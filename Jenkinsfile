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
    }
}