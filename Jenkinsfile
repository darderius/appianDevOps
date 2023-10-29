pipeline {
  agent any
environment {

SITEBASEURL = null
APIKEY = null
PACKAGEFILENAME = null
initiateInspectionJson = null
deploymentResponseJson = null
response = null
warnings = null
errors = null
DEPLOYMENTNAME = null
DEPLOYMENTDESCRIPTION = null
}
  stages {
    
    
   
    stage("Build Package") {
      steps {
        script {
		  def properties = readProperties file: "devops\\deploymentmanagement.test.properties"
		  REPOUSERNAME = properties['repoUsername']
		  REPOPASSWORD = properties['repoPassword']
          def jenkinsUtils = load "groovy/JenkinsUtils.groovy"
          jenkinsUtils.buildPackage("version-manager.properties", REPOUSERNAME, REPOPASSWORD)
        }
      }
    }

    stage("Inspect Package - Test") {
      steps {
        script {
          def properties = readProperties file: "devops\\deploymentmanagement.test.properties"
          DEPLOYMENTDESCRIPTION = properties['deploymentDescription']
          DEPLOYMENTNAME = properties['deploymentName']
          SITEBASEURL = properties['url']
          APIKEY = properties['siteApiKey']
          PACKAGEFILENAME = properties['packageFileName']
          def jenkinsUtils = load "groovy/JenkinsUtils.groovy"
          jenkinsUtils.inspectPackage("${APPLICATIONNAME}.test.properties")
        }
      }
    }
    
    stage("Create Deployment Request - Test") {
      steps {
        script {
          def properties = readProperties file: "devops\\deploymentmanagement.test.properties"
          DEPLOYMENTDESCRIPTION = properties['deploymentDescription']
          DEPLOYMENTNAME = properties['deploymentName']
          SITEBASEURL = properties['url']
          APIKEY = properties['siteApiKey']
          PACKAGEFILENAME = properties['packageFileName']
          def jenkinsUtils = load "groovy/JenkinsUtils.groovy"
          jenkinsUtils.createDeployment("${APPLICATIONNAME}.test.properties")
          


        }
      }
    }
    stage("Check Deployment Status - Test") {
      steps {
        script {
          def properties = readProperties file: "devops\\deploymentmanagement.test.properties"
          DEPLOYMENTDESCRIPTION = properties['deploymentDescription']
          DEPLOYMENTNAME = properties['deploymentName']
          SITEBASEURL = properties['url']
          APIKEY = properties['siteApiKey']
          PACKAGEFILENAME = properties['packageFileName']
          def jenkinsUtils = load "groovy/JenkinsUtils.groovy"
          jenkinsUtils.checkDeploymentStatus()
        }
      }
    }
    stage("Tag Successful Import into Test") {
      steps {
        script {
          def githubUtils = load "groovy/GitHubUtils.groovy"
          githubUtils.tagSuccessfulImport("TEST")
        }
      }
    }

  }
}


