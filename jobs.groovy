
def giturl = 'https://github.com/MNT-Lab/d323dsl.git'
job ("MNTLAB-ukuchynski-main-build-job") {
//    description 'Main job'
    parameters {
        choiceParam('BRANCH_NAME', ['ukuchynski', 'master'], 'Branch name')
        activeChoiceParam('Next_job') {
            description('Choose job')
            choiceType('CHECKBOX')
            groovyScript {
                script('''return ["MNTLAB-ukuchynski-child1-build-job",
                        "MNTLAB-ukuchynski-child2-build-job",
                        "MNTLAB-ukuchynski-child3-build-job",
                        "MNTLAB-ukuchynski-child4-build-job"]''')
            }
        }
    }
//  BUILD_TRIGGER
    steps {
        downstreamParameterized {
            trigger('$Next_job') {
                block {
                    buildStepFailure('FAILURE')
                    failure('FAILURE')
                    unstable('UNSTABLE')
                }
                parameters {
                    predefinedProp('BRANCH_NAME', '$BRANCH_NAME')
                }
            }
        }
    }
}

for (i in (1..4)) {
    job("MNTLAB-ukuchynski-child${i}-build-job") {

        scm {
            git {
                remote {
                    github("MNT-Lab/d323dsl", "https")
                }
                branch('$BRANCH_NAME')
            }
        }

        parameters {
            activeChoiceParam('BRANCH_NAME') {
                description('Choose branch')
                choiceType('SINGLE_SELECT')
                groovyScript {
                    script('''("git ls-remote -h https://github.com/MNT-Lab/d323dsl").execute().text.readLines().collect {
                      it.split()[1].replaceAll(\'refs/heads/\', \'\')}.sort()''')
                }
            }
        }

        steps {
            shell('''
   bash script.sh > output.txt
   tar -czf ${BRANCH_NAME}_dsl_script.tar.gz jobs.groovy''')
        }
        publishers {
            archiveArtifacts {
                pattern('${BRANCH_NAME}_dsl_script.tar.gz')
                allowEmpty(false)
                onlyIfSuccessful(false)
                fingerprint(false)
                defaultExcludes(true)
            }
        }
    }
}

