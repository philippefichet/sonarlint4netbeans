pipeline {
    agent none
    stages {
        stage('Preparation') {
            agent any
            steps {
                git url: 'https://github.com/philippefichet/sonarlint4netbeans'
            }
        }
        stage('Build') {
            agent {
              dockerfile true
            }
            steps {
              ansiColor('xterm') {
                sh """
                  /opt/apache-maven-3.6.1/bin/mvn -Dnetbeans.installation=/opt/netbeans/ install
                """
              }
              archiveArtifacts '**/*.nbm'
            }
        }
    }
}
