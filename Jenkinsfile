pipeline {
    agent any

    environment {
        REGISTRY = "docker.pietr.space"
        PROJECT = "pietr-space"
        IMAGE_NAME = "pietr-space_lobby"
    }

    stages {
        stage('Build Docker Image') {
            when {
                branch 'main'
            }
            agent {
                label 'debian12'
            }
            steps {
                script {
                    def commit = sh(returnStdout: true, script: 'git rev-parse --short HEAD').trim()
                    def branch = env.BRANCH_NAME ?: 'unknown'

                    branch = branch.replaceAll('/', '-')

                    def tag = "${branch}-${commit}-${BUILD_NUMBER}"
                    def image = "${REGISTRY}/${PROJECT}/${IMAGE_NAME}:${tag}"

                    echo "Building and pushing image: ${image}"

                    docker.withRegistry("https://${REGISTRY}", 'pietr-space_harbor') {
                        def img = docker.build("${PROJECT}/${IMAGE_NAME}:${tag}")
                        img.push()
                        img.push('latest')
                    }
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        success {
            echo "Build succeeded! Image pushed to registry."
        }
        failure {
            echo 'Build failed. Please check logs.'
        }
    }
}
