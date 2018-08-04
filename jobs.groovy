joblist = [];

for(i=1; i<=4; i++){

    jobname = 'MNTLAB-knovichuk-child' + i + '-build-job'

    job(jobname){

        scm {
            github('MNT-Lab/d323dsl', '$BRANCH_NAME')
        }
    
        parameters {
            description ('Child' + i + ' job')
            activeChoiceParam('BRANCH_NAME') {
                    description('Choose branch')
                    choiceType('SINGLE_SELECT')
                    groovyScript {
                           script('("git ls-remote -h https://github.' +
                                   'com/MNT-Lab/d323dsl").execute().' +
                                   'text.readLines().collect { it.' +
                                   'split()[1].replaceAll(\'refs/' +
                                   'heads/\', \'\')}.unique()')
                    }
                }
        }

        steps {
                shell('''sh script.sh > output.txt; tar -czvf "$BRANCH_NAME"_dsl_script.tar.gz jobs.groovy''')
            }
        publishers {
            archiveArtifacts {
                pattern('${BRANCH_NAME}_dsl_script.tar.gz, output.txt')
            }
        }
    }

    joblist.add('"' + jobname + '"')
}


job('MNTLAB-knovichuk-main-build-job'){

    parameters {
        description 'Main job'
        activeChoiceParam('Jobs') {
            description('Select a job to execute')
            choiceType('CHECKBOX')
            groovyScript {
                script(joblist.toString())
                fallbackScript('return ["ERROR"]')
                }
          }
          choiceParam('BRANCH_NAME', ['knovichuk', 'master'], 'Choose branch')
    }

    steps {
        downstreamParameterized {
            trigger('$JOBS') {
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
