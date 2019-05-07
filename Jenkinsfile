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
                        /opt/netbeans/extide/ant/bin/ant -Dnbplatform.default.netbeans.dest.dir=/opt/netbeans/ -Dnbplatform.default.harness.dir='\${nbplatform.default.netbeans.dest.dir}/harness' nbm | ccze -A
                    """
                }
                archiveArtifacts '**/*.nbm'
            }
        }
    }
}