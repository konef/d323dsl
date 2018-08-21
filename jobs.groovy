def giturl = 'https://github.com/konef/d323dsl.git'

job ("MNTLAB-{ikonev}-main-build-job") {

  	description("Лучшая main job ever")
   
  parameters {

        choiceParam('BRANCH_NAME', ['ikonev', 'master'], 'Branch name')

        activeChoiceParam('Next_job') {

            description('Choose job')

            choiceType('CHECKBOX')

        groovyScript {

            script('''return ["MNTLAB-{ikonev}-child1-build-job", "MNTLAB-{ikonev}-child2-build-job", "MNTLAB-{ikonev}-child3-build-job",
  "MNTLAB-{ikonev}-child4-build-job"]''')

            }
        }
    }

    steps {

        downstreamParameterized {

            trigger('$Next_job') {

                block {

                    buildStepFailure('FAILURE')

                    failure('FAILURE')

                    unstable('UNSTABLE')
            }        
         }
      }
   }
}

 // CHILD JOB`S

1.upto(4, {
  freeStyleJob("MNTLAB-{ikonev}-child${it}-build-job") {
  
    scm {
        git {
            remote {

              github("konef/d323dsl", "https")

                }

               branch("*/ikonev")
           }
        }


     parameters {

         activeChoiceParam('BRANCH_NAME') {

            description('Choose branch')

              choiceType('SINGLE_SELECT')

                groovyScript {

                    script('''def gettags = ("git ls-remote -h https://github.com/konef/d323dsl.git").execute()
return gettags.text.readLines().collect {it.split()[1].replaceAll('refs/heads/', '').replaceAll('refs/tags/', '')}''')

           }
        }
     }
    steps {
                shell('''bash script.sh > output.txt
				tar -czf ikonev_dsl_script.tar.gz *''')
            }
				publishers {
					archiveArtifacts {
						pattern('**')
						onlyIfSuccessful(false)
			
        }
	}

  }
})
