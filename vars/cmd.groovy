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
  Wrapper to run a script without the hassle to know what's the OS system underneath.
  It runs either sh when running on a *Nix agent or Bat on Windows.

  _NOTE_: bat with returnStdout requires @echo off to bypass the known issue
          https://issues.jenkins-ci.org/browse/JENKINS-44569

  cmd(label: 'foo', script: 'git fetch --all')
*/
def call(Map params = [:]) {
  def returnStdout = params.get('returnStdout', false)
  
  if (isUnix()) {
    return sh(params)
  } else {
    if(returnStdout) {
      def command = """@echo off
                    ${params.script}"""
      params.script = command
    }
    return bat(params)
  }
}
