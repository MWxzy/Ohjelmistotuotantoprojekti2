pipeline {
    agent any

    tools {
        maven 'maven-3.9'
    }

    environment {
        DOCKER_IMAGE = 'junkkari/temperature-converter-gui:latest'
        DOCKER_CREDENTIALS_ID = 'dockerhub-credentials'
        MAVEN_OPTS = '-Dfile.encoding=UTF-8'
        PATH = "C:\\Users\\kaspe\\AppData\\Local\\Programs\\DockerDesktop\\resources\\bin;${env.PATH}"
    }

    stages {
        stage('Checkout') {
            steps { checkout scm }
        }

        stage('Build and Test') {
            steps { bat 'mvn -B clean verify' }
        }

        stage('Publish Test Results') {
            steps { junit '**/target/surefire-reports/*.xml' }
        }

        stage('Publish Coverage Report') {
            steps { jacoco() }
        }

        stage('Build Docker Image') {
            steps {
                bat "docker build --no-cache -t ${env.DOCKER_IMAGE} ."
            }
        }

        stage('Verify Image') {
            steps {
                bat "docker run --rm --entrypoint ls ${env.DOCKER_IMAGE} -la /app"
            }
        }

        stage('Deploy to Docker Hub') {
            steps {
                withCredentials([usernamePassword(credentialsId: "${env.DOCKER_CREDENTIALS_ID}",
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS')]) {
                    bat "docker login -u ${env.DOCKER_USER} -p ${env.DOCKER_PASS}"
                    bat "docker push ${env.DOCKER_IMAGE}"
                }
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'target/site/jacoco/**, target/reports/**', allowEmptyArchive: true
        }
    }
}
