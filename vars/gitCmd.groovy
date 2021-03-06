// Licensed to Elasticsearch B.V. under one or more contributor
// license agreements. See the NOTICE file distributed with
// this work for additional information regarding copyright
// ownership. Elasticsearch B.V. licenses this file to you under
// the Apache License, Version 2.0 (the "License"); you may
// not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

/**
Execute a git command against the git repo, using the credentials passed.
It requires to initialise the pipeline with githubEnv() first.

  gitCmd(credentialsId: 'my_credentials', cmd: 'push', args: '-f')
*/

def call(Map params = [:]) {
  if(!isUnix()){
    error('gitCmd: windows is not supported yet.')
  }
  def cmd =  params.containsKey('cmd') ? params.cmd : error('gitCmd: missing git command')
  def args =  params.containsKey('args') ? params.args : ''
  def credentialsId = (params.containsKey('credentialsId') && params.credentialsId.trim()) ? params.credentialsId : '2a9602aa-ab9f-4e52-baf3-b71ca88469c7-UserAndToken'
  def store = params.get('store', false)
  withCredentials([
    usernamePassword(
      credentialsId: credentialsId,
      passwordVariable: 'GIT_PASSWORD',
      usernameVariable: 'GIT_USERNAME')]) {
    def folder = '.git'
    def filename = "${cmd}.log"
    def logFilename = fileExists(folder) ? "${folder}/${filename}" : "${filename}"
    def storeFlag = store ? "> ${logFilename} 2>&1" : ''
    try {
      sh(label: "Git ${cmd}", script: "git ${cmd} https://\${GIT_USERNAME}:\${GIT_PASSWORD}@github.com/${ORG_NAME}/${REPO_NAME}.git ${args} ${storeFlag}")
    } catch(err) {
      if (store) {
        log(level: 'WARN', text: "gitCmd failed, further details in the archived file '${logFilename}'")
      }
      throw err
    } finally {
      if (store) {
        if (fileExists(folder)) {
          dir(folder) {
            archiveArtifacts(artifacts: "${filename}")
          }
        } else {
          archiveArtifacts(artifacts: "${filename}")
        }
      }
    }
  }
}
