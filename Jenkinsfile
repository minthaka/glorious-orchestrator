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
                // Generates the .class files needed for SonarQube
                sh 'mvn clean compile'
            }
        }

        stage('Code Quality (SonarQube)') {
            steps {
                script {
                    withSonarQubeEnv('MySonarServer') {
                        // Using your specific token
                        sh 'mvn sonar:sonar -Dsonar.token=squ_1bda44233ed6c1648ef650740f90f74e42678bdf'
                    }
                }
            }
        }

        stage('Build, Test & Install') {
            steps {
                // CHANGE: Use 'install' instead of 'package'
                // This "publishes" the JAR to the local .m2 cache so other
                // projects (like the orchestrator) can use it as a dependency.
                sh 'mvn install -DskipTests=false'
            }
        }

        stage('Archive Artifacts') {
            steps {
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }
    }

    post {
        success {
            echo 'Build, Quality Check, and Local Install completed successfully!'
        }
        failure {
            echo 'Build failed. Check the console output.'
        }
    }
}