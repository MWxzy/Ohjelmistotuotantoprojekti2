pipeline {
    agent any

    tools {
        maven 'maven-3.9'
    }

    environment {
        DOCKER_IMAGE = 'junkkari/temperature-converter-gui:latest'
        DOCKER_CREDENTIALS_ID = 'dockerhub-credentials'
        SONARQUBE_SERVER = 'SonarQubeServer'
        SONAR_PROJECT_KEY = 'otp1-temperature-converter'
        MAVEN_OPTS = '-Dfile.encoding=UTF-8'
        JAVA_HOME = 'C:\\Program Files\\Microsoft\\jdk-21.0.12.101-hotspot'
        PATH = "C:\\sonarscanner\\bin;C:\\Users\\kaspe\\AppData\\Local\\Programs\\DockerDesktop\\resources\\bin;${env.PATH}"
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

        stage('SonarQube Analysis') {
            steps {
                withCredentials([string(credentialsId: 'SONAR_TOKEN', variable: 'SONAR_TOKEN')]) {
                    withSonarQubeEnv("${env.SONARQUBE_SERVER}") {
                        bat """
                            sonar-scanner ^
                              -Dsonar.projectKey=${env.SONAR_PROJECT_KEY} ^
                              -Dsonar.projectName=Temperature-Converter ^
                              -Dsonar.projectVersion=1.0-SNAPSHOT ^
                              -Dsonar.host.url=%SONAR_HOST_URL% ^
                              -Dsonar.token=%SONAR_TOKEN%
                        """
                    }
                }
            }
        }

        stage('Quality Gate') {
            steps {
                withCredentials([string(credentialsId: 'SONAR_TOKEN', variable: 'SONAR_TOKEN')]) {
                    script {
                        // SonarQube Community Build rejects loopback webhook URLs,
                        // so poll the quality gate status through the web API.
                        def status = 'NONE'
                        for (int attempt = 0; attempt < 12; attempt++) {
                            bat '''
                                powershell -NoProfile -Command "(Invoke-RestMethod -Uri 'http://localhost:9000/api/qualitygates/project_status?projectKey=otp1-temperature-converter' -Headers @{ Authorization = 'Bearer ' + $env:SONAR_TOKEN }).projectStatus.status" > sonar-quality-gate.txt
                            '''
                            status = readFile('sonar-quality-gate.txt').trim()
                            if (status == 'OK' || status == 'ERROR') {
                                break
                            }
                            sleep(time: 5, unit: 'SECONDS')
                        }
                        echo "SonarQube quality gate: ${status}"
                        if (status != 'OK') {
                            error "SonarQube quality gate failed: ${status}"
                        }
                    }
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                bat "docker build -t ${env.DOCKER_IMAGE} ."
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
