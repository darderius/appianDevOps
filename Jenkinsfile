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
    
    
   //stage("Install FitNesse for Appian") {
	//  steps {
	//	script {
	//	  def jenkinsUtils = load "groovy/JenkinsUtils.groovy"
	//
	//	  // Limpia el directorio de trabajo de F4A
	//	  sh "rm -rf f4a"
    //
	//	  // 1) Extrae el paquete general f4a
	//	  sh "unzip -o appian-devops/f4a.zip -d f4a"

	//	  // 2) Dentro de f4a, extrae el ZIP de FitNesse for Appian
	//	  //    (ajusta el nombre si es distinto, pero por el log es fitnesse-for-appian-25.2.zip)
	//	  sh "unzip -o 'f4a/fitnesse-for-appian-25.2.zip' -d f4a"

	//	  // Ahora sí existe f4a/FitNesseForAppian/configs/metrics.properties
	//	  jenkinsUtils.setProperty(
	//		"f4a/FitNesseForAppian/configs/metrics.properties",
	//		"pipeline.usage",
	//		"true"
	//	  )

	//	  // Copia suites y users.properties desde tu ZIP appian-devops/f4a.zip
	//	  sh "cp -a appian-devops/f4a/test_suites/. f4a/FitNesseForAppian/FitNesseRoot/FitNesseForAppian/Examples/"
	//	  sh "cp appian-devops/f4a/users.properties f4a/FitNesseForAppian/configs/users.properties"
	//	}
	//  }
	//}

		 
	//stage("Build Package") {
    //  steps {
    //    script {
    //      def jenkinsUtils = load "groovy/JenkinsUtils.groovy"
    //      jenkinsUtils.buildPackage("version-manager.properties")
    //    }
    //  }
    //}

    stage("Inspect Package - Test") {
      steps {
        script {
          def properties = readProperties file: "devops\\deploymentmanagement.test.properties"
          DEPLOYMENTDESCRIPTION = properties['deploymentDescription']
          DEPLOYMENTNAME = properties['deploymentName']
          SITEBASEURL = properties['url']
          APIKEY = properties['siteApiKey']
    //      PACKAGEFILENAME = properties['packageFileName']
			PACKAGEFILENAME = "${APPLICATIONNAME}.zip"
          def jenkinsUtils = load "groovy/JenkinsUtils.groovy"
          jenkinsUtils.inspectPackage("${APPLICATIONNAME}.test.properties")
        }
      }
    }
	
	//stage("Aquaman Analyse Patch") {
    //  steps {
    //    script {
    //      def properties = readProperties file: "devops\\deploymentmanagement.test.properties"
    //      DEPLOYMENTDESCRIPTION = properties['deploymentDescription']
    //      DEPLOYMENTNAME = properties['deploymentName']
    //      SITEBASEURL = properties['url']
    //      APIKEY = properties['siteApiKey']
    //      PACKAGEFILENAME = properties['packageFileName']
	//	  APPLICATIONUUID = properties['applicationUUID']
    //      def jenkinsUtils = load "groovy/JenkinsUtils.groovy"
	//	  //En este caso el properties pasado se corresponde con el de customizacion de la aplicacion
    //      jenkinsUtils.requestPatchAnalysis() 
    //      
    //    }
    //  }
    //}


    
    stage("Create Deployment Request - Test") {
      steps {
        script {
          def properties = readProperties file: "devops\\deploymentmanagement.test.properties"
          DEPLOYMENTDESCRIPTION = properties['deploymentDescription']
          DEPLOYMENTNAME = properties['deploymentName']
          SITEBASEURL = properties['url']
          APIKEY = properties['siteApiKey']
          //PACKAGEFILENAME = properties['packageFileName']
		  PACKAGEFILENAME = "${APPLICATIONNAME}.zip"
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
          //PACKAGEFILENAME = properties['packageFileName']
		  PACKAGEFILENAME = "${APPLICATIONNAME}.zip"
          def jenkinsUtils = load "groovy/JenkinsUtils.groovy"
          jenkinsUtils.checkDeploymentStatus()
        }
      }
    }
	
	//stage("Execute Rule Tests") {
    //  steps {
    //    script {
    //      def properties = readProperties file: "devops\\deploymentmanagement.test.properties"
    //      DEPLOYMENTDESCRIPTION = properties['deploymentDescription']
    //      DEPLOYMENTNAME = properties['deploymentName']
    //      SITEBASEURL = properties['url']
    //      APIKEY = properties['siteApiKey']
    //      PACKAGEFILENAME = properties['packageFileName']
	//	  APPLICATIONUUID = properties['applicationUUID']
	//	  APPLICATIONNAME = properties['applicationName']
    //      def jenkinsUtils = load "groovy/JenkinsUtils.groovy"
	//	  //En este caso el properties pasado se corresponde con el de customizacion de la aplicacion
    //      jenkinsUtils.requestApplicationTests() 
    //    }
    //  }
    //}

    //stage("Tag Successful Import into Test") {
    //  steps {
    //    script {
    //      def githubUtils = load "groovy/GitHubUtils.groovy"
    //      githubUtils.tagSuccessfulImport("TEST")
    //    }
    //  }
    //}
    //stage("Run Integration Tests in Docker") {
    //  steps {
    //    script {
    //      def jenkinsUtils = load "groovy/JenkinsUtils.groovy"
    //     jenkinsUtils.runTestsDocker("fitnesse-automation.integrate.properties")
    //    }
    //  }
    //  post {
    //    always {
    //      sh script: "docker-compose -f docker/docker-compose.yml down", returnStatus: true
    //      dir("f4a/FitNesseForAppian"){ junit "fitnesse-results.xml" }
    //   }
    //    failure {
    //      script {
    //        def jenkinsUtils = load "groovy/JenkinsUtils.groovy"
    //        archiveArtifacts artifacts: jenkinsUtils.retrieveLogs("fitnesse-automation.integrate.properties"), fingerprint: true
    //      }
    //    }
    //  }
    //}
	
	stage("Run Integration Tests") {
	  steps {
		bat '''
		  cd /d C:\\Datos\\Software\\Fitnesse\\fitnesse-for-appian-24.3.0

		  rem Arrancar FitNesse en background (si no está ya como servicio)
		  start /MIN "" start.bat

		  rem Pequeña espera para que arranque (usando ping en vez de timeout)
		  ping 127.0.0.1 -n 10 >NUL

		  rem Ejecutar la JenkinsSuite y guardar resultados en HTML
		  curl -s "http://localhost:8980/FitNesseForAppian.JenkinsSuite?suite" ^
			-o fitnesse-results.html
		'''
	  }
	  post {
		always {
		  dir('C:\\Datos\\Software\\Fitnesse\\fitnesse-for-appian-24.3.0') {
			// Archivar el fichero para descargarlo/verlo
			archiveArtifacts artifacts: 'fitnesse-results.html', fingerprint: true

			// Publicar el HTML como informe en Jenkins (requiere plugin HTML Publisher)
			publishHTML(target: [
			  reportDir: '.',
			  reportFiles: 'fitnesse-results.html',
			  reportName: 'FitNesse Report'
			])
		  }
		}
	  }
	}
		   
   
  }
}


