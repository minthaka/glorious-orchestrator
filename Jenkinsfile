pipeline {
    agent any

    tools {
        maven 'Maven 3.9.12'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Compile') {
            steps {
                // -U tells Maven to check for updates of SNAPSHOT dependencies
                // like your healthcare-common library
                sh 'mvn clean compile -U'
            }
        }

        stage('Code Quality (SonarQube)') {
            steps {
                script {
                    withSonarQubeEnv('MySonarServer') {
                        sh 'mvn sonar:sonar -Dsonar.token=squ_1bda44233ed6c1648ef650740f90f74e42678bdf'
                    }
                }
            }
        }

        stage('Package') {
            steps {
                // Since this is a final service, 'package' is enough
                sh 'mvn package -DskipTests=false'
            }
        }

        stage('Archive Artifacts') {
            steps {
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }
    }

    post {
        failure {
            echo 'Build failed. Check if healthcare-common was installed recently.'
        }
    }
}