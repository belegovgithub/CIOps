library 'ci-libs'

def call(Map pipelineParams) {

podTemplate(yaml: """
kind: Pod
metadata:
  name: egov-deployer
spec:
  containers:
  - name: egov-deployer
    image: egovio/egov-deployer:3-master-931c51ff
    command:
    - cat
    tty: true
    volumeMounts:
      - name: kube-config
        mountPath: /root/.kube     
    resources:
      requests:
        memory: "256Mi"
        cpu: "200m"
      limits:
        memory: "2048Mi"
        cpu: "2000m"  
  volumes:
  - name: kube-config
    secret:
        secretName: "${pipelineParams.environment}-kube-config"                    
"""
    ) {
        node(POD_LABEL) {
            git url: pipelineParams.repo, branch: pipelineParams.branch, credentialsId: 'git_read'
                stage('Deploy Images') {
                        container(name: 'egov-deployer', shell: '/bin/sh') {
                            sh """
                                /opt/egov/egov-deployer deploy --helm-dir `pwd`/${pipelineParams.helmDir} -c=${env.CLUSTER_CONFIGS}  -e ${pipelineParams.environment} "${env.IMAGES}"
                            """
                            }
                }
        }
    }


}
