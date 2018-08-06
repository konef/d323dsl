job('MNTLAB-apatapniou-main-build-job'){
    description 'main'
     parameters {
        activeChoiceParam('BUILD_TRIGGER') {
            description('Select Job')
            choiceType('CHECKBOX')
            groovyScript {
                script('''list = []
for(i in 1..4){
  list.add("MNTLAB-apatapniou-child${i}-build-job")
    }
return list''')

            }
        }
        choiceParam('BRANCH_NAME', ['apatapniou', 'master'], '')

    }
       steps {
        downstreamParameterized {
            trigger('$BUILD_TRIGGER') {
                block {
                    buildStepFailure('FAILURE')
                    failure('FAILURE')
                    unstable('UNSTABLE')
                }
               parameters {
                    currentBuildParameters()
}
         
    }
}
       }
    
}

  for(i in 1..4) {
        freeStyleJob("MNTLAB-apatapniou-child${i}-build-job") {
             
        scm {
            github('MNT-Lab/d323dsl', '$BRANCH_NAME')
        }
        parameters {
            activeChoiceParam('BRANCH_NAME') {
                description('Select the branch')
                choiceType('SINGLE_SELECT')
                groovyScript {
                    script('''("git ls-remote -h https://github.com/MNT-Lab/d323dsl").execute().text.readLines().collect {
  it.split()[1].replaceAll(\'refs/heads/\', \'\')
   }.sort()''')
                }
            }
}
       
    steps{
      shell('''bash script.sh > output.txt
tar -czvf \${BRANCH_NAME}_dsl_script.tar.gz output.txt script.sh jobs.groovy''')
    }
             publishers {
                archiveArtifacts {
                    pattern('${BRANCH_NAME}_dsl_script.tar.gz')
                    onlyIfSuccessful()
                }
}
  }
}
