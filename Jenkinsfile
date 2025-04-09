pipeline {
    agent any

    tools {
            jdk 'JAVA_LOCAL'
            maven 'MAVEN_LOCAL'
    }

    parameters {
            string(name: 'DATABASE_USER', defaultValue: 'postgres', description: 'Usuário do banco de dados')
            password(name: 'DATABASE_PASSWORD', defaultValue: '', description: 'Senha do banco de dados')
    }

    stages {
        stage ('Build Backend') {
            steps {
                bat 'mvn clean package'
            }
        }
        stage ('Deploy Prod') {
            environment {
                BUILD_NUMBER = "${env.BUILD_NUMBER}"
                DB_USER = "${params.DATABASE_USER}"
                DB_PASS = "${params.DATABASE_PASSWORD}"
            }
            steps {
                bat 'docker-compose build'
                bat 'docker-compose up -d'
            }
        }
    }

}