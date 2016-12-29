node {
    stage 'Checkout'
    git 'https://github.com/ATLauncher/ATLauncher.git'

    stage 'Setup'
    def maven = docker.image('ryantheallmighty/maven-custom');
    maven.pull();

    stage 'Test'
    maven.inside('-v /m2repo:/m2repo') {
        sh 'mvn -Dmaven.repo.local=/m2repo clean test'
    }

    stage 'Build'
    maven.inside('-v /m2repo:/m2repo') {
        sh 'mvn -Dmaven.repo.local=/m2repo -DskipTests clean install'
    }
    
    stage 'Archive'
    archive 'target/ATLauncher-*.exe,target/ATLauncher-*.zip,target/ATLauncher-*.jar'
}
