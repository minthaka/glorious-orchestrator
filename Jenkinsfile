pipeline {
    agent any

    tools {
        // This name MUST match what you configured in
        // Manage Jenkins -> Tools -> Maven Installations
        maven 'Maven 3.9.12'
    }

    stages {
        stage('Checkout') {
            steps {
                // Grabs the code from the Git repo linked to the job
                checkout scm
            }
        }

        stage('Code Quality (SonarQube)') {
            steps {
                script {
                    // 'MySonarServer' must match the name in
                    // Manage Jenkins -> System -> SonarQube servers
                    // The URL there should be http://sonarqube-local:9000
                    withSonarQubeEnv('MySonarServer') {
                        sh 'mvn sonar:sonar'
                    }
                }
            }
        }

        stage('Build & Unit Tests') {
            steps {
                // Compiles code and runs JUnit tests
                // We use -DskipTests=false to ensure quality
                sh 'mvn clean package'
            }
        }

        stage('Archive Artifacts') {
            steps {
                // This saves the resulting JAR so you can
                // download it from the Jenkins UI
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }
    }

    post {
        success {
            echo 'Build, Quality Check, and Archiving completed successfully!'
        }
        failure {
            echo 'Something went wrong. Check the console output above.'
        }
    }
}